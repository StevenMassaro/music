package music.service;

import music.exception.RatingRangeException;
import music.mapper.PlayMapper;
import music.model.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.multipart.MultipartFile;
import org.testcontainers.shaded.org.apache.commons.lang.time.DateUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static music.helper.BuilderKt.doTrackAssertions;
import static music.helper.BuilderKt.track;
import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
@ContextConfiguration(initializers = TrackServiceIT.MusicPropertiesInitializer.class)
public class TrackServiceIT {

	public static final Library seededMusicLibrary = new Library(1, "Music", "Music");

    @Autowired
    private TrackService trackService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UpdateService updateService;

	@Autowired
	private SmartPlaylistService smartPlaylistService;

	@Autowired
	private PlayMapper playMapper;

	@Autowired
	private PlaylistService playlistService;

    private File tempFile;
    private File tempFile2;

	public static class MusicPropertiesInitializer
		implements ApplicationContextInitializer<ConfigurableApplicationContext> {

		@Override
		public void initialize(ConfigurableApplicationContext applicationContext) {
			TestPropertySourceUtils.addInlinedPropertiesToEnvironment(applicationContext,
				"music.file.source=" + System.getProperty("java.io.tmpdir").replace("\\", "\\\\"));
		}
	}
    @Before
    public void before() throws IOException {
    	tempFile = createTempFile("sample1.flac");
    	tempFile2 = createTempFile("sample2.flac");
    }

    public static File createTempFile(String filename) throws IOException {
		String extension = "." + FilenameUtils.getExtension(filename);
		File dir = new File(System.getProperty("java.io.tmpdir"), seededMusicLibrary.getSubfolder());
		dir.mkdirs();
		File f = File.createTempFile(filename.replace(extension, ""), extension, dir);
		FileUtils.copyFile(ResourceUtils.getFile("classpath:" + filename), f);
		f.deleteOnExit();
		return f;
	}

    @After
    public void after() {
        FileUtils.deleteQuietly(tempFile);
    }

