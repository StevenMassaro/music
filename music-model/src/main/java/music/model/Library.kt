package music.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

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
