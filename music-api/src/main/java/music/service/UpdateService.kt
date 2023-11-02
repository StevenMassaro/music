package music.service

import music.model.ModifyableTags
import music.model.TrackUpdate
import music.repository.IUpdateRepository
import org.apache.commons.io.FileUtils
import org.jaudiotagger.tag.FieldKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File
import java.util.*

@Service
class UpdateService @Autowired constructor(
	private val metadataService: MetadataService,
	private val convertService: ConvertService,
	private val updateRepository: IUpdateRepository
) : AbstractService() {
	private val logger = LoggerFactory.getLogger(UpdateService::class.java)

    /**
     * Update the specified tracks field with the new value. These updates do not occur immediately.
     * Rather, they are queued in the database and persisted to the ID3 tags at some other time.
     * @param id song id
     * @param field ID3 tag field to modify
     * @param value new value to set
     * @return updated track
     */
    fun queueTrackUpdate(id: Long, field: String, newValue: String) {
        requireNotNull(ModifyableTags.values().find { it.propertyName == field }, { "Supplied field $field is not modifyable." })
		queueUpdate(id, field, newValue)
    }

	fun queueAlbumArtUpdate(id: Long, artworkFile: File) {
		val fileByteArray = FileUtils.readFileToByteArray(artworkFile)
		val base64Artwork = Base64.getEncoder().encodeToString(fileByteArray)
		queueUpdate(id, ModifyableTags.ALBUM_ART_FIELD_KEY, base64Artwork, 2L)
	}

	private fun queueUpdate(id: Long, field: String, newValue: String, updateType: Long = 1) {
		val trackUpdate: Optional<TrackUpdate> = updateRepository.findBySongIdAndField(id, field)
		if (trackUpdate.isPresent) {
			val newUpdate = trackUpdate.get();
			newUpdate.newValue = newValue
			updateRepository.saveAndFlush(newUpdate)
		} else {
			updateRepository.saveAndFlush(TrackUpdate(null, id, field, newValue, updateType))
		}
	}

    /**
     * List all the updates.
     */
    private fun list(): Map<Long, List<TrackUpdate>> {
        val updates: MutableMap<Long, MutableList<TrackUpdate>> = HashMap()
        val dbUpdates = updateRepository.findAll()

        for (dbupdate in dbUpdates) {
            val existing = updates.getOrDefault(dbupdate.songId, mutableListOf())
            existing.add(dbupdate)
            updates.put(dbupdate.songId, existing)
        }
        return updates
    }

	fun getAlbumArtUpdate(id: Long): Optional<TrackUpdate> {
		return updateRepository.findBySongIdAndField(id, ModifyableTags.ALBUM_ART_FIELD_KEY);
	}

	/**
	 * Delete a queued track update by the [id] of the particular update (not the track ID).
	 */
	private fun deleteUpdateById(id: Long) {
		updateRepository.deleteById(id)
		logger.debug("Deleted queued track update (ID: {})", id)
	}

	/**
	 * Delete a queued track update by the [songId] of the particular update.
	 */
	fun deleteUpdateBySongId(songId: Long) {
		updateRepository.deleteBySongId(songId)
		logger.debug("Deleted queued track update (Song ID: {})", songId)
	}

	/**
	 * Apply all the queued updates to the files on disk. This silently updates the fields in the track table, and
	 * updates the hash of the file in the database (which is necessary because the file itself is modified).
	 */
	fun applyUpdatesToDisk(trackService: TrackService): Map<Long, List<TrackUpdate>> {
		// todo, the TrackService should not be a param, but this bypasses a circular dependency that was annoying
		val updates = list();
		updates.forEach { (id, trackUpdates) ->
			if (trackUpdates.isNotEmpty()) {
				val track = trackService.get(id)
				trackUpdates.forEach {
					try {
						// Album art update
						if (it.updateType == 2L) {
							logger.trace("Applying album art update to disk: {}", track.id)
							val decodedArtworkFile = Base64.getDecoder().decode(it.newValue)
							val tempArtworkFile = File.createTempFile("temp-artwork-file", ".jpg")
							FileUtils.writeByteArrayToFile(tempArtworkFile, decodedArtworkFile)
							metadataService.updateArtwork(track.libraryPath, tempArtworkFile)
						} else {
							val modifyableTag = it.getModifyableTag()!!
							logger.trace("Applying update to disk: {}", it.toString())
							metadataService.updateTrackField(track, FieldKey.valueOf(it.field.toUpperCase()), it.newValue)

							logger.trace("Updating field {} to {} for ID: {}", it.field, it.newValue, id)
							modifyableTag.updateModel(track, it.newValue)
						}
						deleteUpdateById(it.id!!)
						track.recalculateHash(localMusicFileLocation)
						trackService.update(track)
					} catch (e: Exception) {
						logger.error("Failed to apply update to disk: $it", e)
					}
				}
			}
		}
		return updates
	}

	/**
	 * Count how many updates are queued.
	 */
	fun count(): Long {
		return updateRepository.count()
	}
}
