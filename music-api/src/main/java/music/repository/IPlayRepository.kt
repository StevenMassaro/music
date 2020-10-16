package music.repository

import music.model.Play
import org.springframework.data.repository.CrudRepository

interface IPlayRepository : CrudRepository<Play, Long> {

	fun findAllBySongId(songId: Long): List<Play>
}