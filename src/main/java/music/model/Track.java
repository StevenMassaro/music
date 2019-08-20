package music.model;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

import java.util.Date;

import static music.utils.FieldUtils.getLongOrNull;

public class Track {

    private long id;
    private String title;
    private String album;
    private String artist;
    private String albumArtist;
    private String genre;
    private String year;
    private Long discNumber;
    private Long trackNumber;
    private String comment;
    private Date dateCreated;
    private Date dateUpdated;

    public Track(){
        //            AudioHeader audioHeader = f.getAudioHeader();
//            audioHeader.getBitRateAsNumber();
//            audioHeader.getSampleRateAsNumber();
//            audioHeader.getFormat();
//            audioHeader.getTrackLength();
//            audioHeader.getChannels();
//            audioHeader.getEncodingType();//            tag.getFields("TRACKNUMBER");
////            tag.getFields(FieldKey.COMMENT);
    }

    public Track(Tag v2tag) {
        this.title = v2tag.getFirst(FieldKey.TITLE);
        this.album = v2tag.getFirst(FieldKey.ALBUM);
        this.artist = v2tag.getFirst(FieldKey.ARTIST);
        this.albumArtist = v2tag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = v2tag.getFirst(FieldKey.GENRE);
        this.year = v2tag.getFirst(FieldKey.YEAR);
        this.discNumber = getLongOrNull(v2tag, FieldKey.DISC_NO);
        this.trackNumber = getLongOrNull(v2tag, FieldKey.TRACK);
        this.comment = v2tag.getFirst(FieldKey.COMMENT);
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbumArtist() {
        return albumArtist;
    }

    public void setAlbumArtist(String albumArtist) {
        this.albumArtist = albumArtist;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }
}
