package music.repository

import music.model.Library
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ILibraryRepository : JpaRepository<Library, Long> {
	fun findByName(name: String): Library
}