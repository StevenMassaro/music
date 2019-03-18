package music.service;

import music.model.Track;
import org.apache.commons.io.FilenameUtils;
import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Service
public class MetadataService {

    @Autowired
    private FileService fileService;

    public List<Track> parseIntoTracks(Collection<File> files) throws TagException, ReadOnlyFileException, CannotReadException, InvalidAudioFrameException, IOException {
        List<Track> tracks = new ArrayList<>();

        for(File file : files){
            String extension = FilenameUtils.getExtension(file.getName());
            Track track;
            if(extension.contains("mp3")){
                MP3File mp3File = (MP3File) AudioFileIO.read(file);
                AbstractID3v2Tag v2tag  = mp3File.getID3v2Tag();
                v2tag.getFieldCount();
                track = new Track(v2tag);
            } else if(extension.contains("flac")){
                AudioFile f = AudioFileIO.read(file);
                FlacTag flacTag = (FlacTag) f.getTag();
                flacTag.getFieldCount();
                track = new Track(flacTag);
            } else {
                track = new Track();
            }
            tracks.add(track);
        }
        return tracks;
    }
}
