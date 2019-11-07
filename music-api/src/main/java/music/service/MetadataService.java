package music.service;

import music.model.DeferredTrack;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MetadataService {

    private Logger logger = LoggerFactory.getLogger(MetadataService.class);

    private final FileService fileService;

    @Value("${music.file.source}")
    private String musicFileSource;

    @Autowired
    public MetadataService(FileService fileService) {
        this.fileService = fileService;
    }

    public List<DeferredTrack> getTracks() throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        List<File> files = new ArrayList<>(fileService.listMusicFiles());
        logger.info(String.format("Found %s files in music directory.", files.size()));

        List<DeferredTrack> tracks = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            logger.debug(String.format("Processing file %s of %s: %s", (i + 1), files.size(), file.getName()));
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            AudioHeader header = audioFile.getAudioHeader();
            try {
                DeferredTrack track = new DeferredTrack(tag, header, file.getAbsolutePath().replace(musicFileSource, ""), file, musicFileSource);
                tracks.add(track);
            } catch (Exception e) {
                logger.error(String.format("Failed to parse tag for metadata for file %s", file.getAbsolutePath()), e);
            }
        }
        return tracks;
    }
}
