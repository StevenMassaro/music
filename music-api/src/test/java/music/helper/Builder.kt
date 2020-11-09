package music.helper

import music.model.DeferredTrack
import music.model.Track
import music.service.TrackServiceIT.seededMusicLibrary
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.springframework.test.util.ReflectionTestUtils
import java.io.IOException
import java.util.*

fun track(location: String = "sample1.flac"): DeferredTrack {
    val track = DeferredTrack()
    track.album = "album"
    track.album_artist = "aartist"
    track.artist = "artist"
    track.comment = "comment"
    track.dateCreated = Date()
    track.disc_no = 1L
    track.genre = "genre"
    track.location = location
    track.title = "title"
    track.track = 3L
    track.year = "1998"
    track.deletedInd = false
    track.fileLastModifiedDate = Date()
    track.hash = "1234abc"
	track.duration = 5
	track.library = seededMusicLibrary
    ReflectionTestUtils.setField(track, "musicFileSource", System.getProperty("java.io.tmpdir"))
    return track
}

@Throws(IOException::class)
fun doTrackAssertions(assertId: Boolean, baseline: Track, comparison: Track) {
    assertEquals(baseline.album, comparison.album)
    assertEquals(baseline.album_artist, comparison.album_artist)
    assertEquals(baseline.artist, comparison.artist)
    assertEquals(baseline.comment, comparison.comment)
    //        assertEquals(track1.getDateCreated(), track2.getDateCreated());
    assertNotNull(comparison.dateCreated)
    assertEquals(baseline.disc_no, comparison.disc_no)
    assertEquals(baseline.genre, comparison.genre)
    assertEquals(baseline.location, comparison.location)
    assertEquals(baseline.libraryPath, comparison.libraryPath)
    assertEquals(baseline.title, comparison.title)
    assertEquals(baseline.track, comparison.track)
    assertEquals(baseline.year, comparison.year)
    assertEquals(baseline.deletedInd, comparison.deletedInd)
    assertEquals(baseline.fileLastModifiedDate, comparison.fileLastModifiedDate)
    assertEquals(baseline.hash, comparison.hash)
    if (assertId) {
        assertEquals(baseline.id, comparison.id)
    }
}
