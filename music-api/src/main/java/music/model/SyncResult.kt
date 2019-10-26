package music.model

class SyncResult(
        var newTracks: List<Track>,
        var unmodifiedTracks: List<Track>,
        var modifiedTracks: List<Track>,
        var failedTracks: List<Track>,
        var orphanedTracks: List<Track>,
        var unorphanedTracks: List<Track>
)