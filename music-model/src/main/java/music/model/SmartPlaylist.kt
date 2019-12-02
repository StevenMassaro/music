package music.model

import java.util.*

class SmartPlaylist(val id: Long, val name: String, val dynamicSql: String, val dateCreated: Date = Date(), var dateUpdated: Date?) {
}
