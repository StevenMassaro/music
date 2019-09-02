package music.model;

import music.utils.FieldUtils;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.util.Date;


public class Track {

    private long id;
    private String title;
    private String location;
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

    public Track(Tag v2tag, String location) {
        this.title = v2tag.getFirst(FieldKey.TITLE);
        this.album = v2tag.getFirst(FieldKey.ALBUM);
        this.artist = v2tag.getFirst(FieldKey.ARTIST);
        this.albumArtist = v2tag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = v2tag.getFirst(FieldKey.GENRE);
        this.year = v2tag.getFirst(FieldKey.YEAR);
        this.discNumber = FieldUtils.getLongOrNull(v2tag, FieldKey.DISC_NO);
        this.trackNumber = FieldUtils.getLongOrNull(v2tag, FieldKey.TRACK);
        this.comment = v2tag.getFirst(FieldKey.COMMENT);
        this.location = location;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Long getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(Long discNumber) {
        this.discNumber = discNumber;
    }

    public Long getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Long trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date dateUpdated) {
        this.dateUpdated = dateUpdated;
    }
}
