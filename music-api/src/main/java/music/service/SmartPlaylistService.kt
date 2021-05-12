package music.service

import music.mapper.PlaylistMapper
import music.model.SmartPlaylist
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class SmartPlaylistService @Autowired constructor(val playlistMapper: PlaylistMapper){

	fun insert(name: String, dynamicSql: String) {
		assertSafeSql(dynamicSql, "drop", "select", "delete", "update", "truncate", "insert", "--", ";")
		playlistMapper.insertSmartPlaylist(name, dynamicSql)
	}

	private fun assertSafeSql(sql: String, vararg forbiddenWords: String) = forbiddenWords.forEach {
		require(!sql.toLowerCase().contains(it)) {"Dynamic SQL not allowed to contain $it"}
	}

	fun list(): List<SmartPlaylist> = playlistMapper.listSmartPlaylist()

	fun delete(id: Long) = playlistMapper.deleteSmartPlaylistById(id)

	fun get(id: Long) = playlistMapper.getSmartPlaylist(id)

	fun getByName(name: String) = playlistMapper.getSmartPlaylistByName(name)

	fun updateByName(name: String, dynamicSql: String) = playlistMapper.updateSmartPlaylistByName(name, dynamicSql)
}
