package music.service

import music.model.Device
import music.model.Track
import org.apache.commons.exec.CommandLine
import org.apache.commons.exec.DefaultExecutor
import org.apache.commons.io.FileUtils
import org.apache.commons.lang3.StringUtils
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.io.File

@Service
class ConvertService {

	private val logger = LoggerFactory.getLogger(ConvertService::class.java)

	@Autowired
	private lateinit var fileService: FileService

	@Value("\${music.ffmpeg.path}")
	private val ffmpegPath: String? = null

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

			val cmdLine = if(StringUtils.isNotEmpty(ffmpegPath) && !ffmpegPath!!.contains("$") && !ffmpegPath.contains("@")){
				CommandLine(ffmpegPath)
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
}
