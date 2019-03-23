package music.service;

import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class FileService {

    public Collection<File> listMusicFiles() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
//        return FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true);
        return FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true);
    }

    public File getFile() throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
        return new ArrayList<>(listMusicFiles()).get(0);
    }
}
