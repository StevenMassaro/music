package music.model

data class TrackUpdate(
        val id: Long,
        val songId: Long,
        val field: String,
        val newValue: String,
        val updateType: Long?
) {
	override fun toString(): String {
		return "TrackUpdate(id=$id, songId=$songId, field='$field', newValue='$newValue', updateType=$updateType)"
	}
}