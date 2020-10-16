package music.repository

import music.model.PlayCount
import org.springframework.data.repository.CrudRepository

interface IPlayCountRepository : CrudRepository<PlayCount, Long> {

}