package music.endpoint

import music.service.ConvertService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/convert")
class ConvertEndpoint {

	@Autowired
	private lateinit var convertService: ConvertService

	/**
	 * When a file is converted and sent back to the client, the hash of that file is stored
	 * locally on the server. Then, when another sync is done, the hash of the file on the
	 * client is compared to the hash returned from this function. If the file has been
	 * modified on the server since the conversion occurred, null will be returned.
	 */
	@GetMapping("/{trackId}/hash")
	fun getHashOfConvertedDeviceFile(@PathVariable trackId: Long, @RequestParam deviceName: String?): String? =
		convertService.getHash(deviceName!!, trackId)
}
