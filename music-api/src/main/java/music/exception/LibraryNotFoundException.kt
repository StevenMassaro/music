package music.exception

class LibraryNotFoundException(libraryId: Long) : Exception("Library ${libraryId}does not exist.")