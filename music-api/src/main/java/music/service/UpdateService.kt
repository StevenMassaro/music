package music.service

import music.mapper.UpdateMapper
import music.model.HtmlType
import music.model.ModifyableTags
import music.model.Track
import music.model.TrackUpdate
import music.utils.FieldUtils.calculateHash
import org.jaudiotagger.tag.FieldKey
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import java.lang.Exception
import java.sql.JDBCType
import kotlin.collections.HashMap

@Service
class UpdateService @Autowired constructor(
	private val updateMapper: UpdateMapper,
	private val metadataService: MetadataService,
	private val fileService: FileService,
	private val convertService: ConvertService
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
        updateMapper.insertUpdate(id, field, newValue)
    }

    /**
     * List all the updates for a given song[id]
     */
    private fun listById(id: Long):List<TrackUpdate>{
        return updateMapper.listByTrackId(id)
    }

    /**
     * List all the updates.
     */
    private fun list(): Map<Long, List<TrackUpdate>> {
        val updates: MutableMap<Long, MutableList<TrackUpdate>> = HashMap()
        val dbUpdates = updateMapper.list();

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
	fun deleteUpdateById(id: Long) {
		updateMapper.deleteById(id)
		logger.debug("Deleted queued track update (ID: {})", id)
	}

	/**
	 * Delete a queued track update by the [songId] of the particular update.
	 */
	fun deleteUpdateBySongId(songId: Long) {
		updateMapper.deleteByTrackId(songId)
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
						deleteUpdateById(it.id)

						logger.trace("Updating field {} to {} for ID: {}", it.field, it.newValue, id)
						trackService.updateField(id, it.field, it.newValue, ModifyableTags.valueOf(it.field.toUpperCase()).sqlType);
						convertService.deleteHash(track.id)

						trackService.updateHashOfTrack(track.location, id)
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
		return updateMapper.count()
	}

    /**
     * Apply updates to the provided [tracks]
     */
    fun applyUpdates(tracks: List<Track>): List<Track> {
        val updates = list()

        tracks.forEach { t ->
            val trackUpdates = updates.getOrDefault(t.id, emptyList())
            applyUpdates(t, trackUpdates)
        }

        return tracks
    }

    /**
     * Apply updates to the provided [track]
     */
    fun applyUpdates(track: Track?): Track? {
        if (track != null) {
            applyUpdates(track, listById(track.id))
        }
        return track
    }

    /**
     * Apply the provided [updates] to the [track]
     */
    private fun applyUpdates(track: Track?, updates: List<TrackUpdate>) {
        if (track != null) {
            for (trackUpdate in updates) {
                // todo find the field using correct name
                val field = ReflectionUtils.findField(Track::class.java, trackUpdate.field)
                if (field != null) {
					val tag = ModifyableTags.valueOf(trackUpdate.field.toUpperCase())
					ReflectionUtils.makeAccessible(field)
					if(tag.htmlType == HtmlType.number){
						ReflectionUtils.setField(field, track, trackUpdate.newValue.toLong())
					} else {
						ReflectionUtils.setField(field, track, trackUpdate.newValue)
					}
                } else {
                    logger.error(String.format("Failed to reflectively update field %s on track %s", trackUpdate.field, track.getId()))
                }
            }
        }
    }
}
