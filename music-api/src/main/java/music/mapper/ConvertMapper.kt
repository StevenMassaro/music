package music.mapper

import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface ConvertMapper {

	fun getHashForDeviceAndTrack(trackId: Long, deviceName: String): String?
	fun upsertHash(trackId: Long, deviceId: Long, hash: String)
	fun deleteHash(trackId: Long)
	fun deleteHashByLocation(location: String)
}
