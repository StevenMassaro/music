package music.repository

import jakarta.transaction.Transactional
import music.model.TrackUpdate
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
@Transactional
interface IUpdateRepository : JpaRepository<TrackUpdate, Long> {
	fun findBySongIdAndField(songId: Long, field: String) : Optional<TrackUpdate>
	fun deleteBySongId(songId: Long)
}