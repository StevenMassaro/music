package music.mapper

import org.apache.ibatis.annotations.Mapper
import org.apache.ibatis.annotations.Param
import org.springframework.stereotype.Repository
import java.util.*

@Mapper
@Repository
interface SkipMapper {
	fun insertSkip(@Param("id") id: Long,
				   @Param("skipdate") skipDate: Date,
				   @Param("deviceId") deviceId: Long,
				   @Param("imported") imported: Boolean,
				   @Param("secondsPlayed") secondsPlayed: Double?)

	fun deleteSkipCounts(@Param("id") id: Long)

	fun deleteBySongId(id: Long)
}