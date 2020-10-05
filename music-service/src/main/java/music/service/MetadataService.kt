package music.service

import com.google.common.cache.CacheBuilder
import music.model.DeferredTrack
import music.model.Track
import music.settings.PrivateSettings
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
class MetadataService @Autowired constructor(val fileService: FileService, val privateSettings: PrivateSettings) {
    private val logger = LoggerFactory.getLogger(MetadataService::class.java)
	private val audioFileCache = CacheBuilder.newBuilder()
		.maximumSize(50)
		.build<File, AudioFile>()

    @Throws(TagException::class, ReadOnlyFileException::class, CannotReadException::class, InvalidAudioFrameException::class, IOException::class)
    fun getTracks(): List<DeferredTrack> {
        val files = ArrayList(fileService.listMusicFiles())
        logger.info("Found {} files in music directory.", files.size)

        val tracks = ArrayList<DeferredTrack>()

        for (i in files.indices) {
            val file = files[i]
            logger.debug("Processing file {} of {}: {}", i + 1, files.size, file.name)
            val parsed = parseMetadata(file)
			if (parsed != null){
				tracks.add(parsed)
			}
        }
        return tracks
    }

	/**
	 * Parse the ID3 tag in the [file] and return the metadata from that file. Will catch all exceptions and return null
	 * in the event of an exception.
	 */
	fun parseMetadata(file:File) : DeferredTrack? {
		val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
		val tag = audioFile.tag
		val header = audioFile.audioHeader
		return try {
			DeferredTrack(tag, header, file.absolutePath.replace(privateSettings.localMusicFileLocation!!, ""), file, privateSettings.localMusicFileLocation)
		} catch (e: Exception) {
			logger.error(String.format("Failed to parse tag for metadata for file %s", file.absolutePath), e)
			null
		}
	}

    /**
     * Get album art from the file's [location]. Optionally specify the [index] of the album art you wish to retrieve.
     * Optionally specify whether the [location] is a full path with [isLocationFullPath]. If false, the full path
     * will be determined automatically.
     */
    @JvmOverloads
    fun getAlbumArt(location: String, isLocationFullPath: Boolean = false, index: Int = 0): Artwork {
        if (isLocationFullPath) {
            return getAlbumArt(File(location), index)
        } else {
            return getAlbumArt(fileService.getFile(location), index)
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
		val file = fileService.getFile(track.location)
		if (file != null && file.exists()) {
			val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
			val tag = audioFile.tag
			tag.setField(field, newValue)
			audioFile.commit()
		}
	}

	/**
	 * Update the artwork of the track at the specified [location], using art downloaded from the [url]. The [url]
	 * should be the direct link to an image.
	 */
	fun updateArtwork(location: String, url: String) {
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

		updateArtwork(location, tempArtFile)
		tempArtFile.delete()
	}

	/**
	 * Update the artwork of the track at the specified [location], using the [newArt].
	 */
	fun updateArtwork(location:String, newArt:File){
		val file = fileService.getFile(location)
		if (file != null && file.exists()) {
			val audioFile = audioFileCache.get(file) { AudioFileIO.read(file) }
			val tag = audioFile.tag
			val art = Artwork()
			art.setFromFile(newArt)
			tag.setField(art)
			audioFile.commit()
		}
	}
}
