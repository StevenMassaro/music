package music.service;

import music.model.Track;
import music.settings.PrivateSettings;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Service
public class FileService {

    private final PrivateSettings privateSettings;

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    public FileService(PrivateSettings privateSettings) {
        this.privateSettings = privateSettings;
    }

    public Collection<File> listMusicFiles() {
        SuffixFileFilter caseInsensitiveExtensionFilter = new SuffixFileFilter(privateSettings.getAcceptableExtensions().split(","), IOCase.INSENSITIVE);
        return FileUtils.listFiles(new File(privateSettings.getLocalMusicFileLocation()), caseInsensitiveExtensionFilter, TrueFileFilter.INSTANCE);
    }

    public File getFile(String location){
        return new File(privateSettings.getLocalMusicFileLocation() + location);
    }

    public boolean deleteFile(Track track) throws IOException {
        logger.debug(String.format("Permanently deleting file %s", track.getLocation()));
        boolean fileDeleted = new File(privateSettings.getLocalMusicFileLocation() + track.getLocation()).delete();
        if(fileDeleted){
            String trackDirectory = FilenameUtils.getFullPath(privateSettings.getLocalMusicFileLocation() + track.getLocation());
            boolean dirDeleted = recursivelyDeleteEmptyDirectories(trackDirectory);
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
            logger.debug(String.format("Directory %s is empty and was deleted", directory));
            return recursivelyDeleteEmptyDirectories(FilenameUtils.getFullPath(directory.substring(0, directory.length() - 1)));
        } else {
            logger.debug(String.format("Directory %s is not empty and was not deleted", directory));
        }
        return wasDirectoryDeleted;
    }
}