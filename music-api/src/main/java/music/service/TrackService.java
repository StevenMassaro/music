package music.service;

import music.exception.RatingRangeException;
import music.mapper.PlayMapper;
import music.mapper.TrackMapper;
import music.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Date;
import java.util.List;

import static music.utils.FieldUtils.calculateHash;

@Service
public class TrackService {
    private Logger logger = LoggerFactory.getLogger(TrackService.class);

    private final TrackMapper trackMapper;
    private final PlayMapper playMapper;
    private final FileService fileService;
    private final UpdateService updateService;
    private final SmartPlaylistService smartPlaylistService;
    private final ConvertService convertService;

    @Autowired
    public TrackService(TrackMapper trackMapper, PlayMapper playMapper, FileService fileService, UpdateService updateService, SmartPlaylistService smartPlaylistService, ConvertService convertService) {
        this.trackMapper = trackMapper;
        this.playMapper = playMapper;
        this.fileService = fileService;
        this.updateService = updateService;
		this.smartPlaylistService = smartPlaylistService;
		this.convertService = convertService;
	}

    /**
     * Insert tracks if they don't exist, or update them if the file has changed, or if updates are being forced to occur.
     *
     * @param tracks       tracks to update/insert
     * @param syncResult   result object which will be modified during execution
     */
    public void upsertTracks(List<DeferredTrack> tracks, SyncResult syncResult){
        upsertTracks(tracks, syncResult, false);
    }

    /**
     * Insert tracks if they don't exist, or update them if the file has changed, or if updates are being forced to occur.
     *
     * @param tracks       tracks to update/insert
     * @param syncResult   result object which will be modified during execution
     * @param forceUpdates if true, all tracks will be updated, regardless of whether the file has been modified since
     *                     the last sync
     */
    public void upsertTracks(List<DeferredTrack> tracks, SyncResult syncResult, boolean forceUpdates) {
        for (DeferredTrack track : tracks) {
            try {
                Track existingTrack = getByLocation(track.getLocation());
                if (existingTrack != null) {
                    if (forceUpdates || !existingTrack.getFileLastModifiedDate().equals(track.getFileLastModifiedDate())) {
                        logger.debug(forceUpdates ? "Updates are being forced, updating {}" : "Existing track has been modified since last sync, updating: {}", existingTrack.getTitle());
                        track.setDateUpdated(new Date());
                        trackMapper.updateByLocation(track);
                        convertService.deleteHash(track.getLocation());
						if (syncResult != null) {
							syncResult.getModifiedTracks().add(track);
						}
                    } else {
                        logger.debug(String.format("Existing track has same modified date as last sync, skipping: %s", existingTrack.getTitle()));
						if (syncResult != null) {
							syncResult.getUnmodifiedTracks().add(track);
						}
                    }
                } else {
                    logger.debug(String.format("No existing track found, inserting new metadata for %s", track.getTitle()));
                    trackMapper.insert(track);
					if (syncResult != null) {
						syncResult.getNewTracks().add(track);
					}
                }
            } catch (Exception e) {
                logger.error(String.format("Failed to insert metadata for track %s", track.getLocation()), e);
				if (syncResult != null) {
					syncResult.getFailedTracks().add(track);
				}
            }
        }
    }

    /**
     * Lists all non-deleted tracks, applying the updates that are queued.
     */
    public List<Track> list(){
        return updateService.applyUpdates(trackMapper.list());
    }

    public List<Track> listByAlbum(String album, String artist, Long disc){
    	return updateService.applyUpdates(trackMapper.listByAlbum(album, artist, disc));
	}

    /**
     * Lists all tracks, including those that were marked deleted in the database, applying the updates
     * that are queued.
     */
    public List<Track> listAll(){
        return updateService.applyUpdates(trackMapper.listAll());
    }

    public List<Track> listWithSmartPlaylist(long playlistId) {
    	SmartPlaylist smartPlaylist = smartPlaylistService.get(playlistId);
    	return trackMapper.listWithSmartPlaylist(smartPlaylist.getDynamicSql());
	}

