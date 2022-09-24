package music.endpoint;

import music.model.MismatchedTrackLocation;
import music.model.ModifyableTags;
import music.model.Track;
import music.service.IntegrationTestBase;
import music.service.TrackService;
import music.service.UpdateService;
import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class TrackLocationMismatchEndpointIT extends IntegrationTestBase {

	@Autowired
	private TrackLocationMismatchEndpoint trackLocationMismatchEndpoint;

	@Autowired
	private TrackService trackService;

	@Autowired
	private UpdateService updateService;

	@Test
	public void testList() throws Exception {
		assertTrue(trackLocationMismatchEndpoint.list().isEmpty());

		Track track = trackService.uploadNewTrack(new FileInputStream(tempFile), FilenameUtils.getExtension(tempFile.getName()), seededMusicLibrary.getId());

		assertTrue(trackLocationMismatchEndpoint.list().isEmpty());

		String newAlbumName = "randomnewalbum";
		updateService.queueTrackUpdate(track.getId(), ModifyableTags.ALBUM.getPropertyName(), newAlbumName);
		updateService.applyUpdatesToDisk(trackService);

		List<MismatchedTrackLocation> list = trackLocationMismatchEndpoint.list();
		assertEquals(1, list.size());
		list.get(0).getCorrectLocation().contains(newAlbumName);
	}
}
