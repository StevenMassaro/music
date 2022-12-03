package music.monitoring;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.log4j.Log4j2;
import music.model.Library;
import music.repository.ILibraryRepository;
import music.service.TrackService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.TimeUnit;

@Log4j2
@Service
@ConditionalOnProperty(value="monitoring-folder")
public class MonitoringServiceImpl {

	@Value("${monitoring-folder}")
	private String folderPath;

	private final WatchService watchService;
	private final TrackService trackService;
	private final ILibraryRepository libraryRepository;

	public MonitoringServiceImpl(WatchService watchService,
								 TrackService trackService,
								 ILibraryRepository libraryRepository) {
		this.watchService = watchService;
		this.trackService = trackService;
		this.libraryRepository = libraryRepository;
	}

	@PostConstruct
	public void launchMonitoring() {
		Thread t = new Thread(() -> {
			log.info("Started file system monitoring thread");
			try {
				WatchKey key;
				while ((key = watchService.take()) != null) {
					TimeUnit.SECONDS.sleep(3); // give the file system time to finish updating mod time and stuff
					for (WatchEvent<?> event : key.pollEvents()) {
						String filename = event.context().toString();
						Path directory = (Path)key.watchable();
						Path fullPath = directory.resolve(filename);
						// This is JUST the folder name, not the entire absolute path.
						String parentFolderName = FilenameUtils.getName(String.valueOf(fullPath.getParent()));

						log.debug("Event kind: {}; File affected: {}", event.kind(), filename);
						if (Files.isDirectory(Paths.get(folderPath, filename))) {
							log.warn("{} is a directory and directories are not traversed", filename);
						} else {
							try {
								log.info("Uploading {}", fullPath);
								Library library = libraryRepository.findByName(parentFolderName);
								File file = fullPath.toFile();
								trackService.uploadNewTrack(new FileInputStream(file), FilenameUtils.getExtension(filename), library.getId());
								log.info("Successfully uploaded {}", fullPath);
								FileUtils.moveFileToDirectory(file, Paths.get(folderPath, "uploaded", library.getName()).toFile(), true);
							} catch (Exception e) {
								log.error("Failed to upload {}", fullPath, e);
							}
						}
					}
					key.reset();
				}
			} catch (InterruptedException e) {
				log.warn("Interrupted exception for monitoring service", e);
			}
		});
		t.start();
	}

	@PreDestroy
	public void stopMonitoring() {
		log.info("Stopped file system monitoring thread");

		if (watchService != null) {
			try {
				watchService.close();
			} catch (IOException e) {
				log.error("Exception while closing the monitoring service", e);
			}
		}
	}
}