package music.service;

import music.exception.RatingRangeException;
import music.mapper.PlayMapper;
import music.mapper.SkipMapper;
import music.mapper.TrackMapper;
import music.model.*;
import music.repository.IPlayCountRepository;
import music.repository.IPlayRepository;
import music.repository.ISkipRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
    private final SkipMapper skipMapper;
    private final MetadataService metadataService;
	private final IPlayRepository playRepository;
	private final ISkipRepository skipRepository;
	private final IPlayCountRepository playCountRepository;

	@Autowired
    public TrackService(TrackMapper trackMapper, PlayMapper playMapper, FileService fileService, UpdateService updateService, SmartPlaylistService smartPlaylistService, ConvertService convertService, SkipMapper skipMapper, MetadataService metadataService, IPlayRepository playRepository, ISkipRepository skipRepository, IPlayCountRepository playCountRepository) {
        this.trackMapper = trackMapper;
        this.playMapper = playMapper;
        this.fileService = fileService;
        this.updateService = updateService;
		this.smartPlaylistService = smartPlaylistService;
		this.convertService = convertService;
		this.skipMapper = skipMapper;
		this.metadataService = metadataService;
		this.playRepository = playRepository;
		this.skipRepository = skipRepository;
		this.playCountRepository = playCountRepository;
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
				if (existingTrack != null && !existingTrack.getDeletedInd()) {
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
    public Track permanentlyDelete(long id) {
        Track track = get(id);
        return permanentlyDelete(track);
    }

    /**
     * Deletes the track from the file system and deletes any relevant metadata from the database.
     */
    public Track permanentlyDelete(Track track) {
    	logger.debug("Permanently deleting track {}", track.getId());
        fileService.deleteFile(track);
        permanentlyDeleteTrackMetadata(track);
        return track;
    }

    /**
     * Delete the metadata for a track, in the order necessary to prevent foreign key errors.
     */
	private void permanentlyDeleteTrackMetadata(Track track) {
		permanentlyDeleteTrackMetadata(track.getId());
    }

	/**
	 * Delete the metadata for a track, in the order necessary to prevent foreign key errors.
	 */
	private void permanentlyDeleteTrackMetadata(long id) {
		logger.debug("Permanently deleting all track metadata for {}", id);
		playMapper.deletePlayCounts(id);
		playMapper.deletePlays(id);

		skipMapper.deleteSkipCounts(id);
		skipMapper.deleteBySongId(id);

		convertService.deleteHash(id);
		updateService.deleteUpdateBySongId(id);
		trackMapper.deleteById(id);
	}

	public Track uploadNewTrack(MultipartFile file) throws IOException {
		// write temp track to disk
		File track = fileService.writeTempTrack(file);

		// scan metadata for this track
		DeferredTrack tempTrackMetadata = metadataService.parseMetadata(track);

		File newTrack = fileService.moveTempTrack(track, tempTrackMetadata);

		DeferredTrack trackMetadata = metadataService.parseMetadata(newTrack);
		upsertTracks(Collections.singletonList(trackMetadata), null);

		return getByLocation(trackMetadata.getLocation());
	}

	public Track replaceExistingTrack(MultipartFile file, long existingId) throws IOException {
		// first list of all of the existing plays
		List<Play> existingTrackPlays = playRepository.findAllBySongId(existingId);
		logger.debug("Found {} plays for {}", existingTrackPlays.size(), existingId);
		Optional<PlayCount> optionalExistingPlayCount = playCountRepository.findById(existingId);
		if(optionalExistingPlayCount.isPresent()) {
			logger.debug("Play count also present for {} to be migrated", existingId);
		}
		List<Skip> existingTrackSkips = skipRepository.findAllBySongId(existingId);
		logger.debug("Found {} skips for {}", existingTrackSkips.size(), existingId);

		// delete the existing track
		permanentlyDelete(existingId);

		// create the new track
		Track newTrack = uploadNewTrack(file);

		logger.debug("Inserting {} plays for new track {}", existingTrackPlays.size(), newTrack.getId());
		for (Play existingTrackPlay : existingTrackPlays) {
			playMapper.insertPlay(newTrack.getId(), existingTrackPlay.getPlayDate(), existingTrackPlay.getDeviceId(), existingTrackPlay.isImported());
		}
		if (optionalExistingPlayCount.isPresent()) {
			logger.debug("Inserting play count for new track {}", newTrack.getId());
			PlayCount pc = optionalExistingPlayCount.get();
			playMapper.upsertPlayCount(newTrack.getId(), pc.getDeviceId(), pc.getPlayCount(), pc.isImported());
		}
		logger.debug("Inserting {} skips for new track {}", existingTrackSkips.size(), newTrack.getId());
		for (Skip existingTrackSkip : existingTrackSkips) {
			skipMapper.insertSkip(newTrack.getId(), existingTrackSkip.getSkipDate(), existingTrackSkip.getDeviceId(), existingTrackSkip.isImported(), existingTrackSkip.getSecondsPlayed());
		}
		return get(newTrack.getId());
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

	public Track markSkipped(long id, long deviceId, Double secondsPlayed) throws Exception {
		logger.debug("Marking {} as skipped on device {}", id, deviceId);
		Track track = get(id);
		if (secondsPlayed != null && secondsPlayed >= track.getDuration()) {
			throw new Exception("This skip record is being ignored, because the number of seconds played before skipping" +
				" exceeds the duration of the track, thus it can be assumed that the entire track was played.");
		} else {
			skipMapper.insertSkip(id, new Date(), deviceId, false, secondsPlayed);
			return track;
		}
	}
}
