package music;

import music.endpoint.AdminEndpoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class SyncProcessor implements CommandLineRunner {

    @Autowired
    private AdminEndpoint adminEndpoint;

    @Override
    public void run(String... args) throws Exception {
        adminEndpoint.syncTracksToDb();
    }
}