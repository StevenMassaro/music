package music.model

class SyncResult(
        var newTracks: List<DeferredTrack>,
        var unmodifiedTracks: List<DeferredTrack>,
        var modifiedTracks: List<DeferredTrack>,
        var failedTracks: List<DeferredTrack>,
        var orphanedTracks: List<Track>,
        var unorphanedTracks: List<Track>
)