package music.service;

import music.model.PlayMigrationResult;
import org.springframework.web.multipart.MultipartFile;

public interface MigrationService {

    /**
     * Import plays from the input stream, using the specified device name.
     * @param file
     * @param deviceName
     */
    PlayMigrationResult importPlays(MultipartFile file, String deviceName) throws Exception;
}
