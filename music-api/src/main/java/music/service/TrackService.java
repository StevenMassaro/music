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

    /**
     * Lists all non-deleted tracks.
     */
    public List<Track> list(){
        return trackMapper.list();
    }

    /**
     * Lists all tracks, including those that were marked deleted in the database.
     */
    public List<Track> listAll(){
        return trackMapper.listAll();
    }

    public Track get(long id){
        return trackMapper.get(id);
    }

    /**
     * Deletes the track from the file system and deletes any relevant metadata from the database.
     */
    public Track permanentlyDelete(long id) throws IOException {
        Track track = get(id);
        return permanentlyDelete(track);
    }

    /**
     * Deletes the track from the file system and deletes any relevant metadata from the database.
     */
    public Track permanentlyDelete(Track track) throws IOException {
        fileService.deleteFile(track);
        trackMapper.deleteById(track.getId());
        return track;
    }

    /**
     * Mark the track deleted in the database. Does not actually delete the file from the file system.
     */
    public Track markDeleted(long id){
        Track track = get(id);
        trackMapper.markDeletedById(id, true);
        return track;
    }
}
