package music.service

import music.mapper.UpdateMapper
import music.model.TrackUpdate
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class UpdateService @Autowired constructor(private val updateMapper: UpdateMapper) {

    /**
     * Update the specified tracks field with the new value. These updates do not occur immediately.
     * Rather, they are queued in the database and persisted to the ID3 tags at some other time.
     * @param id song id
     * @param field ID3 tag field to modify
     * @param value new value to set
     * @return updated track
     */
    fun queueTrackUpdate(id: Long, field: String, newValue: String) {
        updateMapper.insertUpdate(id, field, newValue)
    }

    fun listById(id: Long):List<TrackUpdate>{
        return updateMapper.listByTrackId(id)
    }
}