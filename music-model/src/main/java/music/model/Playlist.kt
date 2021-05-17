package music.model

import java.util.*
import javax.persistence.*

@Entity
open class Playlist(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	var id: Long? = null,
	var name: String,
	@OneToMany(cascade = arrayOf(CascadeType.ALL))
	@JoinColumn(name="playlistId", referencedColumnName="id")
	var trackIds: MutableList<PlaylistTrack> = mutableListOf(),
	var dateCreated: Date = Date(),
	var dateUpdated: Date? = null)