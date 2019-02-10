package Music.metadata.endpoint;

import Music.metadata.service.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class MetadataEndpoint {

    @Autowired
    private MetadataService metadataService;

    @GetMapping("testmeta")
    public String testMeta() throws IOException {
        return metadataService.getMetadataTest();
    }
}
