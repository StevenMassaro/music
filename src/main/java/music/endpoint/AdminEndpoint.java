package music.endpoint;

import music.model.Track;
import music.service.FileService;
import music.service.MetadataService;
import music.service.TrackService;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController()
@RequestMapping("/admin")
public class AdminEndpoint {

    @Autowired
    private FileService fileService;

    @Autowired
    private MetadataService metadataService;

    @Autowired
    private TrackService trackService;

    @GetMapping("/dbSync")
    public List<Track> syncTracksToDb() throws ReadOnlyFileException, CannotReadException, TagException, InvalidAudioFrameException, IOException {
        List<Track> files = metadataService.getTracks();
        trackService.addTracks(files);
        return trackService.list();
    }
}
