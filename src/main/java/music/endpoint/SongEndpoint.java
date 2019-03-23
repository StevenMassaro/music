package music.endpoint;

import music.mapper.SongMapper;
import music.model.Song;
import music.model.SongVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/song")
public class SongEndpoint {

    @Autowired
    private SongMapper songsMapper;

    @GetMapping("/count")
    public SongVO count() {
        return new SongVO(songsMapper.selectByPrimaryKey(16668));
    }

    @GetMapping()
    public List<SongVO> list() {
        List<Song> songs = songsMapper.selectByExample(null);
        return songs.stream().map(SongVO::new).collect(Collectors.toList());
    }
}
