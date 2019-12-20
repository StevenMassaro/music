package music.service

import music.mapper.ConvertMapper
import music.model.Device
import music.model.Track
import music.settings.PrivateSettings
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.File

@Service
class ConvertService {

	private val logger = LoggerFactory.getLogger(ConvertService::class.java)

	@Autowired
	private lateinit var fileService: FileService

	@Autowired
	private lateinit var convertMapper: ConvertMapper

	@Autowired
	private lateinit var privateSettings: PrivateSettings

	fun convertFile(device: Device, track: Track): ByteArray? {
		try {
			logger.trace("Converting {}", track.location)

			/*
			ffmpeg does not like spaces in the source directory path, and I have tried numerous ways of fixing it to
			no avail. Instead, it seems much simpler to just copy the source file to the temp file location, which
			usually ensures that it won't have a space in it. At least on Linux, which is what I really care about.
			 */
			val sourceTemp = File.createTempFile("source", ".${FilenameUtils.getExtension(track.location)}")
			sourceTemp.deleteOnExit()
			FileUtils.copyFile(fileService.getFile(track.location), sourceTemp)

			// todo, figure out a way to convert the file into memory rather than on disk (likely not possible due to using ffmpeg)
			val target = File.createTempFile("example", ".${device.format}")
			logger.trace("Output file: {}", target.absolutePath)
			target.deleteOnExit()

			var cmd = "-y -i \"${sourceTemp.absolutePath}\" -ac ${device.channels} -ar ${device.sampleRate} -ab ${device.bitrate} -map_metadata 0 -vf \"scale='min(${device.artsize},iw)':'-1'\" \"${target.absolutePath}\""
			if (StringUtils.isNotEmpty(privateSettings.ffmpegPath) && !privateSettings.ffmpegPath.contains("$") && !privateSettings.ffmpegPath.contains("@")){
				cmd = "${privateSettings.ffmpegPath} $cmd"
			} else { // assume that ffmpeg is on the path
				cmd = "ffmpeg $cmd"
			}
			logger.debug("ffmpeg command: $cmd")
			val cmdLine = CommandLine.parse(cmd)
			val executor = DefaultExecutor();
			val exitValue = executor.execute(cmdLine)
			logger.debug("ffmpeg exit value: $exitValue")

			if (exitValue == 0) {
				// store hash in database for this device
				upsertHash(device.id, track.id, DigestUtils.sha512Hex(target.inputStream()))

				val fileBytes = FileUtils.readFileToByteArray(target)
				FileUtils.deleteQuietly(sourceTemp)
				FileUtils.deleteQuietly(target)
				return fileBytes
			} else {
				throw Exception("Failed to convert ${track.location} with ffmpeg exit value of $exitValue")
			}
		} catch (ex: Exception) {
			logger.error("Failed to convert ${track.location}", ex)
			return null
		}
	}

	fun getHash(deviceName: String, trackId: Long): String? = convertMapper.getHashForDeviceAndTrack(trackId, deviceName)

	private fun upsertHash(deviceId: Long, trackId: Long, hash: String) = convertMapper.upsertHash(trackId, deviceId, hash)

	/**
	 * Delete all the hashes that correspond to the specified [trackId]. This should be used
	 * when making a change to the song file (which would result in a new hash being calculated).
	 */
	fun deleteHash(trackId: Long) = convertMapper.deleteHash(trackId)

	fun deleteHash(location: String) = convertMapper.deleteHashByLocation(location)
}
