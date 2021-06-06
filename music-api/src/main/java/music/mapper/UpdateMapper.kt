package music.mapper

import music.model.TrackUpdate
import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface UpdateMapper {

    fun insertUpdate(@Param("songId") songId: Long, @Param("field") field: String, @Param("newValue") newValue: String)
    fun list():List<TrackUpdate>
	fun count():Long
	fun deleteById(id:Long)
	fun deleteByTrackId(trackId:Long)
}