	/**
	 * Return the count of tracks that can be purged from the file system (or in other words, they were marked deleted
	 * in the database).
	 */
	public long countPurgableTracks(){
    	return trackMapper.countPurgableTracks();
	}

    public List<Track> listPlaysByDate(Date date) { return trackMapper.listPlaysByDate(date); }

    public List<Date> listHistoricalDates(){ return trackMapper.listHistoricalDates(); }

    public Track getByLocation(String location) {
        return updateService.applyUpdates(trackMapper.getByLocation(location));
    }

    public Track get(long id){
        return updateService.applyUpdates(trackMapper.get(id));
    }

    public Track get(String title, String artist, String album, List<Track> trackCache) {
		if (!CollectionUtils.isEmpty(trackCache)) {
			for (Track track : trackCache) {
				if (track.getTitle().equals(title)
					&& track.getArtist().equals(artist)
					&& track.getAlbum().equals(album)) {
					return track;
				}
			}
		}
		return trackMapper.getByTitleArtistAlbum(title, artist, album);
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
        permanentlyDeleteTrackMetadata(track);
        return track;
    }

    /**
     * Delete the metadata for a track, in the order necessary to prevent foreign key errors.
     */
    private void permanentlyDeleteTrackMetadata(Track track){
        playMapper.deletePlayCounts(track.getId());
        playMapper.deletePlays(track.getId());
        convertService.deleteHash(track.getId());
        updateService.deleteUpdateBySongId(track.getId());
        trackMapper.deleteById(track.getId());
    }

    /**
     * Delete metadata for all tracks in database which no longer exist on disk.
     * @param actualTracks list of tracks that exist on disk
     */
    public void deleteOrphanedTracksMetadata(List<DeferredTrack> actualTracks, SyncResult syncResult) {
        logger.debug("Begin deleted orphaned tracks");
        List<Track> dbTracks = listAll();
        // if a track exists in the database but doesn't exist on disk, then delete it from the db
        for (Track dbTrack : dbTracks){
            boolean doesTrackExistOnDisk = actualTracks.stream().anyMatch(t -> t.id3Equals(dbTrack));
            if(!doesTrackExistOnDisk){
                logger.debug("Track {} no longer exists on disk, deleting associated metadata ({} - {}; {})", dbTrack.getId(), dbTrack.getTitle(), dbTrack.getArtist(), dbTrack.getLocation());
                permanentlyDeleteTrackMetadata(dbTrack);
                syncResult.getOrphanedTracks().add(dbTrack);
            } else {
                logger.trace("Track {} still exists on disk ({} - {}; {})", dbTrack.getId(), dbTrack.getTitle(), dbTrack.getArtist(), dbTrack.getLocation());
                syncResult.getUnorphanedTracks().add(dbTrack);
            }
        }
        logger.debug("Finished deleted orphaned tracks");
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

    public Track updateField(long id, String field, Object newValue, SQLType type){
    	trackMapper.updateFieldById(id, field, newValue, type.getName());
    	return get(id);
	}

	public void updateHashOfTrack(String location, long id) throws IOException {
		String hash = calculateHash(fileService.getFile(location));
		logger.trace("Updating field hash to {} for ID: {}", hash, id);
		updateField(id, "hash", hash, JDBCType.VARCHAR);
	}

	/**
	 * Assign all historical plays to a new track ID.
	 * @param oldId the track to pull historical plays from
	 * @param newId the track to assign the plays to
	 */
	public void migratePlays(long oldId, long newId) {
		logger.debug("Migrating historical plays from {} to {}", oldId, newId);
		long migrated = trackMapper.migratePlays(oldId, newId);
		logger.trace("Migrated {} historical play rows", migrated);

		logger.debug("Migrating play count from {} to {}", oldId, newId);
		long migrated2 = trackMapper.migratePlayCount(oldId, newId);
		logger.trace("Migrated {} play count rows", migrated2);
	}
}
