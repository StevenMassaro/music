package music.service;

import lombok.extern.log4j.Log4j2;
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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Objects;
import java.util.regex.Matcher;

@Service
@Log4j2
public class FileService extends AbstractService {

	@Value("${music.acceptable.file.extensions:}")
	private String acceptableExtensions;

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
    public File writeTempTrack(InputStream inputStream, String extension) throws IOException {
    	// copy file to temp file
    	File tempTrack = File.createTempFile(RandomStringUtils.randomAlphabetic(10), "." + extension);
    	FileUtils.copyInputStreamToFile(inputStream, tempTrack);

		return tempTrack;
	}

	/**
	 * Move the temporary track into a dynamically generated folder, determined by using the pattern specified in
	 * the application properties and replacing those placeholder values with the actual values from the file's ID3 tag.
	 */
	public File moveTempTrack(File tempTrack, Track metadata) throws Exception {
		String newFullPath = generateFilename(metadata); // includes filename at end of path
		String newPath = FilenameUtils.getPath(newFullPath); // just the path, no filename
		String newFilename = FilenameUtils.getName(newFullPath);

		log.debug("Moving file {} to {}", tempTrack, newFullPath);

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
	public String generateFilename(Track deferredTrack) throws Exception {
    	String pattern = Paths.get(deferredTrack.getLibrary().getSubfolder(), deferredTrack.getLibrary().getTrackNamePattern()).toString();
    	int successfulReplaces = 0;
    	int failedReplaces = 0;
    	for(TrackNamePattern trackNamePattern : TrackNamePattern.values()){
			Field field = ReflectionUtils.findField(deferredTrack.getClass(), trackNamePattern.toString().toLowerCase());
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				Object value = ReflectionUtils.getField(field, deferredTrack);
				String trackNamePatternString = trackNamePattern.toString();
				if (value != null && ((!(value instanceof String)) || !((String) value).isEmpty())) {
					log.trace("Replacing {} with value {}", trackNamePatternString, value);
					pattern = replaceStringInPattern(pattern, trackNamePatternString, value);
					successfulReplaces++;
				} else {
					value = "Unknown";
					log.trace("Replacing {} with value {}", trackNamePatternString, value);
					pattern = replaceStringInPattern(pattern, trackNamePatternString, value);
					failedReplaces++;
				}
			}
		}

    	if (successfulReplaces == 0 && failedReplaces > 0) {
    		throw new Exception("Failed to generate a filename from the metadata from the track. This usually occurs when the uploaded track has no metadata.");
		}

    	String extension = deferredTrack.getExtension();
    	return pattern + "." + extension;
	}

	private String replaceStringInPattern(String pattern, String matcher, Object value) {
		return pattern.replaceAll(matcher, Matcher.quoteReplacement(value.toString()).replaceAll("[^a-zA-Z0-9.\\-_\\s]", ""));
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
	 * NOTE that delete is called on the supplied File, regardless of whether it is a directory or a file.
	 */
	public boolean recursivelyDeleteEmptyDirectories(String directory) {
		return recursivelyDeleteEmptyDirectories(new File(directory));
	}

    /**
     * Deletes the supplied directory if it is empty. If it is empty, goes one directory up and repeats process until
     * a nonempty directory is found, then stops.
	 * NOTE that delete is called on the supplied File, regardless of whether it is a directory or a file.
     */
    public boolean recursivelyDeleteEmptyDirectories(File directory) {
    	if (directory == null) {
    		return false;
		}
        boolean wasDirectoryDeleted = directory.delete();
        if (wasDirectoryDeleted) {
            log.debug("Directory {} is empty and was deleted", directory);
            return recursivelyDeleteEmptyDirectories(directory.getParentFile());
        } else {
            log.debug("Directory {} is not empty and was not deleted", directory);
        }
        return wasDirectoryDeleted;
    }
}
