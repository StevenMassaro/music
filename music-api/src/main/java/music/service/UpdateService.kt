package music.service

import music.mapper.UpdateMapper
import music.model.ModifyableTags
import music.model.Track
import music.model.TrackUpdate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.util.ReflectionUtils
import kotlin.collections.HashMap

@Service
class UpdateService @Autowired constructor(private val updateMapper: UpdateMapper) {
    private val logger = LoggerFactory.getLogger(TrackService::class.java)

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
                    // todo correctly cast the new value
                    ReflectionUtils.makeAccessible(field)
                    ReflectionUtils.setField(field, track, trackUpdate.newValue)
                } else {
                    logger.error(String.format("Failed to reflectively update field %s on track %s", trackUpdate.field, track.getId()))
                }
            }
        }
    }
}