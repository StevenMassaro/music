package music.endpoint;

import com.google.common.base.Preconditions;
import lombok.extern.log4j.Log4j2;
import music.exception.RatingRangeException;
import music.model.Device;
import music.model.ModifyableTags;
import music.model.Track;
import music.model.TrackUpdate;
import music.service.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.Diff;
import org.apache.commons.lang3.builder.DiffResult;
import org.jaudiotagger.tag.datatype.Artwork;
import org.jaudiotagger.tag.id3.valuepair.ImageFormats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static music.utils.EndpointUtils.responseEntity;

@RestController
@RequestMapping("/track")
@Log4j2
public class TrackEndpoint extends AbstractEndpoint {
	private final TrackService trackService;

    private final UpdateService updateService;

    private final MetadataService metadataService;

	private final DeviceService deviceService;

	private final ConvertService convertService;

	private final TrackWebsocket trackWebsocket;
	private final FileService fileService;

    private final String DATE_FORMAT = "yyyy-MM-dd";

    @Autowired
	public TrackEndpoint(TrackService trackService, UpdateService updateService, MetadataService metadataService, DeviceService deviceService, ConvertService convertService, TrackWebsocket trackWebsocket, FileService fileService) {
		this.trackService = trackService;
        this.updateService = updateService;
        this.metadataService = metadataService;
		this.deviceService = deviceService;
		this.convertService = convertService;
		this.trackWebsocket = trackWebsocket;
		this.fileService = fileService;
	}

    @GetMapping
	public List<Track> list(@RequestParam(required = false, name = "smartPlaylist") Long smartPlaylistId,
							@RequestParam(required = false) Long playlist,
							@RequestParam(required = false) Long libraryId) {
		if (smartPlaylistId != null) {
			return trackService.listWithSmartPlaylist(smartPlaylistId);
		} else if (playlist != null) {
			return trackService.listWithPlaylist(playlist);
		} else {
			return trackService.list(libraryId);
		}
    }

    @GetMapping("/historical")
	public List<String> listHistoricalDates(){
		return trackService.listHistoricalDates().stream().map(d -> new SimpleDateFormat(DATE_FORMAT).format(d)).collect(Collectors.toList());
	}

	@GetMapping("/historical/{date}")
	public List<Track> listHistoricalPlaysByDate(@PathVariable @DateTimeFormat(pattern = DATE_FORMAT) Date date) {
    	return trackService.listPlaysByDate(date);
	}

    @DeleteMapping("/{id}")
    public Track delete(@PathVariable long id) throws IOException {
        return trackService.markDeleted(id);
    }

    @PatchMapping
	public Track updateTrackInfo(@RequestBody Track track){
    	Track existing = trackService.get(track.getId());
		DiffResult<?> diff = existing.diff(track);
		for(Diff<?> d: diff.getDiffs()) {
			log.trace("Applying track update: {}, from {} to {}", d.getFieldName(), d.getLeft(), d.getRight());
			updateService.queueTrackUpdate(track.getId(), d.getFieldName(), d.getRight().toString());
		}
		return trackService.get(track.getId());
	}

	@PatchMapping("/{trackId}/library/{libraryId}")
	public Track moveTrackToLibrary(@PathVariable long trackId, @PathVariable long libraryId) throws Exception {
    	return trackService.moveTrackToLibrary(trackId, libraryId);
	}

	@PatchMapping("/{id}/rating/{rating}")
	public Track updateRating(@PathVariable long id, @PathVariable int rating) throws RatingRangeException {
    	trackService.setRating(id, rating);
    	return trackService.get(id);
	}

    @GetMapping("/modifyabletags")
	public List<ModifyableTags> listModifyableTags(){
    	return Arrays.asList(ModifyableTags.values());
	}

    @GetMapping(value = "/{id}/stream", params = "deviceName")
	public ResponseEntity<Resource> stream(@PathVariable long id,
										   @RequestParam String deviceName) {
        Track track = trackService.get(id);
		String filename = track.getFilename();

		// todo check that the device is actually a device which requires converting
		Device device = deviceService.getDeviceByName(deviceName);
		byte[] convertedFile = convertService.convertFile(device, track);
		return responseEntity(filename, "audio/" + device.getFormat(), convertedFile);
    }

	@GetMapping(value = "/{id}/stream")
	public ResponseEntity<Resource> stream(@PathVariable long id) throws IOException {
		Track track = trackService.get(id);
		File file = track.getFile(localMusicFileLocation);
		String mimeType = Files.probeContentType(file.toPath());

		HttpHeaders header = new HttpHeaders();
		header.add(HttpHeaders.CONTENT_TYPE, mimeType);
		header.add(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + file.getName() + "\"");
		header.add("Cache-Control", "no-cache, no-store, must-revalidate");
		header.add("Pragma", "no-cache");
		header.add("Expires", "0");

		return ResponseEntity.ok()
			.headers(header)
			.contentLength(file.length())
			.contentType(MediaType.parseMediaType(mimeType))
			.body(new FileSystemResource(file));
	}

