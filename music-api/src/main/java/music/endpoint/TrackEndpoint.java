package music.endpoint;

import music.model.Track;
import music.service.FileService;
import music.service.TrackService;
import music.service.UpdateService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/track")
public class TrackEndpoint {

    private final FileService fileService;

    private final TrackService trackService;

    private final UpdateService updateService;

    @Value("${music.file.source}")
    private String musicFileSource;

    @Autowired
    public TrackEndpoint(FileService fileService, TrackService trackService, UpdateService updateService) {
        this.fileService = fileService;
        this.trackService = trackService;
        this.updateService = updateService;
    }

    @GetMapping
    public List<Track> list() {
        return trackService.list();
    }

    @DeleteMapping("/{id}")
    public Track delete(@PathVariable long id) throws IOException {
        return trackService.markDeleted(id);
    }

    @PatchMapping("/{id}/{field}/{value}")
    public Track updateTrackInfo(@PathVariable long id, @PathVariable String field, @PathVariable String value){
        // todo assert that the field is actually one of the fields that can be updated
        updateService.queueTrackUpdate(id, field, value);
        return trackService.get(id);
    }

    /*
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
    */

    @GetMapping("/{id}/stream")
    public ResponseEntity<Resource> stream(@PathVariable long id) throws IOException {
        Track track = trackService.get(id);

        Resource file = new InputStreamResource(FileUtils.openInputStream(fileService.getFile(track)));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + FilenameUtils.getName(track.getLocation()) + "\"")
                .header(HttpHeaders.CONTENT_TYPE, "audio/" + FilenameUtils.getExtension(track.getLocation()).toLowerCase())
                .body(file);
    }

    @PostMapping("/{id}/listened")
    public Track markTrackAsListened(@PathVariable long id, @RequestParam long deviceId){
        return trackService.markListened(id, deviceId);
    }
}
