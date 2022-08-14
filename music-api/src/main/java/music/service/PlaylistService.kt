package music.service

import music.exception.TrackAlreadyExistsException
import music.model.Playlist
import music.model.PlaylistTrack
import music.repository.IPlaylistRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
class PlaylistService {

	@Autowired
	private lateinit var playlistRepository: IPlaylistRepository

	fun list() : List<Playlist> = playlistRepository.findAll(Sort.by(Playlist::name.name)).toList();

	fun create(name:String) : Playlist = playlistRepository.save(Playlist(-1, name))

	fun getById(id: Long) : Optional<Playlist> = playlistRepository.findById(id)

	fun addTrack(id: Long, trackId: Long): Playlist {
		val playlist = getById(id).get()
		val modDate = Date()
		val newPlaylistTrack = PlaylistTrack(id, trackId, null, modDate)
		if (playlist.trackIds.contains(newPlaylistTrack)) {
			throw TrackAlreadyExistsException()
		} else {
			playlist.trackIds.add(newPlaylistTrack)
			playlist.dateUpdated = modDate
			return playlistRepository.save(playlist)
		}
	}
}