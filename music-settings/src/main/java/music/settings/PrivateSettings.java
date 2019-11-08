package music.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrivateSettings {

  @Value("${music.file.source}")
  private String localMusicFileLocation;

  @Value("${music.acceptable.file.extensions}")
  private String acceptableExtensions;

  public String getLocalMusicFileLocation() {
    return localMusicFileLocation;
  }

  public String getAcceptableExtensions() {
    return acceptableExtensions;
  }
}
