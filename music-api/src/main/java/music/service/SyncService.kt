package music.service

import music.endpoint.AdminEndpoint
import music.exception.TaskInProgressException
import music.model.SyncResult
import music.repository.ILibraryRepository
import org.jaudiotagger.audio.exceptions.CannotReadException
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException
import org.jaudiotagger.tag.TagException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.IOException
import java.util.concurrent.atomic.AtomicBoolean

@Service
class SyncService @Autowired constructor(private val metadataService: MetadataService,
										 private val trackService: TrackService,
										 private val libraryRepository: ILibraryRepository) {

	private val logger = LoggerFactory.getLogger(AdminEndpoint::class.java)
	private val currentlySyncing: AtomicBoolean = AtomicBoolean(false)


	@Throws(ReadOnlyFileException::class, CannotReadException::class, TagException::class, InvalidAudioFrameException::class, IOException::class)
	fun syncTracksToDb(forceUpdates: Boolean = false, libraryId: Long): SyncResult {
		checkNotSyncing()
		try {
			logger.info("Begin database sync")
			currentlySyncing.set(true)
			val syncResult = SyncResult()
			val library = libraryRepository.findById(libraryId).get()
			val files = metadataService.getTracks(library)
			trackService.upsertTracks(files, syncResult, forceUpdates)
			trackService.deleteOrphanedTracksMetadata(files, syncResult, library)
			logger.info("Finished database sync")
			logger.debug("Sync results: {}", syncResult)
			return syncResult
		} finally {
			currentlySyncing.set(false)
		}
	}

	private fun checkNotSyncing() {
		if (currentlySyncing.get()) {
			throw TaskInProgressException("sync")
		}
	}

}