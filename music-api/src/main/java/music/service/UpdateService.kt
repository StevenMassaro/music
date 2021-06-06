package music.service

import music.model.ModifyableTags
import music.model.TrackUpdate
import music.repository.IUpdateRepository
import org.jaudiotagger.tag.FieldKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*
import kotlin.collections.HashMap

@Service
class UpdateService @Autowired constructor(
	private val metadataService: MetadataService,
	private val convertService: ConvertService,
	private val updateRepository: IUpdateRepository
) {
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
		val trackUpdate: Optional<TrackUpdate> = updateRepository.findBySongIdAndField(id, field)
		if (trackUpdate.isPresent) {
			val newUpdate = trackUpdate.get();
			newUpdate.newValue = newValue
			updateRepository.saveAndFlush(newUpdate)
		} else {
			updateRepository.saveAndFlush(TrackUpdate(null, id, field, newValue, 1))
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
						logger.trace("Applying update to disk: {}", it.toString())
						metadataService.updateTrackField(track, FieldKey.valueOf(it.field.toUpperCase()), it.newValue)
						deleteUpdateById(it.id!!)

						logger.trace("Updating field {} to {} for ID: {}", it.field, it.newValue, id)
						trackService.updateField(id, it.field, it.newValue, ModifyableTags.valueOf(it.field.toUpperCase()).sqlType);
						convertService.deleteHash(track.id)

						trackService.updateHashOfTrack(track.libraryPath, id)
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
