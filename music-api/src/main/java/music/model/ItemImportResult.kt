package music.model

data class ItemImportResult(var successful: List<IItem>, var failed: List<IItem>, var alreadyImported: List<IItem>)