package music;

import music.endpoint.AdminEndpoint;
import music.model.Track;
import music.service.TrackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;

@Component
public class StartupProcessor implements CommandLineRunner {

    Logger logger = LoggerFactory.getLogger(StartupProcessor.class);

    @Autowired
    private AdminEndpoint adminEndpoint;

    @Autowired
    private TrackService trackService;

    @Override
    public void run(String... args) throws Exception {
        purgeDeletedTracks();
        adminEndpoint.syncTracksToDb();
    }

    /**
     * Find all tracks marked deleted in the database and delete those tracks from the file system.
     */
    private void purgeDeletedTracks() throws IOException {
        logger.info("Purging deleted files from disk.");
        List<Track> allTracks = trackService.listAll();
        long deletedCount = 0;
        for(Track track : allTracks){
            if(track.getDeletedInd()){
                trackService.permanentlyDelete(track);
                deletedCount++;
            }
        }
        logger.info(String.format("Deleted %s tracks from the file system.", deletedCount));
    }
}