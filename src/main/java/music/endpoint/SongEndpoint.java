package music.endpoint;

import music.mapper.SongMapper;
import music.model.Song;
import music.model.SongVO;
import music.service.SongService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/song")
public class SongEndpoint {

//    @Autowired
//    private SongMapper songsMapper;

    @Autowired
    private SongService songService;

    @GetMapping("/{id}")
    public SongVO count(@PathVariable int id) {
        return new SongVO(songService.get(id));
    }

    @GetMapping()
    public List<SongVO> list() {
        return SongVO.toList(songService.list());
    }

    @GetMapping("/{id}/download")
    public ResponseEntity<Resource> download(@PathVariable int id) throws IOException {
        Song song = songService.get(id);

        Resource file = new InputStreamResource(FileUtils.openInputStream(new File("C:\\Users\\Steven\\Desktop\\Dev\\Music\\" + song.getFilename())));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,"inline; filename=\""+song.getFilename()+"\"")
                .header(HttpHeaders.CONTENT_TYPE, "audio/"+song.getExtension().toLowerCase())
                .body(file);
    }
}
