package music.service;

import music.model.Track;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

@Service
public class FileService {

    @Autowired
    private TrackService trackService;

    @Value("${music.file.source}")
    private String musicFileSource;

    private Logger logger = LoggerFactory.getLogger(FileService.class);

    public Collection<File> listMusicFiles() {
//        return FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true);
        return FileUtils.listFiles(new File(musicFileSource), new String[]{"mp3", "flac", "FLAC"}, true);
    }

    public File getFile(Track track) {
        return new File(musicFileSource + track.getLocation());
    }

    public File getFile(long id){
        return getFile(trackService.get(id));
    }

    public boolean deleteFile(Track track) throws IOException {
        logger.debug(String.format("Permanently deleting file %s", track.getLocation()));
        boolean fileDeleted = new File(musicFileSource + track.getLocation()).delete();
        if(fileDeleted){
            String trackDirectory = FilenameUtils.getFullPath(musicFileSource + track.getLocation());
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
