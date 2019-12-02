package music.service

import junit.framework.Assert.assertTrue
import music.model.SmartPlaylist
import org.junit.Test
import org.springframework.beans.factory.annotation.Autowired
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

open class SmartPlaylistServiceIT : IntegrationTestBase() {

	@Autowired
	lateinit var smartPlaylistService: SmartPlaylistService

	@Test
	fun testInsertListGetUpdateDelete() {
		val name = "test"
		var sql = "year > 1990"
		smartPlaylistService.insert(name, sql)

		var playlists = smartPlaylistService.list();
		assertEquals(1, playlists.size)
		doAssertions(name, sql, playlists.get(0))

		var playlist = smartPlaylistService.get(playlists.get(0).id)
		doAssertions(name, sql, playlist)
		assertEquals(playlists.get(0).id, playlist.id)
		assertEquals(playlists.get(0).dateCreated, playlist.dateCreated)

		sql = "CAST(year as INT) > 1990"
		smartPlaylistService.updateByName(playlist.name, sql)

		playlist = smartPlaylistService.get(playlists.get(0).id)
		doAssertions(name, sql, playlist)
		assertNotNull(playlist.dateUpdated)

		smartPlaylistService.delete(playlists.get(0).id)
		playlists = smartPlaylistService.list()
		assertTrue(playlists.isEmpty())
	}

	@Test(expected = IllegalArgumentException::class)
	fun testInvalidSql(){
		smartPlaylistService.insert("test", "drop table hello world cascade")
	}

	private fun doAssertions(expectedName: String, expectedSql: String, comparisonPlaylist: SmartPlaylist) {
		assertEquals(expectedName, comparisonPlaylist.name)
		assertEquals(expectedSql, comparisonPlaylist.dynamicSql)
	}
}
