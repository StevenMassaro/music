package music.endpoint;

import music.model.Track;
import music.service.FileService;
import music.service.MetadataService;
import music.service.TrackService;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController()
@RequestMapping("/admin")
public class AdminEndpoint {

    private Logger logger = LoggerFactory.getLogger(AdminEndpoint.class);

    @Autowired
    private FileService fileService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private TrackService trackService;

    @GetMapping("/dbSync")
    public List<Track> syncTracksToDb() throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
        logger.info("Begin database sync");
        List<Track> files = metadataService.getTracks();
        trackService.upsertTracks(files);
        logger.info("Finished database sync");
        return trackService.list();
    }

    /**
     * Find all tracks marked deleted in the database and delete those tracks from the file system.
     */
    @DeleteMapping("/purge")
    public void purgeDeletedTracks() throws IOException {
        logger.info("Purging deleted files from disk.");
        List<Track> allTracks = trackService.listAll();
        long deletedCount = 0;
        if (allTracks != null) {
            for (Track track : allTracks) {
                if (track.getDeletedInd()) {
                    trackService.permanentlyDelete(track);
                    deletedCount++;
                }
            }
        }
        logger.info(String.format("Deleted %s tracks from the file system.", deletedCount));
    }
}
