package music.endpoint;

import music.model.SyncResult;
import music.model.Track;
import music.model.TrackUpdate;
import music.service.SyncService;
import music.service.TrackService;
import music.service.UpdateService;
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
import java.util.Map;

@RestController()
@RequestMapping("/admin")
public class AdminEndpoint {

    private Logger logger = LoggerFactory.getLogger(AdminEndpoint.class);

	private final TrackService trackService;

	private final SyncService syncService;

	private final UpdateService updateService;

    @Autowired
	public AdminEndpoint(TrackService trackService, SyncService syncService, UpdateService updateService) {
		this.trackService = trackService;
		this.syncService = syncService;
		this.updateService = updateService;
	}

    @PostMapping("/dbSync")
    public SyncResult syncTracksToDb(@RequestParam(defaultValue = "false") boolean forceUpdates,
									 @RequestParam long libraryId) throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
		return syncService.syncTracksToDb(forceUpdates, libraryId);
	}

	@GetMapping("/purge/count")
	public long countPurgableTracks(){
		return trackService.countPurgableTracks();
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

    @PostMapping("/update")
    public void applyUpdatesToSongs(){
    	logger.info("Applying updates to disk");
        Map<Long, List<TrackUpdate>> updates = updateService.applyUpdatesToDisk(trackService);
        logger.info("Finished applying {} updates to disk", updates.size());
    }

    @GetMapping("/update/count")
	public long countUpdates(){
    	return updateService.count();
	}
}
