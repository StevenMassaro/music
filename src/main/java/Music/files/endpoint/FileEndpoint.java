package Music.files.endpoint;

import Music.files.service.FileService;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;

@RestController
public class FileEndpoint {

    @Autowired
    private FileService fileService;

    @GetMapping("/test")
    public String test() throws IOException {
        StringBuilder response = new StringBuilder();
        for (File file : fileService.listMusicFiles()) {
            response.append(file.getName());
            response.append(System.lineSeparator());
        }

        return response.toString();
    }
}
