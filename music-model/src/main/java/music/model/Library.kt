package music.model

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
data class Library(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	val id: Long = -1,
	@JsonIgnore
	val subfolder: String,
	val name: String,
	/**
	 * /ARTIST/ALBUM/TRACK - TITLE
	 */
	@JsonIgnore
	val trackNamePattern: String
)
