package music.service;

import music.mapper.PlayMapper;
import music.model.Device;
import music.model.MediaMonkeyPlay;
import music.model.PlayImportResult;
import music.model.Track;
import org.apache.commons.io.FileUtils;
import org.postgresql.util.PSQLException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.sql.*;
import java.util.*;

@Service
public class MediaMonkeyMigrationService implements MigrationService {

    private Logger logger = LoggerFactory.getLogger(MediaMonkeyMigrationService.class);

    private final String SONG_TITLE = "SongTitle";
    private final String ARTIST = "Artist";
    private final String ALBUM = "Album";
    private final String PLAY_DATE = "PlayDate";

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

    private PlayImportResult importPlaysFromDb(String absolutePath, Device device) {
        String sql = "SELECT s.SongTitle, s.Artist, s.Album, p.PlayDate FROM played p " +
                "INNER JOIN songs s ON p.IDSong = s.ID";

        PlayImportResult playImportResult = new PlayImportResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

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
                        playMapper.insertPlay(track.getId(), mediaMonkeyPlay.getDateAsDelphi(), device.getId());
                        playImportResult.getSuccessful().add(mediaMonkeyPlay);
                    } catch (DuplicateKeyException exception) {
                        playImportResult.getAlreadyImported().add(mediaMonkeyPlay);
                    }
                } else {
                    playImportResult.getFailed().add(mediaMonkeyPlay);
                }
            }
        } catch (SQLException e) {
            logger.error("Failed to connect to MediaMonkey database", e);
        }
        return playImportResult;
    }

    @Override
    public PlayImportResult importPlays(MultipartFile file, String deviceName) throws Exception {
        if (file.getOriginalFilename().equalsIgnoreCase("mm.db")) {
            Device device = deviceService.getOrInsert(deviceName);
            File temporaryDatabase = File.createTempFile("mediamonkey", ".db");
            FileUtils.copyInputStreamToFile(file.getInputStream(), temporaryDatabase);

            logger.debug(String.format("Temporary database location: %s", temporaryDatabase.getAbsolutePath()));

            temporaryDatabase.deleteOnExit();
            return importPlaysFromDb(temporaryDatabase.getAbsolutePath(), device);
        } else {
            throw new Exception("Imported file does not have the expected filename from a MediaMonkey database 'mm.db'.");
        }
    }
}
