package music.service

import music.endpoint.AdminEndpoint
import music.exception.TaskInProgressException
import music.model.SyncResult
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.TagException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.ArrayList
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SyncService @Autowired constructor(private val metadataService: MetadataService, private val trackService: TrackService) {

	private val logger = LoggerFactory.getLogger(AdminEndpoint::class.java)
	private val currentlySyncing: AtomicBoolean = AtomicBoolean(false);

	@Throws(ReadOnlyFileException::class, CannotReadException::class, TagException::class, InvalidAudioFrameException::class, IOException::class)
	fun syncTracksToDb(forceUpdates: Boolean = false): SyncResult {
		checkNotSyncing()
		try {
			logger.info("Begin database sync")
			currentlySyncing.set(true)
			val syncResult = SyncResult(ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList(), ArrayList())
			val files = metadataService.getTracks()
			trackService.upsertTracks(files, syncResult, forceUpdates)
			trackService.deleteOrphanedTracksMetadata(files, syncResult)
			logger.info("Finished database sync")
			currentlySyncing.set(false)
			return syncResult
		} catch (e: Exception) {
			currentlySyncing.set(false)
			throw e
		}
	}

	private fun checkNotSyncing() {
		if (currentlySyncing.get()) {
			throw TaskInProgressException("sync")
		}
	}

}