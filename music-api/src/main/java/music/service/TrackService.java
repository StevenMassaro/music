package music.service;

import music.exception.RatingRangeException;
import music.mapper.PlayMapper;
import music.mapper.TrackMapper;
import music.model.Track;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class TrackService {
    private Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackMapper trackMapper;
    private final PlayMapper playMapper;
    private final FileService fileService;
    private final UpdateService updateService;

    @Autowired
    public TrackService(TrackMapper trackMapper, PlayMapper playMapper, FileService fileService, UpdateService updateService) {
        this.trackMapper = trackMapper;
        this.playMapper = playMapper;
        this.fileService = fileService;
        this.updateService = updateService;
    }

    public void upsertTracks(List<Track> tracks){
        for(Track track : tracks){
            try {
                Track existingTrack = getByLocation(track.getLocation());
                if (existingTrack != null) {
                    if (!existingTrack.getFileLastModifiedDate().equals(track.getFileLastModifiedDate())) {
                        logger.debug(String.format("Existing track has been modified since last sync, updating: %s", existingTrack.getTitle()));
                        trackMapper.updateByLocation(track);
                    } else {
                        logger.debug(String.format("Existing track has same modified date as last sync, skipping: %s", existingTrack.getTitle()));
                    }
                } else {
                    logger.debug(String.format("No existing track found, inserting new metadata for %s", track.getTitle()));
                    trackMapper.insert(track);
                }
            } catch (Exception e) {
                logger.error(String.format("Failed to insert metadata for track %s", track.getLocation()), e);
            }
        }
    }

    /**
     * Lists all non-deleted tracks, applying the updates that are queued.
     */
    public List<Track> list(){
        return updateService.applyUpdates(trackMapper.list());
    }

    /**
     * Lists all tracks, including those that were marked deleted in the database, applying the updates
     * that are queued.
     */
    public List<Track> listAll(){
        return updateService.applyUpdates(trackMapper.listAll());
    }

    public Track getByLocation(String location) {
        return updateService.applyUpdates(trackMapper.getByLocation(location));
    }

    public Track get(long id){
        return updateService.applyUpdates(trackMapper.get(id));
    }

    public Track get(String title, String artist, String album, List<Track> trackCache) {
        for (Track track : trackCache) {
            if (track.getTitle().equals(title)
                    && track.getArtist().equals(artist)
                    && track.getAlbum().equals(album)) {
                return track;
            }
        }
        // todo: this should find the track out of the database
        return null;
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

    public Track markListened(long id, long deviceId){
        Track track = get(id);
        playMapper.insertPlay(id, new Date(), deviceId, false);
        return track;
    }

    /**
     * Set the rating of a track with a particular id.
     * @param rating the rating of the song, between 0 and 10 (inclusive)
     * @throws RatingRangeException if the supplied rating is outside the allowable range
     */
    public void setRating(long id, byte rating) throws RatingRangeException {
        if(rating > 10 || rating < 0){
            throw new RatingRangeException(String.format("Rating %s is outside range of [0-10]", rating));
        } else {
            trackMapper.setRatingById(id, rating);
        }
    }
}
