package music.service;

import music.model.MigrationResult;
import org.springframework.web.multipart.MultipartFile;

public interface MigrationService {

    MigrationResult doImport(MultipartFile file, String deviceName, boolean importPlays, boolean importRatings) throws Exception;
}
