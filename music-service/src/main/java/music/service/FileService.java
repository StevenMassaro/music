package music.service;

import lombok.extern.log4j.Log4j2;
import music.model.DeferredTrack;
import music.model.Library;
import music.model.Track;
import music.model.TrackNamePattern;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collection;
import java.util.Objects;

@Service
@Log4j2
public class FileService extends AbstractService {

	@Value("${music.acceptable.file.extensions:}")
	private String acceptableExtensions;

	@Value("${music.track.name.pattern:}")
	private String trackNamePattern;


    public Collection<File> listMusicFiles(Library library) {
        SuffixFileFilter caseInsensitiveExtensionFilter = new SuffixFileFilter(acceptableExtensions.split(","), IOCase.INSENSITIVE);
        return FileUtils.listFiles(new File(Objects.requireNonNull(localMusicFileLocation), library.getSubfolder()), caseInsensitiveExtensionFilter, TrueFileFilter.INSTANCE);
    }

	/**
	 * List all files in the libraries directory, including files which do not match the acceptable extensions list.
	 */
	public Collection<File> listAllFiles(Library library) {
    	return FileUtils.listFiles(new File(Objects.requireNonNull(localMusicFileLocation), library.getSubfolder()), null ,true);
	}

	/**
	 * Write the supplied file to the temporary directory.
	 */
    public File writeTempTrack(MultipartFile file) throws IOException {
    	// copy file to temp file
    	File tempTrack = File.createTempFile(RandomStringUtils.randomAlphabetic(10), "." + FilenameUtils.getExtension(file.getOriginalFilename()));
    	FileUtils.copyInputStreamToFile(file.getInputStream(), tempTrack);

		return tempTrack;
	}

	/**
	 * Move the temporary track into a dynamically generated folder, determined by using the pattern specified in
	 * the application properties and replacing those placeholder values with the actual values from the file's ID3 tag.
	 */
	public File moveTempTrack(File tempTrack, DeferredTrack metadata) throws IOException {
		String newFullPath = generateFilename(metadata); // includes filename at end of path
		String newPath = FilenameUtils.getPath(newFullPath); // just the path, no filename
		String newFilename = FilenameUtils.getName(newFullPath);

		// first make the directories that the track will need, if they don't yet exist
		File folder = new File(Objects.requireNonNull(localMusicFileLocation), newPath);
		folder.mkdirs();

		// then copy the track into those directories
		File track = new File(folder, newFilename);
		FileUtils.copyFile(tempTrack, track);

		tempTrack.delete();

		return track;
	}

	/**
	 * Generate the filename (including folders) using the pattern specified in the application.properties, replacing
	 * the placeholder values with the values specified in the deferred track.
	 */
	public String generateFilename(DeferredTrack deferredTrack){
    	String pattern = deferredTrack.getLibrary().getSubfolder() + File.separator + trackNamePattern;
    	for(TrackNamePattern trackNamePattern : TrackNamePattern.values()){
			Field field = ReflectionUtils.findField(deferredTrack.getClass(), trackNamePattern.toString().toLowerCase());
			ReflectionUtils.makeAccessible(field);
			Object value = ReflectionUtils.getField(field, deferredTrack);
    		pattern = pattern.replaceAll(trackNamePattern.toString(), value.toString());
		}
    	String extension = deferredTrack.getExtension();
    	return pattern + "." + extension;
	}

    public File getFile(String libraryPath){
        return new File(Objects.requireNonNull(localMusicFileLocation), libraryPath);
    }

    public boolean deleteFile(Track track) {
        log.debug("Permanently deleting file {}", track.getLibraryPath());
		File fileToDelete = new File(Objects.requireNonNull(localMusicFileLocation), track.getLibraryPath());
		boolean fileDeleted = fileToDelete.delete();
        if(fileDeleted){
        	log.trace("Successfully deleted file {}", track.getLibraryPath());
            String trackDirectory = FilenameUtils.getFullPath(fileToDelete.getAbsolutePath());
            boolean dirDeleted = recursivelyDeleteEmptyDirectories(trackDirectory);
        } else {
        	log.warn("Failed to delete file {}", track.getLibraryPath());
        	try {
				Files.delete(fileToDelete.toPath());
			} catch (IOException e) {
				log.warn("Failed to delete file {} on second try", track.getLibraryPath(), e);
			}
		}
        return fileDeleted;
    }

    /**
     * Deletes the supplied directory if it is empty. If it is empty, goes one directory up and repeats process until
     * a nonempty directory is found, then stops.
     */
    public boolean recursivelyDeleteEmptyDirectories(String directory) {
        boolean wasDirectoryDeleted = new File(directory).delete();
        if (wasDirectoryDeleted) {
            log.debug("Directory {} is empty and was deleted", directory);
            return recursivelyDeleteEmptyDirectories(FilenameUtils.getFullPath(directory.substring(0, directory.length() - 1)));
        } else {
            log.debug("Directory {} is not empty and was not deleted", directory);
        }
        return wasDirectoryDeleted;
    }
}
