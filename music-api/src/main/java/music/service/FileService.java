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

    public void deleteFile(Track track) throws IOException {
        boolean fileDeleted = new File(musicFileSource + track.getLocation()).delete();
        String trackDirectory = FilenameUtils.getFullPath(musicFileSource + track.getLocation());
        recursivelyDeleteEmptyDirectories(trackDirectory);
    }

    /**
     * Deletes the supplied directory if it is empty. If it is empty, goes one directory up and repeats process until
     * a nonempty directory is found, then stops.
     */
    public boolean recursivelyDeleteEmptyDirectories(String directory) throws IOException {
        Collection<File> filesInDirectory = FileUtils.listFiles(new File(directory), null, false);
        if(filesInDirectory.isEmpty()){
            logger.debug(String.format("Directory %s is empty, deleting", directory));
            FileUtils.deleteDirectory(new File(directory));
            recursivelyDeleteEmptyDirectories(FilenameUtils.getFullPath(directory.substring(0, directory.length()-1)));
            return true;
        } else {
            logger.debug(String.format("Directory %s is not empty, stopping deletion process", directory));
            return false;
        }
    }
}
