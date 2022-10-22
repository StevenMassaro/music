package music.endpoint;

import org.springframework.beans.factory.annotation.Value;

public abstract class AbstractEndpoint {

	/**
	 * The path where the music files are stored.
	 */
	@Value("${local.music.file.location}")
	public String localMusicFileLocation;
}
