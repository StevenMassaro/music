package music.endpoint;

import lombok.extern.log4j.Log4j2;
import music.model.MismatchedTrackLocation;
import music.model.Track;
import music.service.FileService;
import music.service.TrackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/track-location-mismatch")
@Log4j2
public class TrackLocationMismatchEndpoint {

	private final TrackService trackService;
	private final FileService fileService;

	public TrackLocationMismatchEndpoint(TrackService trackService, FileService fileService) {
		this.trackService = trackService;
		this.fileService = fileService;
	}

	@GetMapping
	public List<MismatchedTrackLocation> list() {
		List<Track> tracks = trackService.list();
		List<MismatchedTrackLocation> mismatchedTracks = new ArrayList<>();
		for (Track track : tracks) {
			String currentLocation = track.getLocation();
			String intendedLocation = fileService.generateFilename(track);
			if (!currentLocation.equals(intendedLocation)) {
				mismatchedTracks.add(new MismatchedTrackLocation(track.getId(), currentLocation, intendedLocation));
			} else {
				log.trace("Track {} is in the correct location", track.getId());
			}
		}
		return mismatchedTracks;
	}
}
