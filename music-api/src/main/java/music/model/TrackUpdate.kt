package music.model

data class TrackUpdate(
        val id: Long?,
        val songId: Long,
        val field: String,
        val newValue: String,
        val updateType: Long?
)