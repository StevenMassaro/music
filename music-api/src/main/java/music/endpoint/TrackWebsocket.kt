package music.endpoint

import music.model.AlbumArtUpdateStatusMessage
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller

@Controller
class TrackWebsocket {
	private val logger = LoggerFactory.getLogger(TrackWebsocket::class.java)

	@Autowired
	private lateinit var template: SimpMessagingTemplate

	fun sendAlbumArtModificationMessage(album: String, position: Int, max: Int) {
		val message = AlbumArtUpdateStatusMessage(album, position + 1, max)
		logger.trace("Sending {}", message)
		template.convertAndSend(ALBUM_ART_UPDATE_STATUS_DESTINATION, message)
	}

	companion object {
		const val ALBUM_ART_UPDATE_STATUS_DESTINATION = "/topic/art/updates"
	}
}
