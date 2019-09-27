package music.service;

import music.model.Device;
import music.model.Track;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
public class TrackServiceIT {

    @Autowired
    private TrackService trackService;

    @Autowired
    private DeviceService deviceService;

    @Test
    public void addingTracks() throws IOException {
        Track track = track();
        trackService.upsertTracks(Collections.singletonList(track));

        List<Track> list = trackService.list();
        assertEquals(1, list.size());
        doTrackAssertions(false, track, list.get(0));
    }

    @Test
    public void listenedTrack() {
        // first create a device with which to associate the plays
        Device device = deviceService.getOrInsert("devname");

        trackService.upsertTracks(Collections.singletonList(track()));
        Track track = trackService.list().get(0);

        assertEquals(0, track.getPlays());

        trackService.markListened(track.getId(), device.getId());

        track = trackService.list().get(0);
        assertEquals(1, track.getPlays());
    }

    @Test
    public void deletedTrack() throws IOException {
        trackService.upsertTracks(Collections.singletonList(track()));
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

    private Track track() {
        Track track = new Track();
        track.setAlbum("album");
        track.setAlbumArtist("aartist");
        track.setArtist("artist");
        track.setComment("comment");
        track.setDateCreated(new Date());
        track.setDiscNumber(1L);
        track.setGenre("genre");
        track.setLocation("C:/dev/1.flac");
        track.setTitle("title");
        track.setTrackNumber(3L);
        track.setYear("1998");
        track.setDeletedInd(false);
        track.setFileLastModifiedDate(new Date());
        track.setHash("1234abc");
        return track;
    }

    private void doTrackAssertions(boolean assertId, Track baseline, Track comparison) throws IOException {
        assertEquals(baseline.getAlbum(), comparison.getAlbum());
        assertEquals(baseline.getAlbumArtist(), comparison.getAlbumArtist());
        assertEquals(baseline.getArtist(), comparison.getArtist());
        assertEquals(baseline.getComment(), comparison.getComment());
//        assertEquals(track1.getDateCreated(), track2.getDateCreated());
        assertNotNull(comparison.getDateCreated());
        assertEquals(baseline.getDiscNumber(), comparison.getDiscNumber());
        assertEquals(baseline.getGenre(), comparison.getGenre());
        assertEquals(baseline.getLocation(), comparison.getLocation());
        assertEquals(baseline.getTitle(), comparison.getTitle());
        assertEquals(baseline.getTrackNumber(), comparison.getTrackNumber());
        assertEquals(baseline.getYear(), comparison.getYear());
        assertEquals(baseline.getDeletedInd(), comparison.getDeletedInd());
        assertEquals(baseline.getFileLastModifiedDate(), comparison.getFileLastModifiedDate());
        assertEquals(baseline.getHash(), comparison.getHash());
        if (assertId) {
            assertEquals(baseline.getId(), comparison.getId());
        }
    }
}
