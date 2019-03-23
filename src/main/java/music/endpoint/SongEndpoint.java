package music.endpoint;

import music.mapper.SongMapper;
import music.model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/song")
public class SongEndpoint {

    @Autowired
    private SongMapper songsMapper;

    @GetMapping("/count")
    public Song count(){
        return songsMapper.selectByPrimaryKey(16668);
    }

    @GetMapping()
    public List<Song> list(){
        return songsMapper.selectByExample(null);
    }
}
