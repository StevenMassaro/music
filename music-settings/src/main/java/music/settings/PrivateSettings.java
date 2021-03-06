package music.settings;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PrivateSettings {

  @Value("${music.file.source}")
  private String localMusicFileLocation;

  @Value("${music.acceptable.file.extensions}")
  private String acceptableExtensions;

  @Value("${music.ffmpeg.path}")
  private String ffmpegPath;

  @Value("${music.track.name.pattern}")
  private String trackNamePattern;

  public String getLocalMusicFileLocation() {
    return localMusicFileLocation;
  }

  public String getAcceptableExtensions() {
    return acceptableExtensions;
  }

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public String getTrackNamePattern() {
		return trackNamePattern;
	}

}
