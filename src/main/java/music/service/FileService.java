package music.service;

import music.model.Track;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
public class FileService {

    @Autowired
    private MetadataService metadataService;

    public List<Track> listMusicFiles() throws ReadOnlyFileException, IOException, TagException, InvalidAudioFrameException, CannotReadException {
//        return FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true);
         return metadataService.parseIntoTracks(FileUtils.listFiles(new File("/music"), new String[]{"mp3", "flac", "FLAC"}, true));
    }
}
