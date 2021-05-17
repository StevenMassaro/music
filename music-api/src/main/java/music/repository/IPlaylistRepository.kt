package music.repository

import music.model.Playlist
import org.springframework.data.repository.CrudRepository

interface IPlaylistRepository : CrudRepository<Playlist, Long>