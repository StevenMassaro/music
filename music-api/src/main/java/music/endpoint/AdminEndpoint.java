package music.endpoint;

import music.model.DeferredTrack;
import music.model.SyncResult;
import music.model.Track;
import music.service.MetadataService;
import music.service.SyncService;
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

	private final TrackService trackService;

	private final SyncService syncService;

    @Autowired
	public AdminEndpoint(TrackService trackService, SyncService syncService) {
		this.trackService = trackService;
		this.syncService = syncService;
	}

    @PostMapping("/dbSync")
    public SyncResult syncTracksToDb(@RequestParam(defaultValue = "false") boolean forceUpdates) throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
		return syncService.syncTracksToDb(forceUpdates);
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
}
