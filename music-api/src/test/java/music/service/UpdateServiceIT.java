package music.service;

import music.model.ModifyableTags;
import music.model.Track;
import music.model.TrackUpdate;
import music.repository.IUpdateRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UpdateServiceIT extends IntegrationTestBase {

    @Autowired
    private UpdateService updateService;

    @Autowired
	private IUpdateRepository updateRepository;

    @Test(expected = IllegalArgumentException.class)
    public void testNonAllowedUpdate(){
        updateService.queueTrackUpdate(1, "akskjahd", "asjkld");
    }

    @Test
	public void testRepository() {
		Track track = insertTempFile();

		TrackUpdate tu = new TrackUpdate(null, track.getId(), ModifyableTags.ALBUM.getPropertyName(), "asjdl", 1L);
		updateRepository.save(tu);

		TrackUpdate loadedTu = updateRepository.findBySongIdAndField(track.getId(), ModifyableTags.ALBUM.getPropertyName()).get();
		assertNotNull(loadedTu.getId());
		assertEquals(tu.getField(), loadedTu.getField());
		assertEquals(tu.getUpdateType(), loadedTu.getUpdateType());
		assertEquals(tu.getNewValue(), loadedTu.getNewValue());
		assertEquals(tu.getSongId(), loadedTu.getSongId());
	}
}
