package music.endpoint

import music.model.SmartPlaylist
import music.service.SmartPlaylistService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/playlist/smart")
class SmartPlaylistEndpoint {

	@Autowired
	private lateinit var smartPlaylistService: SmartPlaylistService

	@GetMapping
	fun list() = smartPlaylistService.list()

	@GetMapping("/{id}")
	fun get(@PathVariable id: Long) = smartPlaylistService.get(id)

	@DeleteMapping("/{id}")
	fun delete(@PathVariable id: Long): SmartPlaylist {
		val pl = smartPlaylistService.get(id)
		smartPlaylistService.delete(id)
		return pl
	}

	@PostMapping
	fun create(@RequestBody smartPlaylist: SmartPlaylist): SmartPlaylist {
		smartPlaylistService.insert(smartPlaylist.name, smartPlaylist.dynamicSql)
		return smartPlaylistService.getByName(smartPlaylist.name)
	}

	@PatchMapping
	fun update(@RequestBody smartPlaylist: SmartPlaylist): SmartPlaylist {
		smartPlaylistService.updateByName(smartPlaylist.name, smartPlaylist.dynamicSql)
		return smartPlaylistService.getByName(smartPlaylist.name)
	}
}
