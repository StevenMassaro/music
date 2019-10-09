package music.endpoint;

import music.model.MigrationResult;
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

    private final String FALSE = "false";

    @Autowired
    public MigrationEndpoint(MigrationService migrationService) {
        this.migrationService = migrationService;
    }

    @PostMapping("/import")
    public MigrationResult doImport(@RequestParam("file") MultipartFile file,
                                    @RequestParam String deviceName,
                                    @RequestParam(defaultValue = FALSE) boolean importPlays,
                                    @RequestParam(defaultValue = FALSE) boolean importRatings) throws Exception {
        return migrationService.doImport(file, deviceName, importPlays, importRatings);
    }
}
