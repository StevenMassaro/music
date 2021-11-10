package music.endpoint;

import lombok.extern.log4j.Log4j2;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/admin")
@Log4j2
public class AdminEndpoint {
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

	@GetMapping("/purge")
	public List<Track> listPurgableTracks() {
    	return trackService.listPurgableTracks();
	}

    /**
     * Find all tracks marked deleted in the database and delete those tracks from the file system.
     */
    @DeleteMapping("/purge")
    public List<Track> purgeDeletedTracks(@RequestBody(required = false) List<Long> tracksToDelete) {
    	List<Track> deleted;
    	if (CollectionUtils.isEmpty(tracksToDelete)) {
			log.info("Purging all deleted files from disk.");
			List<Track> allTracks = trackService.listDeleted();
			deleted = doPurge(allTracks);
		} else {
    		log.info("Purging tracks {} from disk.", tracksToDelete);
    		List<Track> tracks = new ArrayList<>(tracksToDelete.size());
			for (Long id : tracksToDelete) {
				tracks.add(trackService.get(id));
			}
			deleted = doPurge(tracks);
		}
		log.info("Deleted {} tracks from the file system.", deleted.size());
		return deleted;
    }

    private List<Track> doPurge(List<Track> tracksToDelete) {
		List<Track> deleted = new ArrayList<>();
		if (tracksToDelete != null) {
			for (Track track : tracksToDelete) {
				if (track.getDeletedInd()) {
					trackService.permanentlyDelete(track);
					deleted.add(track);
				} else {
					log.warn("Track {} is not marked as deleted, but was attempted to be purged. No action taken.", track.getId());
				}
			}
		}
		return deleted;
	}

    @PostMapping("/update")
    public void applyUpdatesToSongs(){
    	log.info("Applying updates to disk");
        Map<Long, List<TrackUpdate>> updates = updateService.applyUpdatesToDisk(trackService);
        log.info("Finished applying {} updates to disk", updates.size());
    }

    @GetMapping("/update/count")
	public long countUpdates(){
    	return updateService.count();
	}
}
