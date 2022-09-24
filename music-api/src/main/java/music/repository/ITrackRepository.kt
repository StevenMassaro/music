package music.repository

import music.model.Track
import org.springframework.data.repository.CrudRepository

interface ITrackRepository: CrudRepository<Track, Long>