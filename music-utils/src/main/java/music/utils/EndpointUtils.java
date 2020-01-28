package music.utils;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;

public class EndpointUtils {

  public static ResponseEntity<Resource> responseEntity(String filename, String contentType, byte[] file){
    return responseEntity(filename, contentType, new ByteArrayResource(file));
  }

  public static ResponseEntity<Resource> responseEntity(String filename, String contentType, Resource file){
    return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline" + (!StringUtils.isEmpty(filename) ? "; filename=\"" + filename + "\"" : ""))
            .header(HttpHeaders.CONTENT_TYPE, contentType)
            .body(file);
  }
}
