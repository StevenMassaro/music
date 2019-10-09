package music.model

import com.fasterxml.jackson.annotation.JsonIgnore

data class MediaMonkeyRating(
        var songTitle: String,
        var artist: String,
        var album: String,
        @JsonIgnore var rating: Byte
) : IItem {
    fun getNormalizedRating(): Byte {
        return (rating / 10).toByte();
    }
}