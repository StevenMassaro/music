package music.service;

import music.exception.RatingRangeException;
import music.model.*;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
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
public class TrackServiceIT {

    @Autowired
    private TrackService trackService;

    @Autowired
    private DeviceService deviceService;

    @Autowired
    private UpdateService updateService;

	@Autowired
	private SmartPlaylistService smartPlaylistService;

    private File tempFile;

    @Before
    public void before() throws IOException {
        tempFile = File.createTempFile("111", ".flac");
        FileUtils.copyFile(ResourceUtils.getFile("classpath:1.flac"), tempFile);
        tempFile.deleteOnExit();
    }

    @After
    public void after() {
        FileUtils.deleteQuietly(tempFile);
    }

    @Test
    public void addingTracks() throws IOException {
        DeferredTrack track = track(tempFile.getName());
        trackService.upsertTracks(Collections.singletonList(track), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

        List<Track> list = trackService.list();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));
    }

	@Test
	public void listingTracksByAlbum() throws IOException {
		DeferredTrack track = track(tempFile.getName());
		trackService.upsertTracks(Collections.singletonList(track), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

		List<Track> list = trackService.listByAlbum("album", track.getArtist(), track.getDisc_no());
		assertEquals(1, list.size());
		doTrackAssertions(false, track, list.get(0));

		list = trackService.listByAlbum("otherone", track.getArtist(), track.getDisc_no());
		assertEquals(0, list.size());
	}

    @Test
    public void updatingTrack() throws IOException {
        SyncResult syncResult = new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>());

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

        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        Track track = trackService.list().get(0);

        assertEquals(0, track.getPlays());

        trackService.markListened(track.getId(), device.getId());

        track = trackService.list().get(0);
        assertEquals(1, track.getPlays());

        List<Date> historicalDates = trackService.listHistoricalDates();
        assertEquals(1, historicalDates.size());
        assertTrue(DateUtils.isSameDay(new Date(), historicalDates.get(0)));

        List<Track> playedTracks = trackService.listPlaysByDate(historicalDates.get(0));
        assertEquals(1, playedTracks.size());
        doTrackAssertions(false, track, playedTracks.get(0));
    }

    @Test
	public void skippedTrack() throws Exception {
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		Track track = trackService.list().get(0);

		assertEquals(0, track.getSkips());

		trackService.markSkipped(track.getId(), device.getId(), 1.0);

		track = trackService.list().get(0);
		assertEquals(1, track.getSkips());
	}

	@Test
	public void skippedTrack_ExceptionThrownWhenSecondsPlayedEqualsTrackDuration() {
		Device device = deviceService.getOrInsert("devname");

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
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

		trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		Track track = trackService.list().get(0);

		assertEquals(0, track.getSkips());

		trackService.markSkipped(track.getId(), device.getId(), null);

		track = trackService.list().get(0);
		assertEquals(1, track.getSkips());
	}

    @Test
    public void deletedTrack() throws IOException {
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        Track track = trackService.list().get(0);

        assertFalse(track.getDeletedInd());

        trackService.markDeleted(track.getId());

        track = trackService.get(track.getId());
        assertTrue(track.getDeletedInd());

        track = trackService.getByLocation(track.getLocation());
        assertTrue(track.getDeletedInd());

        List<Track> list = trackService.list();
        assertTrue(list.isEmpty());

        list = trackService.listAll();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));
    }

    @Test
    public void ratingTrack() throws RatingRangeException {
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        Track track = trackService.list().get(0);

        assertNull(track.getRating());

        byte rating = 3;
        trackService.setRating(track.getId(), rating);

        track = trackService.get(track.getId());
        assertEquals(rating, (byte)track.getRating());
    }

    @Test
    public void testListingTrackWithUpdates(){
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
        Track track = trackService.list().get(0);

        String field = ModifyableTags.ALBUM.getPropertyName();
        String newVal = "fart";

        updateService.queueTrackUpdate(track.getId(), field, newVal);

        track = trackService.list().get(0);
        assertEquals(newVal, track.getAlbum());
    }

    @Test
    public void testListingTrackWithTwoUpdates(){
        trackService.upsertTracks(Collections.singletonList(track(tempFile.getName())), new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
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
		trackService.upsertTracks(fauxtracks, new SyncResult(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));

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
}
