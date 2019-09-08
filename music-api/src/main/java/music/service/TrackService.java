package music.service;

import music.mapper.TrackMapper;
import music.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;

@Service
public class TrackService {
    private Logger logger = LoggerFactory.getLogger(TrackService.class);

    @Autowired
    private TrackMapper trackMapper;

    @Autowired
    private FileService fileService;

    public void upsertTracks(List<Track> tracks){
        for(Track track : tracks){
            try {
                trackMapper.upsert(track);
            } catch (Exception e) {
                logger.error(String.format("Failed to insert metadata for track %s", track.getLocation()), e);
            }
        }
    }

    public List<Track> list(){
        return trackMapper.list();
    }

    public Track get(long id){
        return trackMapper.get(id);
    }

    public Track delete(long id) throws IOException {
        Track track = get(id);
        fileService.deleteFile(track);
        trackMapper.deleteById(id);
        return track;
    }
}