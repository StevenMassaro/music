package music.repository

import music.model.Skip
import org.springframework.data.repository.CrudRepository

interface ISkipRepository : CrudRepository<Skip, Long> {
	fun findAllBySongId(songId:Long):List<Skip>
}