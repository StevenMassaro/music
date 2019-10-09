package music.service;

import music.exception.RatingRangeException;
import music.mapper.PlayMapper;
import music.model.*;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.UncategorizedSQLException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

@Service
public class MediaMonkeyMigrationService implements MigrationService {

    private Logger logger = LoggerFactory.getLogger(MediaMonkeyMigrationService.class);

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
                        logger.debug(String.format("Already imported MediaMonkey track %s", songTitle), exception);
                        migrationResult.getPlays().getAlreadyImported().add(mediaMonkeyPlay);
                    }
                } else {
                    logger.debug(String.format("No matching track found for MediaMonkey track %s", songTitle));
                    migrationResult.getPlays().getFailed().add(mediaMonkeyPlay);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to connect to MediaMonkey database", e);
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
                        logger.debug(String.format("Already imported MediaMonkey track %s", songTitle), e);
                        migrationResult.getPlayCounts().getAlreadyImported().add(mediaMonkeyPlay);
                    }
                } else {
                    logger.debug(String.format("No matching track found for MediaMonkey track %s", songTitle));
                    migrationResult.getPlayCounts().getFailed().add(mediaMonkeyPlay);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to connect to MediaMonkey database", e);
        }
    }

    @Override
    public MigrationResult doImport(MultipartFile file, String deviceName, boolean importPlays, boolean importRatings) throws Exception {
        validateDatabaseFilename(file);
        Device device = deviceService.getOrInsert(deviceName);
        File temporaryDatabase = createTempDb(file);

        MigrationResult migrationResult = new MigrationResult(
                new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>()),
                new ItemImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>())
        );

        if(importPlays){
            insertPlayCounts(temporaryDatabase.getAbsolutePath(), device, migrationResult);
            insertIndividualPlays(temporaryDatabase.getAbsolutePath(), device, migrationResult);
        }
        if (importRatings){
            insertRatings(temporaryDatabase.getAbsolutePath(), migrationResult);
        }
        return migrationResult;
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
                        logger.debug(String.format("Already imported MediaMonkey track %s", songTitle), e);
                        migrationResult.getRatings().getAlreadyImported().add(mediaMonkeyRating);
                    } catch (RatingRangeException e) {
                        logger.error(String.format("Track %s rating of %s is outside allowable range.", track.getId(), rating), e);
                        migrationResult.getRatings().getFailed().add(mediaMonkeyRating);
                    }
                } else {
                    logger.debug(String.format("No matching track found for MediaMonkey track %s", songTitle));
                    migrationResult.getRatings().getFailed().add(mediaMonkeyRating);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to connect to MediaMonkey database", e);
        }
    }

    /**
     * Read in the database from the input stream and store it as a temporary file on the server.
     */
    private File createTempDb(MultipartFile file) throws IOException {
        File temporaryDatabase = File.createTempFile("mediamonkey", ".db");
        FileUtils.copyInputStreamToFile(file.getInputStream(), temporaryDatabase);

        logger.debug(String.format("Temporary database location: %s", temporaryDatabase.getAbsolutePath()));

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
}