    @Test
    public void addingTracks() throws IOException {
        DeferredTrack track = track(tempFile.getName());
        trackService.upsertTracks(Collections.singletonList(track), new SyncResult());

        List<Track> list = trackService.list();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));
    }

    @Test
	public void replacingTrack() throws Exception {
		DeferredTrack track = track(tempFile.getName());
		trackService.upsertTracks(Collections.singletonList(track), new SyncResult());

		List<Track> list = trackService.list();
		assertEquals(1, list.size());
		doTrackAssertions(false, track, list.get(0));

		Device device = deviceService.getOrInsert("devname");
		trackService.markListened(list.get(0).getId(), device.getId());
		trackService.markListened(list.get(0).getId(), device.getId());
		playMapper.upsertPlayCount(list.get(0).getId(), device.getId(), 7, true);
		trackService.markSkipped(list.get(0).getId(), device.getId(), null);

		// get updates on play counts and skip counts
		list = trackService.list();

		MultipartFile replacementTrack = new MockMultipartFile(tempFile2.getName(), tempFile2.getName(), "application/flac", FileUtils.readFileToByteArray(tempFile2));
		Track newTrack = trackService.replaceExistingTrack(replacementTrack, list.get(0).getId());
		assertNotEquals(list.get(0).getHash(), newTrack.getHash());
		assertEquals(list.get(0).getPlays(), newTrack.getPlays());
		assertEquals(list.get(0).getSkips(), newTrack.getSkips());
		assertEquals(1, trackService.list().size());
		assertEquals(0, trackService.listDeleted().size());
		assertFalse(newTrack.getLocation().contains(seededMusicLibrary.getSubfolder()));
	}

	@Test
	public void listingTracksByAlbum() throws IOException {
		DeferredTrack track = track(tempFile.getName());
		trackService.upsertTracks(Collections.singletonList(track), new SyncResult());

		List<Track> list = trackService.listByAlbum("album", track.getArtist(), track.getDisc_no());
		assertEquals(1, list.size());
		doTrackAssertions(false, track, list.get(0));

		list = trackService.listByAlbum("otherone", track.getArtist(), track.getDisc_no());
		assertEquals(0, list.size());
	}

    @Test
    public void updatingTrack() throws IOException {
        SyncResult syncResult = new SyncResult();

        DeferredTrack track = track(tempFile.getName());
        trackService.upsertTracks(Collections.singletonList(track), syncResult);

        List<Track> list = trackService.list();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));

        // not forcing updates, and the date is the same, so it shouldn't update
        long bitrate = 123;
        track.setBitrate(bitrate);
        trackService.upsertTracks(Collections.singletonList(track), syncResult);
        list = trackService.list();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));

        // now force the update
        trackService.upsertTracks(Collections.singletonList(track), syncResult, true);
        list = trackService.list();
        assertEquals(1, list.size());
        assertEquals(bitrate, list.get(0).getBitrate());
        assertNotNull(list.get(0).getDateUpdated());
    }

    @Test
    public void listenedTrack() throws IOException {
        // first create a device with which to associate the plays
        Device device = deviceService.getOrInsert("devname");

        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
        Track track = trackService.list().get(0);

        assertEquals(0, track.getPlays());
        assertNull(track.getLastPlayedDate());

        trackService.markListened(track.getId(), device.getId());

        track = trackService.list().get(0);
        assertEquals(1, track.getPlays());
        assertNotNull(track.getLastPlayedDate());
        assertTrue(org.apache.commons.lang3.time.DateUtils.isSameDay(track.getLastPlayedDate(), new Date()));

        List<Date> historicalDates = trackService.listHistoricalDates();
        assertEquals(1, historicalDates.size());
        assertTrue(DateUtils.isSameDay(new Date(), historicalDates.get(0)));

        List<Track> playedTracks = trackService.listPlaysByDate(historicalDates.get(0));
        assertEquals(1, playedTracks.size());
        doTrackAssertions(false, track, playedTracks.get(0));
    }

	/**
	 * Previously, the TrackMapper was doing left outer joins to the plays and skips tables. This was causing the counts
	 * to be incorrect in some instances. I rewrote the sql to use subselects, and this test confirms that fix.
	 * @throws Exception
	 */
	@Test
	public void listenedTrack_PlayCountsCorrect() throws Exception {
		// first create a device with which to associate the plays
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
		Track track = trackService.list().get(0);

		assertEquals(0, track.getPlays());

		trackService.markListened(track.getId(), device.getId());
		trackService.markListened(track.getId(), device.getId());
		trackService.markSkipped(track.getId(), device.getId(), 1.0);

		track = trackService.list().get(0);
		assertEquals(2, track.getPlays());
		assertEquals(1, track.getSkips());
	}

    @Test
	public void skippedTrack() throws Exception {
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
		Track track = trackService.list().get(0);

		assertEquals(0, track.getSkips());

		trackService.markSkipped(track.getId(), device.getId(), 1.0);

		track = trackService.list().get(0);
		assertEquals(1, track.getSkips());
	}

	@Test
	public void skippedTrack_ExceptionThrownWhenSecondsPlayedEqualsTrackDuration() {
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
		Track track = trackService.list().get(0);

		assertEquals(0, track.getSkips());

		Track finalTrack = track;
		assertThrows(Exception.class, () -> trackService.markSkipped(finalTrack.getId(), device.getId(), (double) finalTrack.getDuration()));

		track = trackService.list().get(0);
		assertEquals(0, track.getSkips());
	}

	@Test
	public void skippedTrack_NullSecondsPlayed() throws Exception {
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
		Track track = trackService.list().get(0);

		assertEquals(0, track.getSkips());

		trackService.markSkipped(track.getId(), device.getId(), null);

		track = trackService.list().get(0);
		assertEquals(1, track.getSkips());
	}

    @Test
    public void deletedTrack() throws IOException {
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
        Track track = trackService.list().get(0);

        assertFalse(track.getDeletedInd());

        trackService.markDeleted(track.getId());

        track = trackService.get(track.getId());
        assertTrue(track.getDeletedInd());

        List<Track> list = trackService.list();
        assertTrue(list.isEmpty());

        list = trackService.listDeleted();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));
    }

    @Test
    public void ratingTrack() throws RatingRangeException {
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
        Track track = trackService.list().get(0);

        assertNull(track.getRating());

        byte rating = 3;
        trackService.setRating(track.getId(), rating);

        track = trackService.get(track.getId());
        assertEquals(rating, (byte)track.getRating());
    }

    @Test
    public void testListingTrackWithUpdates(){
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
        Track track = trackService.list().get(0);

        String field = ModifyableTags.ALBUM.getPropertyName();
        String newVal = "fart";

        updateService.queueTrackUpdate(track.getId(), field, newVal);

        track = trackService.list().get(0);
        assertEquals(newVal, track.getAlbum());
    }

    @Test
    public void testListingTrackWithTwoUpdates(){
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult());
        Track track = trackService.list().get(0);

        String field = ModifyableTags.ALBUM.getPropertyName();
        String newVal = "fart";

        updateService.queueTrackUpdate(track.getId(), field, newVal);
        updateService.queueTrackUpdate(track.getId(), field, newVal+newVal);

        track = trackService.list().get(0);
        assertEquals(newVal+newVal, track.getAlbum());
    }

	@Test
	public void testSmartPlaylist() throws IOException {
		List<DeferredTrack> fauxtracks = Collections.singletonList(track(tempFile.getName()));
		trackService.upsertTracks(fauxtracks, new SyncResult());

		String name = "test";
		String sql = "CAST(year AS int) > 1990";
		smartPlaylistService.insert(name, sql);
		List<SmartPlaylist> playlists = smartPlaylistService.list();

		List<Track> tracks = trackService.listWithSmartPlaylist(playlists.get(0).getId());
		assertEquals(1, tracks.size());
		doTrackAssertions(false, fauxtracks.get(0), tracks.get(0));

		name = "test2";
		sql = "CAST(year AS int) < 1990";
		smartPlaylistService.insert(name, sql);
		playlists = smartPlaylistService.list();

		tracks = trackService.listWithSmartPlaylist(playlists.get(1).getId());
		assertTrue(tracks.isEmpty());
	}

	@Test
	public void testListingTracksInPlaylist() {
		List<DeferredTrack> fauxtracks = new ArrayList<>(2);
		fauxtracks.add(track(tempFile.getName()));
		fauxtracks.add(track(tempFile2.getName()));
		trackService.upsertTracks(fauxtracks, new SyncResult());
		List<Track> tracks = trackService.list();
		assertEquals(2, tracks.size());

		Playlist playlist = playlistService.create("newone");
		playlistService.addTrack(playlist.getId(), tracks.get(0).getId());
		playlistService.addTrack(playlist.getId(), tracks.get(1).getId());

		List<Track> playlistTracks = trackService.listWithPlaylist(playlist.getId());
		assertEquals(2, playlistTracks.size());
		assertEquals(tracks.get(0).getId(), playlistTracks.get(0).getId());
		assertEquals(tracks.get(1).getId(), playlistTracks.get(1).getId());
		assertTrue(playlist.getTrackIds().get(0).getSequenceId() < playlist.getTrackIds().get(1).getSequenceId());

		// confirm that when deleting a track, it is also removed from the playlist
		trackService.permanentlyDelete(tracks.get(1));

		playlistTracks = trackService.listWithPlaylist(playlist.getId());
		assertEquals(1, playlistTracks.size());
		assertEquals(tracks.get(0).getId(), playlistTracks.get(0).getId());
	}
}
