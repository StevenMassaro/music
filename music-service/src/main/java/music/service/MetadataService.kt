package music.service

import com.google.common.cache.CacheBuilder
import music.model.DeferredTrack
import music.model.Library
import music.model.Track
import okhttp3.OkHttpClient
import okhttp3.Request
import org.apache.commons.io.FilenameUtils
import org.apache.commons.io.IOUtils
import org.jaudiotagger.audio.AudioFile
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.FieldKey
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.datatype.Artwork
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit


@Service
class MetadataService @Autowired constructor(private val fileService: FileService) : AbstractService() {
    private val logger = LoggerFactory.getLogger(MetadataService::class.java)
	private val audioFileCache = CacheBuilder.newBuilder()
		.maximumSize(50)
		.build<File, AudioFile>()

    @Throws(TagException::class, ReadOnlyFileException::class, CannotReadException::class, InvalidAudioFrameException::class, IOException::class)
    fun getTracks(library: Library): List<DeferredTrack> {
        val files = ArrayList(fileService.listMusicFiles(library))
        logger.info("Found {} files in music directory.", files.size)

        val tracks = ArrayList<DeferredTrack>()

        for (i in files.indices) {
            val file = files[i]
            logger.debug("Processing file {} of {}: {}", i + 1, files.size, file.name)
            val parsed = parseMetadata(file, library)
			if (parsed != null){
				tracks.add(parsed)
			}
        }
        return tracks
    }

	/**
	 * Given a [file], generate a location from the file's absolute path and [library].
	 */
	fun generateLocationFromFilePath(file: File, library: Library) : String {
		/*
		* We always want the track location to start with a slash, for backwards compatibility with existing data.
		* Sometimes the directories are nested enough:
		* 	c:/whateverfolder/localmusicfilelocation/librarysubfolder/album/artist/track.flac
		* that after removing the beginning parts of the path we are left with:
		*   //album/artist/track.flac
		*
		* So we remove the first separator. But we not have two slashes there (if the music file is in the root
		* directory for some reason, so if we remove it and the string no longer starts with a slash, add one
		*/
		var location = file.absolutePath
			.replaceFirst(localMusicFileLocation, "")
			.replaceFirst(File.separator, "")
			.replaceFirst(library.subfolder, "")
		if (!location.startsWith(File.separator)) {
			location = File.separator + location
		}
		return location
	}

	/**
	 * Parse the ID3 tag in the [file] and return the metadata from that file. Will catch all exceptions and return null
	 * in the event of an exception.
	 */
	fun parseMetadata(file:File, library:Library) : DeferredTrack? {
		val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
		val tag = audioFile.tag
		val header = audioFile.audioHeader
		val location = generateLocationFromFilePath(file, library)
		return try {
			DeferredTrack(
				tag,
				header,
				location,
				file,
				localMusicFileLocation,
				library)
		} catch (e: Exception) {
			logger.error("Failed to parse tag for metadata for file {}", file.absolutePath, e)
			null
		}
	}

    /**
     * Get album art from the file's [libraryPath]. Optionally specify the [index] of the album art you wish to retrieve.
     * Optionally specify whether the [libraryPath] is a full path with [isLocationFullPath]. If false, the full path
     * will be determined automatically.
     */
    @JvmOverloads
    fun getAlbumArt(libraryPath: String, isLocationFullPath: Boolean = false, index: Int = 0): Artwork {
        if (isLocationFullPath) {
            return getAlbumArt(File(libraryPath), index)
        } else {
            return getAlbumArt(fileService.getFile(libraryPath), index)
        }
    }

    /**
     * Get album art from the [file]. Optionally specify the [index] of the album art you wish to retrieve.
     */
    fun getAlbumArt(file: File, index: Int = 0): Artwork {
		val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
        val tag = audioFile.tag

        return tag.artworkList.get(index)
    }

	/**
	 * Update the [track]'s [field] to the [newValue], persisting the change to the ID3 tag on disk.
	 */
	@Throws(TagException::class, ReadOnlyFileException::class, CannotReadException::class, InvalidAudioFrameException::class, IOException::class)
	fun updateTrackField(track: Track, field: FieldKey, newValue: String) {
		val file = track.getFile(localMusicFileLocation)
		if (file != null && file.exists()) {
			val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
			val tag = audioFile.tag
			tag.setField(field, newValue)
			audioFile.commit()
		}
	}

	/**
	 * Update the artwork of the track at the specified [libraryPath], using art downloaded from the [url]. The [url]
	 * should be the direct link to an image.
	 */
	fun updateArtwork(libraryPath: String, url: String) {
		// todo validate that the url actually points to an image
		val tempArtFile = File.createTempFile("artwork", ".${FilenameUtils.getExtension(url)}")
		tempArtFile.deleteOnExit()

		val client = OkHttpClient().newBuilder()
			.connectTimeout(5, TimeUnit.SECONDS)
			.callTimeout(15, TimeUnit.SECONDS)
			.readTimeout(15, TimeUnit.SECONDS)
			.writeTimeout(15, TimeUnit.SECONDS)
			.build()
		val request = Request.Builder()
			.url(url)
			.build()

		client.newCall(request).execute().use { IOUtils.copy(it.body!!.byteStream(), tempArtFile.outputStream()) }

		updateArtwork(libraryPath, tempArtFile)
		tempArtFile.delete()
	}

	/**
	 * Update the artwork of the track at the specified [libraryPath], using the [newArt].
	 */
	fun updateArtwork(libraryPath:String, newArt:File){
		val file = fileService.getFile(libraryPath)
		if (file != null && file.exists()) {
			val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
			val tag = audioFile.tag
			val art = Artwork.createArtworkFromFile(newArt)
			tag.setField(art)
			audioFile.commit()
		}
	}
}
