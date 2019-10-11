package music.service;

import music.model.Track;
import music.model.TrackUpdate;
import org.jaudiotagger.tag.FieldKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

import static music.helper.BuilderKt.track;
import static org.junit.Assert.assertEquals;

public class UpdateServiceIT extends IntegrationTestBase {

    @Autowired
    private UpdateService updateService;

    @Autowired
    private TrackService trackService;

    @Test
    public void testUpdateTrack(){
        trackService.upsertTracks(Collections.singletonList(track()));
        Track track = trackService.list().get(0);

        String field = FieldKey.ALBUM.toString();
        String newVal = "fart";

        updateService.queueTrackUpdate(track.getId(), field, newVal);

        List<TrackUpdate> updates = updateService.listById(track.getId());
        assertEquals(1, updates.size());
        assertEquals(field, updates.get(0).getField());
        assertEquals(newVal, updates.get(0).getNewValue());
    }
}
