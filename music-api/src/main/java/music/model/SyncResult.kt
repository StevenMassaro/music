package music.model

class SyncResult(
	var newTracks: List<DeferredTrack> = ArrayList(),
	var unmodifiedTracks: List<DeferredTrack> = ArrayList(),
	var modifiedTracks: List<DeferredTrack> = ArrayList(),
	var failedTracks: List<DeferredTrack> = ArrayList(),
	var orphanedTracks: List<Track> = ArrayList(),
	var unorphanedTracks: List<Track> = ArrayList()
) {
	override fun toString(): String {
		return "SyncResult(newTracks=$newTracks, unmodifiedTracks=$unmodifiedTracks, modifiedTracks=$modifiedTracks, failedTracks=$failedTracks, orphanedTracks=$orphanedTracks, unorphanedTracks=$unorphanedTracks)"
	}
}