package music.service;

import music.mapper.TrackMapper;
import music.model.Track;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TrackService {

    @Autowired
    private TrackMapper trackMapper;

    public void addTracks(List<Track> tracks){
        for(Track track : tracks){
            trackMapper.insert(track);
        }
    }

    public List<Track> list(){
        return trackMapper.list();
    }
}
