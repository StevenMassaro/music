package music.mapper

import music.model.TrackUpdate
import org.apache.ibatis.annotations.Mapper

@Mapper
interface UpdateMapper {

    fun insertUpdate(songId: Long, field: String, newValue: String)
    fun listByTrackId(songId: Long):List<TrackUpdate>
    fun list():List<TrackUpdate>
	fun count():Long
	fun deleteById(id:Long)
}