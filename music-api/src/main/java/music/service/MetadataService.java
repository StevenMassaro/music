package music.service;

import music.model.DeferredTrack;
import music.model.Track;
import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
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

    @Autowired
    private FileService fileService;

    @Value("${music.file.source}")
    private String musicFileSource;

    public List<Track> getTracks() throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        List<File> files = new ArrayList<>(fileService.listMusicFiles());
        logger.info(String.format("Found %s files in music directory.", files.size()));

        List<Track> tracks = new ArrayList<>();

        for (int i = 0; i < files.size(); i++) {
            File file = files.get(i);
            logger.debug(String.format("Processing file %s of %s: %s", (i + 1), files.size(), file.getName()));
            AudioFile audioFile = AudioFileIO.read(file);
            Tag tag = audioFile.getTag();
            try {
                Track track = new DeferredTrack(tag, file.getAbsolutePath().replace(musicFileSource, ""), file, musicFileSource);
                tracks.add(track);
            } catch (Exception e) {
                logger.error(String.format("Failed to parse tag for metadata for file %s", file.getAbsolutePath()), e);
            }
        }
        return tracks;
    }
}
