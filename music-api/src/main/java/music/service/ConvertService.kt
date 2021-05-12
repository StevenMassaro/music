package music.service

import music.mapper.ConvertMapper
import music.model.Device
import music.model.Track
import music.settings.PrivateSettings
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.io.FileUtils
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

	fun reduceFlacFileSize(track: Track) {
		// confirm that file is actually a flac
		track.extension.equals("flac", true);

		// confirm that track hasn't already been converted to level 12

		// convert orig file to flac compression level 12
		// https://askubuntu.com/a/758299
		//		ffmpeg -i input.wav -c:a flac -compression_level 12 output.flac

		// convert orig file to wav
		//		ffmpeg -i orig.flac orig.wav

		// convert compressed-12 file to wav
		// 		ffmpeg -i output.flac output.wav

		// compare hashes of two wav files (note that this conversion to wav will go to a max of 16 bits, so 24 bit files get dropped down to 16)
		// https://superuser.com/a/532223
		//		ffmpeg -loglevel error -i FILE -map 0 -f hash -

		// if they match
		// delete wav files
		// replace original file with newly compressed one
		// mark track as converted to flac type 12
	}

	fun convertFile(device: Device, track: Track): ByteArray? {
		try {
			logger.trace("Converting {}", track.libraryPath)

			/*
			ffmpeg does not like spaces in the source directory path, and I have tried numerous ways of fixing it to
			no avail. Instead, it seems much simpler to just copy the source file to the temp file location, which
			usually ensures that it won't have a space in it. At least on Linux, which is what I really care about.
			 */
			val sourceTemp = File.createTempFile("source", ".${track.extension}")
			sourceTemp.deleteOnExit()
			FileUtils.copyFile(fileService.getFile(track.libraryPath), sourceTemp)

			// todo, figure out a way to convert the file into memory rather than on disk (likely not possible due to using ffmpeg)
			val target = File.createTempFile("example", ".${device.format}")
			logger.trace("Output file: {}", target.absolutePath)
			target.deleteOnExit()

			val cmdLine = if(StringUtils.isNotEmpty(privateSettings.ffmpegPath) && !privateSettings.ffmpegPath.contains("$") && !privateSettings.ffmpegPath.contains("@")){
				CommandLine(privateSettings.ffmpegPath)
			} else { // assume that ffmpeg is on the path
				CommandLine("ffmpeg")
			}

			cmdLine.addArgument("-y")
			cmdLine.addArgument("-i")
			cmdLine.addArgument(sourceTemp.absolutePath)
			cmdLine.addArgument("-ac")
			cmdLine.addArgument(device.channels.toString())
			cmdLine.addArgument("-ar")
			cmdLine.addArgument(device.sampleRate.toString())
			cmdLine.addArgument("-ab")
			cmdLine.addArgument(device.bitrate.toString())
			cmdLine.addArgument("-map_metadata")
			cmdLine.addArgument(0.toString())
			cmdLine.addArgument("-vf")
			cmdLine.addArgument("scale='min(${device.artsize},iw)':'-1'", false)
			cmdLine.addArgument(target.absolutePath)

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
				throw Exception("Failed to convert ${track.libraryPath} with ffmpeg exit value of $exitValue")
			}
		} catch (ex: Exception) {
			logger.error("Failed to convert ${track.libraryPath}", ex)
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
