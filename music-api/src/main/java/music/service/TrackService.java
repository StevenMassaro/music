package music.service;

import lombok.extern.log4j.Log4j2;
import music.exception.LibraryNotFoundException;
import music.exception.RatingRangeException;
import music.exception.TrackAlreadyExistsException;
import music.mapper.PlayMapper;
import music.mapper.SkipMapper;
import music.mapper.TrackMapper;
import music.model.*;
import music.repository.ILibraryRepository;
import music.repository.IPlayCountRepository;
import music.repository.IPlayRepository;
import music.repository.ISkipRepository;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.JDBCType;
import java.sql.SQLType;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static music.utils.HashUtils.calculateHash;

@Service
@Log4j2
public class TrackService {
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
	private final ILibraryRepository libraryRepository;

	@Autowired
    public TrackService(TrackMapper trackMapper, PlayMapper playMapper, FileService fileService, UpdateService updateService, SmartPlaylistService smartPlaylistService, ConvertService convertService, SkipMapper skipMapper, MetadataService metadataService, IPlayRepository playRepository, ISkipRepository skipRepository, IPlayCountRepository playCountRepository, ILibraryRepository libraryRepository) {
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
		this.libraryRepository = libraryRepository;
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
            	// we must load not by ID here because we may not know the tracks ID, like if we are syncing from disk
                Track existingTrack = getByLocationAndLibrary(track.getLocation(), track.getLibrary().getId());
				if (existingTrack != null && !existingTrack.getDeletedInd()) {
                    if (forceUpdates || !existingTrack.getFileLastModifiedDate().equals(track.getFileLastModifiedDate())) {
                        log.debug(forceUpdates ? "Updates are being forced, updating {}" : "Existing track has been modified since last sync, updating: {}", existingTrack.getTitle());
                        track.setDateUpdated(new Date());
                        // since there is an existing track that we're updating, we should use the existing tracks ID for updates
                        track.setId(existingTrack.getId());
                        trackMapper.update(track);
						if (syncResult != null) {
							syncResult.getModifiedTracks().add(track);
						}
                    } else {
                        log.debug("Existing track has same modified date as last sync, skipping: {}", existingTrack.getTitle());
						if (syncResult != null) {
							syncResult.getUnmodifiedTracks().add(track);
						}
                    }
                } else {
                    log.debug("No existing track found, inserting new metadata for {}", track.getTitle());
                    trackMapper.insert(track);
					if (syncResult != null) {
						syncResult.getNewTracks().add(track);
					}
                }
            } catch (Exception e) {
                log.error("Failed to insert metadata for track {}", track.getLibraryPath(), e);
				if (syncResult != null) {
					syncResult.getFailedTracks().add(track);
				}
            }
        }
    }

    public List<Track> list() {
    	return trackMapper.list();
	}

	public void update(Track track) {
    	trackMapper.update(track);
	}

    /**
     * Lists all non-deleted tracks, applying the updates that are queued.
	 * @param libraryId only tracks in this library will be returned. If null, all tracks will be returned.
     */
    public List<Track> list(Long libraryId){
		if (libraryId == null) {
			return trackMapper.list();
		} else {
			return trackMapper.listByLibraryId(libraryId);
		}
	}

    public List<Track> listByAlbum(String album, String artist, Long disc){
    	return trackMapper.listByAlbum(album, artist, disc);
	}

    /**
     * Lists all tracks, including those that were marked deleted in the database, applying the updates
     * that are queued.
     */
    public List<Track> listDeleted(){
        return trackMapper.listDeleted();
    }

	/**
	 * Lists all tracks, including those that were marked deleted in the database, applying the updates
	 * that are queued.
	 * @param libraryId only tracks in this library will be returned
	 */
    public List<Track> listDeleted(long libraryId){
        return trackMapper.listDeletedByLibraryId(libraryId);
    }

    public List<Track> listWithSmartPlaylist(long playlistId) {
    	SmartPlaylist smartPlaylist = smartPlaylistService.get(playlistId);
    	return trackMapper.listWithSmartPlaylist(smartPlaylist.getDynamicSql());
	}

	public List<Track> listWithPlaylist(Long playlistId) {
		return trackMapper.listWithPlaylist(playlistId);
	}

	/**
	 * Return the count of tracks that can be purged from the file system (or in other words, they were marked deleted
	 * in the database).
	 */
	public long countPurgableTracks(){
    	return trackMapper.countPurgableTracks();
	}

	public List<Track> listPurgableTracks() {
		return trackMapper.listPurgableTracks();
	}

    public List<Track> listPlaysByDate(Date date) { return trackMapper.listPlaysByDate(date); }

    public List<Date> listHistoricalDates(){ return trackMapper.listHistoricalDates(); }

    private Track getByLocationAndLibrary(String location, long libraryId) {
        return trackMapper.getByLocationAndLibrary(location, libraryId);
    }

    public Track get(long id){
        return trackMapper.get(id);
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
    	log.debug("Permanently deleting track {}", track.getId());
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
		log.debug("Permanently deleting all track metadata for {}", id);
		playMapper.deletePlayCounts(id);
		playMapper.deletePlays(id);

		skipMapper.deleteSkipCounts(id);
		skipMapper.deleteBySongId(id);

		updateService.deleteUpdateBySongId(id);
		trackMapper.deleteById(id);
	}

	public Track uploadNewTrack(MultipartFile file, long libraryId) throws Exception {
		return uploadNewTrack(file.getInputStream(), FilenameUtils.getExtension(file.getOriginalFilename()), libraryId);
	}

	public Track uploadNewTrack(InputStream inputStream, String fileExtension, long libraryId) throws Exception {
		Optional<Library> libraryOpt = libraryRepository.findById(libraryId);
		if(!libraryOpt.isPresent()) {
			throw new LibraryNotFoundException(libraryId);
		}
		// write temp track to disk
		File track = fileService.writeTempTrack(inputStream, fileExtension);

		// scan metadata for this track
		DeferredTrack tempTrackMetadata = metadataService.parseMetadata(track, libraryOpt.get());

		File newTrack = fileService.moveTempTrack(track, tempTrackMetadata);

		DeferredTrack trackMetadata = metadataService.parseMetadata(newTrack, libraryOpt.get());
		SyncResult syncResult = new SyncResult();
		upsertTracks(Collections.singletonList(trackMetadata), syncResult);

		// todo, is this change in return handling safe to make with the UI? can we make the UI display these error messages inline in the file upload modal?
		if (!syncResult.getNewTracks().isEmpty()) {
			return get(syncResult.getNewTracks().get(0).getId());
		} else if (!syncResult.getFailedTracks().isEmpty()) {
			throw new Exception("Failed to upload track.");
		} else if (!syncResult.getModifiedTracks().isEmpty()) {
			throw new TrackAlreadyExistsException();
		} else {
			return null;
		}
	}


	public Track copyMetadata(long fromId, long toId) {
		Track existingTrack = get(fromId);

		// first list of all of the existing plays
		List<Play> existingTrackPlays = playRepository.findAllBySongId(fromId);
		log.debug("Found {} plays for {}", existingTrackPlays.size(), fromId);
		Optional<PlayCount> optionalExistingPlayCount = playCountRepository.findById(fromId);
		if(optionalExistingPlayCount.isPresent()) {
			log.debug("Play count also present for {} to be migrated", fromId);
		}
		List<Skip> existingTrackSkips = skipRepository.findAllBySongId(fromId);
		log.debug("Found {} skips for {}", existingTrackSkips.size(), fromId);

		log.debug("Inserting {} plays for new track {}", existingTrackPlays.size(), toId);
		for (Play existingTrackPlay : existingTrackPlays) {
			playMapper.insertPlay(toId, existingTrackPlay.getPlayDate(), existingTrackPlay.getDeviceId(), existingTrackPlay.isImported());
		}
		if (optionalExistingPlayCount.isPresent()) {
			log.debug("Inserting play count for new track {}", toId);
			PlayCount pc = optionalExistingPlayCount.get();
			playMapper.upsertPlayCount(toId, pc.getDeviceId(), pc.getPlayCount(), pc.isImported());
		}
		log.debug("Inserting {} skips for new track {}", existingTrackSkips.size(), toId);
		for (Skip existingTrackSkip : existingTrackSkips) {
			skipMapper.insertSkip(toId, existingTrackSkip.getSkipDate(), existingTrackSkip.getDeviceId(), existingTrackSkip.isImported(), existingTrackSkip.getSecondsPlayed());
		}
		Integer existingTrackRating = existingTrack.getRating();
		if (existingTrackRating != null) {
			try {
				setRating(toId, existingTrackRating);
			} catch (RatingRangeException e) {
				log.error("Failed to set rating of {} on track {}", existingTrackRating, toId, e);
			}
		}

		return get(toId);
	}

	public Track replaceExistingTrack(MultipartFile file, long existingId) throws Exception {
		// first list of all of the existing plays
		Track existingTrack = get(existingId);

		// create the new track
		Track newTrack = uploadNewTrack(file, existingTrack.getLibrary().getId());

		newTrack = copyMetadata(existingId, newTrack.getId());

		// delete the existing track
		permanentlyDelete(existingId);

		return newTrack;
	}

    /**
     * Delete metadata for all tracks in database which no longer exist on disk.
     * @param actualTracks list of tracks that exist on disk
     */
    public void deleteOrphanedTracksMetadata(List<DeferredTrack> actualTracks, SyncResult syncResult, Library library) {
        log.debug("Begin deleting orphaned tracks");
        List<Track> dbTracks = list(library.getId());
        // if a track exists in the database but doesn't exist on disk, then delete it from the db
        for (Track dbTrack : dbTracks){
            boolean doesTrackExistOnDisk = actualTracks.stream().anyMatch(t -> t.id3Equals(dbTrack));
            if(!doesTrackExistOnDisk){
                log.debug("Track {} no longer exists on disk, deleting associated metadata ({} - {}; {})", dbTrack.getId(), dbTrack.getTitle(), dbTrack.getArtist(), dbTrack.getLibraryPath());
                permanentlyDeleteTrackMetadata(dbTrack);
                syncResult.getOrphanedTracks().add(dbTrack);
            } else {
                log.trace("Track {} still exists on disk ({} - {}; {})", dbTrack.getId(), dbTrack.getTitle(), dbTrack.getArtist(), dbTrack.getLibraryPath());
                syncResult.getUnorphanedTracks().add(dbTrack);
            }
        }
        log.debug("Finished deleted orphaned tracks");
    }

    /**
     * Mark the track deleted in the database. Does not actually delete the file from the file system.
     */
    public Track markDeleted(long id){
        trackMapper.markDeletedById(id, true);
        return get(id);
    }

    public Track markListened(long id, long deviceId){
        playMapper.insertPlay(id, new Date(), deviceId, false);
        return get(id);
    }

    /**
     * Set the rating of a track with a particular id.
     * @param rating the rating of the song, between 0 and 10 (inclusive)
     * @throws RatingRangeException if the supplied rating is outside the allowable range
     */
    public void setRating(long id, int rating) throws RatingRangeException {
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

	public void updateHashOfTrack(String libraryPath, long id) throws IOException {
		String hash = calculateHash(fileService.getFile(libraryPath));
		log.trace("Updating field hash to {} for ID: {}", hash, id);
		updateField(id, "hash", hash, JDBCType.VARCHAR);
	}

	public Track markSkipped(long id, long deviceId, Double secondsPlayed) throws Exception {
		log.debug("Marking {} as skipped on device {}", id, deviceId);
		Track track = get(id);
		if (secondsPlayed != null && secondsPlayed >= track.getDuration()) {
			throw new Exception("This skip record is being ignored, because the number of seconds played before skipping" +
				" exceeds the duration of the track, thus it can be assumed that the entire track was played.");
		} else {
			skipMapper.insertSkip(id, new Date(), deviceId, false, secondsPlayed);
			return get(id);
		}
	}
}
