package music.service;

import music.model.DeferredTrack;
import music.model.Playlist;
import music.model.SyncResult;
import music.model.Track;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static music.helper.BuilderKt.track;
import static music.service.TrackServiceIT.createTempFile;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class PlaylistServiceIT extends IntegrationTestBase {
	@Autowired
	private PlaylistService playlistService;
	@Autowired
	private TrackService trackService;

	private File tempFile;

	@Before
	public void before() throws IOException {
		tempFile = createTempFile("sample1.flac");
	}

	@Test
	public void testCrud() {
		assertTrue(playlistService.list().isEmpty());

		String name = "testplaylist";
		Playlist playlist = playlistService.create(name);
		assertNotNull(playlist.getId());
		assertEquals(name, playlist.getName());
		assertTrue(DateUtils.isSameDay(new Date(), playlist.getDateCreated()));
		assertTrue(playlist.getTrackIds().isEmpty());

		assertEquals(1, playlistService.list().size());

		DeferredTrack track = track(tempFile.getName());
		trackService.upsertTracks(Collections.singletonList(track), new SyncResult());
		List<Track> tracks = trackService.list();
		playlistService.addTrack(playlist.getId(), tracks.get(0).getId());

		Optional<Playlist> getPlaylist = playlistService.getById(playlist.getId());
		assertEquals(1, getPlaylist.get().getTrackIds().size());
		assertNotNull(getPlaylist.get().getTrackIds().get(0).getDateAdded());
		assertNotNull(getPlaylist.get().getTrackIds().get(0).getSequenceId());

		List<Playlist> list = playlistService.list();
		assertNotNull(list);
	}
}
