package music.mapper

import music.model.SmartPlaylist
import org.apache.ibatis.annotations.Mapper
import org.springframework.stereotype.Repository

@Mapper
@Repository
interface PlaylistMapper {

	fun insertSmartPlaylist(name: String, dynamicSql: String)
	fun listSmartPlaylist(): List<SmartPlaylist>
	fun deleteSmartPlaylistById(id: Long)
	fun getSmartPlaylist(id: Long): SmartPlaylist
	fun getSmartPlaylistByName(name: String): SmartPlaylist
	fun updateSmartPlaylistByName(name: String, dynamicSql: String)
}
