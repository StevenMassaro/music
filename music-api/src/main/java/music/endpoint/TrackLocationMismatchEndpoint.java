package music.endpoint;

import lombok.extern.log4j.Log4j2;
import music.model.Library;
import music.model.MismatchedTrackLocation;
import music.model.Track;
import music.model.TrackLocationMismatchResponseWrapper;
import music.service.FileService;
import music.service.MetadataService;
import music.service.TrackService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;

/**
 * This endpoint exposes tools for moving tracks to the correct location, as determined by
 * {@link MetadataService#generateLocationFromFilePath(File, Library)}. In previous versions, applying metadata changes
 * did not cause the file to move location, even if it should move. That issue can be resolved with these tools.
 */
@RestController
@RequestMapping("/track-location-mismatch")
@Log4j2
public class TrackLocationMismatchEndpoint extends AbstractEndpoint {

	private final TrackService trackService;
	private final FileService fileService;
	private final MetadataService metadataService;

	public TrackLocationMismatchEndpoint(TrackService trackService, FileService fileService, MetadataService metadataService) {
		this.trackService = trackService;
		this.fileService = fileService;
		this.metadataService = metadataService;
	}

	@GetMapping
	public TrackLocationMismatchResponseWrapper<List<MismatchedTrackLocation>> list() throws Exception {
		List<Track> tracks = trackService.list();
		List<MismatchedTrackLocation> mismatchedTracks = new ArrayList<>();
		Map<Track, Exception> errors = new LinkedHashMap<>();
		Set<String> seenNewLocations = new HashSet<>();
		for (Track track : tracks) {
			try {
				String currentLocation = track.getLibraryPath();
				String intendedLocation = fileService.generateFilename(track);
				if (!currentLocation.equals(intendedLocation)) {
					if (seenNewLocations.contains(intendedLocation)) {
						errors.put(track, new Exception("This track is being skipped because another track with the same correct location is already in the list. It would overwrite another file if we tried to move it. Perhaps the track name pattern for this library needs to be adjusted to introduce more uniqueness into the filenames."));
					} else {
						mismatchedTracks.add(new MismatchedTrackLocation(track.getId(), currentLocation, intendedLocation));
						seenNewLocations.add(intendedLocation);
					}
				} else {
					log.trace("Track {} is in the correct location", track.getId());
				}
			} catch (Exception e) {
				log.warn("Failed to determine whether track {} has a mismatched location", track.getId(), e);
				errors.put(track, e);
			}
		}
		if (!errors.isEmpty()) {
			log.error("Failed to determine whether {} tracks had mismatched locations.", errors);
		}
		return new TrackLocationMismatchResponseWrapper<>(mismatchedTracks, errors);
	}

	@PostMapping
	public TrackLocationMismatchResponseWrapper<List<Track>> move() throws Exception {
		List<MismatchedTrackLocation> list = list().getData();
		List<Track> moved = new ArrayList<>(list.size());
		Map<Track, Exception> errors = new LinkedHashMap<>();
		for (int i = 0; i < list.size(); i++) {
			log.debug("Processing mismatched track {}/{}", i, list.size());
			MismatchedTrackLocation mismatchedTrackLocation = list.get(i);
			String currentLocation = mismatchedTrackLocation.getCurrentLocation();
			long trackId = mismatchedTrackLocation.getTrackId();
			Track track = trackService.get(trackId);
			try {
				File currentFile = track.getFile(localMusicFileLocation);
				if (!currentFile.exists()) {
					throw new FileNotFoundException(currentLocation + " does not exist.");
				}
				File newTrack = fileService.moveTempTrack(currentFile, track);

				// update the location in database
				String newLocation = metadataService.generateLocationFromFilePath(newTrack, track.getLibrary());
				track.setLocation(newLocation);
				trackService.update(track);

				// recursively delete empty directories from original location
				File existingFileParent = currentFile.getParentFile();
				if (existingFileParent.isDirectory()) {
					File[] files = existingFileParent.listFiles();
					if (files != null && files.length == 0) {
						fileService.recursivelyDeleteEmptyDirectories(existingFileParent);
					}
				}

				moved.add(track);
			} catch (Exception e) {
				log.error("Failed to process track ID " + trackId, e);
				errors.put(track, e);
			}
		}
		if (!errors.isEmpty()) {
			log.error("Failed to process some tracks.");
		}
		return new TrackLocationMismatchResponseWrapper<>(moved, errors);
	}

}
