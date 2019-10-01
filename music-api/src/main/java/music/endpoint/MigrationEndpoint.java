package music.endpoint;

import music.model.PlayImportResult;
import music.model.PlayMigrationResult;
import music.service.MigrationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/migration")
public class MigrationEndpoint {

    private final MigrationService migrationService;

    @Autowired
    public MigrationEndpoint(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/plays")
    public PlayMigrationResult importPlays(@RequestParam("file") MultipartFile file,
                                           @RequestParam String deviceName) throws Exception {
        return migrationService.importPlays(file, deviceName);
    }
}
