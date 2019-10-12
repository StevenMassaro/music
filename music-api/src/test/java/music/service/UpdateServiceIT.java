package music.service;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class UpdateServiceIT extends IntegrationTestBase {

    @Autowired
    private UpdateService updateService;

    @Test(expected = IllegalArgumentException.class)
    public void testNonAllowedUpdate(){
        updateService.queueTrackUpdate(1, "akskjahd", "asjkld");
    }
}
