package music.model

import com.fasterxml.jackson.annotation.JsonIgnore
import javax.persistence.Entity
import javax.persistence.Id

@Entity
data class Library(
	@Id
	val id: Long,
	@JsonIgnore
	val subfolder: String,
	val name: String
)
