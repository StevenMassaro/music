package music.service;

import music.model.Track;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class FileService {

    @Autowired
    private TrackService trackService;

    @Value("${music.file.source}")
    private String musicFileSource;

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
}
