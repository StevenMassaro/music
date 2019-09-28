package music.model

data class PlayImportResult(var successful: List<IPlay>, var failed: List<IPlay>, var alreadyImported: List<IPlay>)