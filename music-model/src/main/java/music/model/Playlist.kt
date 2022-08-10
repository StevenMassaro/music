package music.model

import java.util.*
import javax.persistence.*

@Entity
open class Playlist(
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	open var id: Long = -1,
	open var name: String,
	@OneToMany(cascade = arrayOf(CascadeType.ALL))
	@JoinColumn(name="playlistId", referencedColumnName="id")
	open var trackIds: MutableList<PlaylistTrack> = mutableListOf(),
	open var dateCreated: Date = Date(),
	open var dateUpdated: Date? = null)