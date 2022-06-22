package music.endpoint;

import lombok.extern.log4j.Log4j2;
import music.model.Library;
import music.model.SyncResult;
import music.model.Track;
import music.model.TrackUpdate;
import music.repository.ILibraryRepository;
import music.service.FileService;
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

import java.io.File;
import java.io.IOException;
import java.util.*;

@RestController()
@RequestMapping("/admin")
@Log4j2
public class AdminEndpoint {
	private final TrackService trackService;
	private final SyncService syncService;
	private final UpdateService updateService;
	private final FileService fileService;
	private final ILibraryRepository libraryRepository;

    @Autowired
	public AdminEndpoint(TrackService trackService, SyncService syncService, UpdateService updateService, FileService fileService, ILibraryRepository libraryRepository) {
		this.trackService = trackService;
		this.syncService = syncService;
		this.updateService = updateService;
		this.fileService = fileService;
		this.libraryRepository = libraryRepository;
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
	 * Permanently delete a track, and transfer the deleted tracks plays, skips and rating to
	 * another track that will be preserved.
	 * @param idToDelete the ID of the track to delete
	 * @param existingId the ID of the track that will receive all of the metadata from the deleted track.
	 *                   This track will not be deleted.
	 */
	@DeleteMapping("/purge/{idToDelete}/into/{existingId}")
	public List<Track> purgeInto(@PathVariable long idToDelete, @PathVariable long existingId) {
		trackService.copyMetadata(idToDelete, existingId);
		return purgeDeletedTracks(Collections.singletonList(idToDelete));
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

	/**
	 * Generate a list of files which exist on disk but which are not in the database. These files are invisible to
	 * the application and could possibly be deleted.
	 */
	@GetMapping("orphanedFiles")
	public List<File> listOrphanedFiles() {
		List<Library> libraries = libraryRepository.findAll();
		List<File> orphanedFiles = new ArrayList<>();
		for (Library library : libraries) {
			log.debug("Looking for orphaned files in library {}", library.getName());
			Collection<File> files = fileService.listAllFiles(library);
			List<Track> tracks = trackService.list(library.getId());
			for (File file : files) {
				log.trace("Checking if file {} is orphaned", file.getAbsolutePath());
				// this is probably not the best way to find if there are any matches, doing an exact check on path would be better
				if (tracks.stream().noneMatch(track -> file.getAbsolutePath().contains(track.getLocation()))){
					orphanedFiles.add(file);
				}
			}
		}
		log.debug("Orphaned files {}", orphanedFiles);
		return orphanedFiles;
	}
}
