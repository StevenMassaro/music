package music.service;

import music.model.PlayImportResult;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

public interface MigrationService {

    /**
     * Import plays from the input stream, using the specified device name.
     * @param file
     * @param deviceName
     */
    PlayImportResult importPlays(MultipartFile file, String deviceName) throws Exception;
}
