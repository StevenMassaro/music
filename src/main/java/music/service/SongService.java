package music.service;

import music.mapper.SongMapper;
import music.model.Song;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SongService {

    @Autowired
    private SongMapper songMapper;

    public Song get(int id){
        return songMapper.selectByPrimaryKey(id);
    }

    public List<Song> list(){
        return songMapper.selectByExample(null);
    }
}
