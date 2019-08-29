package music.endpoint;

import music.model.Track;
import music.service.FileService;
import music.service.MetadataService;
import music.service.TrackService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.impl.DefaultFileSystemManager;
import org.apache.commons.vfs2.provider.ram.RamFileProvider;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ws.schild.jave.AudioAttributes;
import ws.schild.jave.Encoder;
import ws.schild.jave.EncodingAttributes;
import ws.schild.jave.MultimediaObject;

import java.io.File;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/track")
public class TrackEndpoint {

    @Autowired
    private FileService fileService;

    @Autowired
    private TrackService trackService;

    @Value("${music.file.source}")
    private String musicFileSource;

    @GetMapping
    public List<Track> list() {
        return trackService.list();
    }

    @GetMapping("{id}/convert")
    public ResponseEntity<Resource> convertFile(@PathVariable long id) {
        try {
            File source = fileService.getFile(id);
            File target = File.createTempFile("example", ".mp3");
            target.deleteOnExit();

            //Audio Attributes
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libmp3lame");
            audio.setBitRate(256000);
            audio.setChannels(2);
            audio.setSamplingRate(44100);

            //Encoding attributes
            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setFormat("mp3");
            attrs.setAudioAttributes(audio);

            //Encode
            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(source), target, attrs);
            Resource file = new ByteArrayResource(FileUtils.readFileToByteArray(target));
            return ResponseEntity.ok().header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=\"" + target.getName() + "\"").body(file);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable long id) throws IOException {
        Track track = trackService.get(id);

        Resource file = new InputStreamResource(FileUtils.openInputStream(fileService.getFile(track)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + FilenameUtils.getName(track.getLocation()) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "audio/" + FilenameUtils.getExtension(track.getLocation()).toLowerCase())
                .body(file);
    }
}
