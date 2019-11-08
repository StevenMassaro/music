package music.service

import music.model.DeferredTrack
import music.settings.PrivateSettings
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.TagException
import org.jaudiotagger.tag.datatype.Artwork
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.util.ArrayList

@Service
class MetadataService @Autowired constructor(val fileService: FileService, val privateSettings: PrivateSettings) {
    private val logger = LoggerFactory.getLogger(MetadataService::class.java)

    @Throws(TagException::class, ReadOnlyFileException::class, CannotReadException::class, InvalidAudioFrameException::class, IOException::class)
    fun getTracks(): List<DeferredTrack> {
        val files = ArrayList(fileService.listMusicFiles())
        logger.info("Found {} files in music directory.", files.size)

        val tracks = ArrayList<DeferredTrack>()

        for (i in files.indices) {
            val file = files[i]
            logger.debug("Processing file {} of {}: {}", i + 1, files.size, file.name)
            val audioFile = AudioFileIO.read(file)
            val tag = audioFile.tag
            val header = audioFile.audioHeader
            try {
                val track = DeferredTrack(tag, header, file.absolutePath.replace(privateSettings.localMusicFileLocation!!, ""), file, privateSettings.localMusicFileLocation)
                tracks.add(track)
            } catch (e: Exception) {
                logger.error(String.format("Failed to parse tag for metadata for file %s", file.absolutePath), e)
            }

        }
        return tracks
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
        val audioFile = AudioFileIO.read(file)
        val tag = audioFile.tag

        return tag.artworkList.get(index)
    }
}