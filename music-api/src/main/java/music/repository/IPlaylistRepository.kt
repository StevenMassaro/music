package music.repository

import music.model.Playlist
import org.springframework.data.jpa.repository.JpaRepository

interface IPlaylistRepository : JpaRepository<Playlist, Long>