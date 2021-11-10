package music.service;

import lombok.extern.log4j.Log4j2;
import music.exception.RatingRangeException;
import music.exception.TaskInProgressException;
import music.mapper.PlayMapper;
import music.model.*;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@Log4j2
public class MediaMonkeyMigrationService implements MigrationService {

	private AtomicBoolean currentlyMigrating = new AtomicBoolean(false);

    private final String SONG_TITLE = "SongTitle";
    private final String ARTIST = "Artist";
    private final String ALBUM = "Album";
    private final String PLAY_DATE = "PlayDate";
    private final String PLAY_COUNT = "PlayCounter";
    private final String RATING = "Rating";

    private final PlayMapper playMapper;

    private final TrackService trackService;

    private final DeviceService deviceService;

    @Autowired
    public MediaMonkeyMigrationService(PlayMapper playMapper, TrackService trackService, DeviceService deviceService) {
        this.playMapper = playMapper;
        this.trackService = trackService;
        this.deviceService = deviceService;
    }

    /**
     * Connect to the specified database
     *
     * @return the Connection object
     */
    private Connection connect(String absolutePath) {
        // SQLite connection string
        String url = "jdbc:sqlite:" + absolutePath;
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(url);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return conn;
    }

    private void insertIndividualPlays(String absolutePath, Device device, MigrationResult migrationResult) {
        String sql = "SELECT s.SongTitle, s.Artist, s.Album, p.PlayDate FROM played p " +
                "INNER JOIN songs s ON p.IDSong = s.ID";

        List<Track> tracks = trackService.list();

        try (Connection conn = this.connect(absolutePath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // loop through the result set
            while (rs.next()) {
                String songTitle = rs.getString(SONG_TITLE);
                String artist = rs.getString(ARTIST);
                String album = rs.getString(ALBUM);
                String playDate = rs.getString(PLAY_DATE);

                Track track = trackService.get(songTitle, artist, album, tracks);
                MediaMonkeyPlay mediaMonkeyPlay = new MediaMonkeyPlay(songTitle, artist, album, playDate);
                if (track != null) {
                    try {
                        playMapper.insertPlay(track.getId(), mediaMonkeyPlay.getDateAsDelphi(), device.getId(), true);
                        migrationResult.getPlays().getSuccessful().add(mediaMonkeyPlay);
                    } catch (DuplicateKeyException | UncategorizedSQLException exception) {
                        log.debug("Already imported MediaMonkey track {}", songTitle, exception);
                        migrationResult.getPlays().getAlreadyImported().add(mediaMonkeyPlay);
                    }
                } else {
                    log.debug("No matching track found for MediaMonkey track {}", songTitle);
                    migrationResult.getPlays().getFailed().add(mediaMonkeyPlay);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to connect to MediaMonkey database", e);
        }
    }

    private void insertPlayCounts(String absolutePath, Device device, MigrationResult migrationResult) {
        String playCountSql = "SELECT SongTitle, Artist, Album, PlayCounter FROM songs";

        List<Track> tracks = trackService.list();

        try (Connection conn = this.connect(absolutePath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(playCountSql)) {

            // loop through the result set
            while (rs.next()) {
                String songTitle = rs.getString(SONG_TITLE);
                String artist = rs.getString(ARTIST);
                String album = rs.getString(ALBUM);
                long playCount = rs.getLong(PLAY_COUNT);

                Track track = trackService.get(songTitle, artist, album, tracks);
                MediaMonkeyPlayCount mediaMonkeyPlay = new MediaMonkeyPlayCount(songTitle, artist, album, playCount);

                if(track != null){
                    try {
                        playMapper.upsertPlayCount(track.getId(), device.getId(), playCount, true);
                        migrationResult.getPlayCounts().getSuccessful().add(mediaMonkeyPlay);
                    } catch (DuplicateKeyException |UncategorizedSQLException e) {
                        log.debug("Already imported MediaMonkey track {}", songTitle, e);
                        migrationResult.getPlayCounts().getAlreadyImported().add(mediaMonkeyPlay);
                    }
                } else {
                    log.debug("No matching track found for MediaMonkey track {}", songTitle);
                    migrationResult.getPlayCounts().getFailed().add(mediaMonkeyPlay);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to connect to MediaMonkey database", e);
        }
    }

    @Override
    public MigrationResult doImport(MultipartFile file, String deviceName, boolean importPlays, boolean importRatings) throws Exception {
		checkNotCurrentlyMigrating();
		try {
			currentlyMigrating.set(true);
			validateDatabaseFilename(file);
			Device device = deviceService.getOrInsert(deviceName);
			File temporaryDatabase = createTempDb(file);

			MigrationResult migrationResult = new MigrationResult(
				new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
				new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
				new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
			);

			if (importPlays) {
				insertPlayCounts(temporaryDatabase.getAbsolutePath(), device, migrationResult);
				insertIndividualPlays(temporaryDatabase.getAbsolutePath(), device, migrationResult);
			}
			if (importRatings) {
				insertRatings(temporaryDatabase.getAbsolutePath(), migrationResult);
			}
			currentlyMigrating.set(false);
			return migrationResult;
		} catch (Exception e) {
			currentlyMigrating.set(false);
			throw e;
		}
    }

    private void insertRatings(String absolutePath, MigrationResult migrationResult) {
        String playCountSql = "SELECT SongTitle, Artist, Album, Rating FROM songs WHERE Rating <> -1";

        List<Track> tracks = trackService.list();

        try (Connection conn = this.connect(absolutePath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(playCountSql)) {

            // loop through the result set
            while (rs.next()) {
                String songTitle = rs.getString(SONG_TITLE);
                String artist = rs.getString(ARTIST);
                String album = rs.getString(ALBUM);
                byte rating = rs.getByte(RATING);

                Track track = trackService.get(songTitle, artist, album, tracks);
                MediaMonkeyRating mediaMonkeyRating = new MediaMonkeyRating(songTitle, artist, album, rating);

                if(track != null){
                    try {
                        trackService.setRating(track.getId(), mediaMonkeyRating.getNormalizedRating());
                        migrationResult.getRatings().getSuccessful().add(mediaMonkeyRating);
                    } catch (DuplicateKeyException |UncategorizedSQLException e) {
                        log.debug("Already imported MediaMonkey track {}", songTitle, e);
                        migrationResult.getRatings().getAlreadyImported().add(mediaMonkeyRating);
                    } catch (RatingRangeException e) {
                        log.error("Track {} rating of {} is outside allowable range.", track.getId(), rating, e);
                        migrationResult.getRatings().getFailed().add(mediaMonkeyRating);
                    }
                } else {
                    log.debug("No matching track found for MediaMonkey track {}", songTitle);
                    migrationResult.getRatings().getFailed().add(mediaMonkeyRating);
                }
            }
        } catch (SQLException e) {
            log.error("Failed to connect to MediaMonkey database", e);
        }
    }

    /**
     * Read in the database from the input stream and store it as a temporary file on the server.
     */
    private File createTempDb(MultipartFile file) throws IOException {
        File temporaryDatabase = File.createTempFile("mediamonkey", ".db");
        FileUtils.copyInputStreamToFile(file.getInputStream(), temporaryDatabase);

        log.debug("Temporary database location: {}", temporaryDatabase.getAbsolutePath());

        temporaryDatabase.deleteOnExit();
        return temporaryDatabase;
    }

    /**
     * Validate that the filename matches the expected name.
     */
    private void validateDatabaseFilename(MultipartFile file) throws Exception {
        String expectedFilename = "mm.db";
        if (file == null) {
            throw new Exception("Supplied file is null.");
        }
        if (!StringUtils.isEmpty(file.getOriginalFilename()) &&
                !file.getOriginalFilename().equalsIgnoreCase(expectedFilename)) {
            throw new Exception(String.format("Imported file does not have the expected filename from a MediaMonkey database '%s'.", expectedFilename));
        }
    }

	/**
	 * Check to make sure that a migration is not currently underway.
	 */
	private void checkNotCurrentlyMigrating() throws Exception {
		if (currentlyMigrating.get()) {
			throw new TaskInProgressException("migration");
		}
	}
}
