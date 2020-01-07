package music.model

class AlbumArtUpdateStatusMessage (val album:String, val position: Int, val max: Int) {

	override fun toString(): String {
		return "AlbumArtUpdateStatusMessage(album='$album', position=$position, max=$max)"
	}
}