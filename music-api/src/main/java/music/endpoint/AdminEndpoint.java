package music.endpoint;

import music.model.SyncResult;
import music.model.Track;
import music.service.MetadataService;
import music.service.TrackService;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController()
@RequestMapping("/admin")
public class AdminEndpoint {

    private Logger logger = LoggerFactory.getLogger(AdminEndpoint.class);

    private final MetadataService metadataService;

    private final TrackService trackService;

    @Autowired
    public AdminEndpoint(MetadataService metadataService, TrackService trackService) {
        this.metadataService = metadataService;
        this.trackService = trackService;
    }

    @PostMapping("/dbSync")
    public SyncResult syncTracksToDb() throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
        logger.info("Begin database sync");
        SyncResult syncResult = new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        List<Track> files = metadataService.getTracks();
        trackService.upsertTracks(files, syncResult);
        trackService.deleteOrphanedTracksMetadata(files, syncResult);
        logger.info("Finished database sync");
        return syncResult;
    }

    /**
     * Find all tracks marked deleted in the database and delete those tracks from the file system.
     */
    @DeleteMapping("/purge")
    public List<Track> purgeDeletedTracks() throws IOException {
        logger.info("Purging deleted files from disk.");
        List<Track> allTracks = trackService.listAll();
        List<Track> deleted = new ArrayList<>();
        if (allTracks != null) {
            for (Track track : allTracks) {
                if (track.getDeletedInd()) {
                    trackService.permanentlyDelete(track);
                    deleted.add(track);
                }
            }
        }
        logger.info(String.format("Deleted %s tracks from the file system.", deleted.size()));
        return deleted;
    }
}
