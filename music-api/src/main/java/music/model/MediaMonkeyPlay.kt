package music.model

import com.fasterxml.jackson.annotation.JsonIgnore
import java.math.BigDecimal
import java.util.*

data class MediaMonkeyPlay(var songTitle: String, var artist: String, var album: String, var playDate: String) : IItem {

    @JsonIgnore
    fun getDateAsDelphi():Date {
        val split:List<String> = playDate.split("\\.".toRegex())

        val c = Calendar.getInstance()
        c.set(1899, 11, 30, 0, 0, 0)// init delphi version of start of time
// months go 0..11
        c.set(Calendar.MILLISECOND, 0);

        c.add(Calendar.DATE, Integer.parseInt(split[0])) // add in the days
        val min = BigDecimal("0." + split[1]).multiply(BigDecimal(60)).multiply(BigDecimal(24))
        c.add(Calendar.MINUTE, min.toInt()) // add the minutes

        return c.time
    }

    override fun toString(): String {
        return "MediaMonkeyPlay(songTitle='$songTitle', artist='$artist', album='$album', playDate='$playDate')"
    }
}