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

    public void upsertTracks(List<Track> tracks){
        for(Track track : tracks){
            trackMapper.upsert(track);
        }
    }

    public List<Track> list(){
        return trackMapper.list();
    }

    public Track get(long id){
        return trackMapper.get(id);
    }
}
