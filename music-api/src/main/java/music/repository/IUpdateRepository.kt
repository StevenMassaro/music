package music.repository

import music.model.TrackUpdate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface IUpdateRepository : JpaRepository<TrackUpdate, Long> {
	fun findBySongIdAndField(songId: Long, field: String) : Optional<TrackUpdate>
	fun deleteBySongId(songId: Long)
}