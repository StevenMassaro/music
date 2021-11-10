package music.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public abstract class AbstractService {

	/**
	 * The path where the music files are stored.
	 */
	@Value("${local.music.file.location}")
	public String localMusicFileLocation;
}
