package music.monitoring;

import lombok.extern.log4j.Log4j2;
import music.model.Library;
import music.repository.ILibraryRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.util.List;

@Log4j2
@Configuration
@ConditionalOnProperty(value="monitoring-folder")
public class MonitoringConfig {

	@Value("${monitoring-folder}")
	private String folderPath;

	private final ILibraryRepository libraryRepository;

	public MonitoringConfig(ILibraryRepository libraryRepository) {
		this.libraryRepository = libraryRepository;
	}

	@Bean
	public WatchService watchService() {
		log.debug("Monitoring folder: {}", folderPath);
		WatchService watchService = null;
		try {
			watchService = FileSystems.getDefault().newWatchService();
			List<Library> libraries = libraryRepository.findAll();
			for (Library library : libraries) {
				File libraryFolder = new File(folderPath, library.getName());
				if (!libraryFolder.exists()) {
					boolean mkdir = libraryFolder.mkdir();
					if (!mkdir) {
						throw new IOException("Failed to create library folder " + library.getName());
					}
				}
				Path path = Paths.get(folderPath, library.getName());

				if (!Files.isDirectory(path)) {
					throw new RuntimeException("Incorrect monitoring folder: " + path);
				}

				path.register(
					watchService,
					StandardWatchEventKinds.ENTRY_CREATE
				);
			}
		} catch (IOException e) {
			log.error("Exception for watch service creation:", e);
		}
		return watchService;
	}
}