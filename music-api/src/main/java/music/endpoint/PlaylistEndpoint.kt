package music.endpoint

import music.model.Playlist
import music.service.PlaylistService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RequestMapping("/playlist")
@RestController
class PlaylistEndpoint {

	@Autowired
	private lateinit var playlistService: PlaylistService

	@GetMapping
	fun list() : List<Playlist> = playlistService.list()

	@PostMapping
	fun create(@RequestBody playlist: Playlist) = playlistService.create(playlist.name)

	@GetMapping("/{id}")
	fun getById(@PathVariable id: Long) = playlistService.getById(id)

	@PatchMapping("/{id}")
	fun addTrackToPlaylist(@PathVariable id:Long, @RequestParam(required = true) trackId: Long) = playlistService.addTrack(id, trackId)
}