    @PostMapping("/upload")
	@ResponseStatus(HttpStatus.CREATED)
	public Track uploadTrack(@RequestParam MultipartFile file,
							 @RequestParam(required = false) Long existingId,
							 @RequestParam(required = false) Long libraryId) throws Exception {
		Preconditions.checkNotNull(file);
		if (existingId != null) {
			return trackService.replaceExistingTrack(file, existingId);
		} else {
			Preconditions.checkNotNull(libraryId);
			return trackService.uploadNewTrack(file, libraryId, false);
		}
	}

    @GetMapping("/{id}/art")
    public ResponseEntity<Resource> getAlbumArt(@PathVariable long id, @RequestParam(defaultValue = "0") Integer index){
        Track track = trackService.get(id);

		Optional<TrackUpdate> albumArtUpdate = updateService.getAlbumArtUpdate(id);
		if (albumArtUpdate.isPresent()) {
			TrackUpdate trackUpdate = albumArtUpdate.get();
			byte[] newAlbumArt = Base64.getDecoder().decode(trackUpdate.getNewValue());
			return responseEntity(null, ImageFormats.getMimeTypeForBinarySignature(newAlbumArt), newAlbumArt);
		} else {
			Artwork albumArt = metadataService.getAlbumArt(track.getLibraryPath(), false, index);
			return responseEntity(null, albumArt.getMimeType(), albumArt.getBinaryData());
		}
    }

	private List<Track> determineTracksToUpdateAlbumArt(long id, Boolean updateForEntireAlbum) {
		Track track = trackService.get(id);
		List<Track> tracksToUpdate;
		if (Boolean.TRUE.equals(updateForEntireAlbum)) {
			tracksToUpdate = trackService.listByAlbum(track.getAlbum(), track.getArtist(), track.getDisc_no());
		} else {
			tracksToUpdate = Collections.singletonList(track);
		}

		trackWebsocket.sendAlbumArtModificationMessage(track.getAlbum(), -1, tracksToUpdate.size());

		return tracksToUpdate;
	}

    @PostMapping("{id}/art")
	public Track setAlbumArt(@PathVariable long id,
							 @RequestParam(required = false) MultipartFile file,
							 @RequestParam(required = false) String url,
							 @RequestParam Boolean updateForEntireAlbum) throws IOException {
		List<Track> tracksToUpdate = determineTracksToUpdateAlbumArt(id, updateForEntireAlbum);

		if (file != null) {
			String originalExtension = FilenameUtils.getExtension(file.getOriginalFilename());
			File tempFile;
			if (StringUtils.isEmpty(originalExtension)) {
				byte[] fileContents = IOUtils.toByteArray(file.getInputStream());
				tempFile = createFileFromByteArray(fileContents);
			} else {
				tempFile = File.createTempFile("temp", "." + originalExtension);
				FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
			}
			for (int i = 0; i < tracksToUpdate.size(); i++) {
				Track trackToUpdate = tracksToUpdate.get(i);
				updateAlbumArt(trackToUpdate, tempFile, file.getOriginalFilename(), i, tracksToUpdate.size());
			}
			tempFile.delete();
		} else {
			byte[] fileBytes = fileService.downloadFileFromUrl(url);
			File artworkFile = createFileFromByteArray(fileBytes);
			for (int i = 0; i < tracksToUpdate.size(); i++) {
				Track trackToUpdate = tracksToUpdate.get(i);
				updateAlbumArt(trackToUpdate, artworkFile, url, i, tracksToUpdate.size());
			}
			artworkFile.delete();
		}

    	return trackService.get(id);
	}

	private void updateAlbumArt(Track trackToUpdate, File artworkFile, String albumArtSource, int i, int max) {
		updateService.queueAlbumArtUpdate(trackToUpdate.getId(), artworkFile);
		updateAlbumArtSource(trackToUpdate, albumArtSource);
		trackWebsocket.sendAlbumArtModificationMessage(trackToUpdate.getAlbum(), i, max);
	}

	private File createFileFromByteArray(byte[] fileContents) throws IOException {
		String mimeType = ImageFormats.getMimeTypeForBinarySignature(fileContents);
		String extension = "." + mimeType.replace("image/", "");
		File tempFile = File.createTempFile("temp", extension);
		tempFile.deleteOnExit();
		FileUtils.writeByteArrayToFile(tempFile, fileContents);
		return tempFile;
	}

	private void updateAlbumArtSource(Track track, String albumArtSource) {
		track.setAlbumArtSource(albumArtSource);
		track.setDateUpdated(new Date());
		track.setAlbumArtDateUpdated(new Date());
		trackService.update(track);
	}

    @PostMapping("/{id}/listened")
    public Track markTrackAsListened(@PathVariable long id, @RequestParam long deviceId){
        return trackService.markListened(id, deviceId);
    }

	@PostMapping("/{artist}/{album}/{title}/listened")
	public Track markTrackAsListened(@PathVariable String artist,
									 @PathVariable String album,
									 @PathVariable String title,
									 @RequestParam String deviceName) {
		Device device = deviceService.getOrInsert(deviceName);
		Track track = trackService.get(title, artist, album, null);
		return trackService.markListened(track.getId(), device.getId());
	}

	@PostMapping("/{id}/skipped")
	public Track markTrackAsSkipped(@PathVariable long id, @RequestParam long deviceId, @RequestParam(required = false) Double secondsPlayed) throws Exception {
    	return trackService.markSkipped(id, deviceId, secondsPlayed);
	}
}
