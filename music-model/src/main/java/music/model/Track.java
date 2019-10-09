package music.model;

import music.utils.FieldUtils;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;

import java.io.File;
import java.io.IOException;
import java.util.Date;


public class Track {

    private long id;
    private String title;
    private String location;
    private String hash;
    private String album;
    private String artist;
    private String albumArtist;
    private String genre;
    private String year;
    private Long discNumber;
    private Long trackNumber;
    private long plays;
    private Byte rating;
    private String comment;
    private boolean deletedInd = false;
    private Date dateCreated;
    private Date dateUpdated;
    private Date fileLastModifiedDate;

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

    public Track(Tag v2tag, String location, File file, Date fileLastModifiedDate) throws IOException {
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
        this.fileLastModifiedDate = fileLastModifiedDate;
        if(file != null){
            this.hash = DigestUtils.sha512Hex(FileUtils.readFileToByteArray(file));
        }
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

    public String getHash() throws IOException {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
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

    public long getPlays() {
        return plays;
    }

    public void setPlays(long plays) {
        this.plays = plays;
    }

    public Byte getRating() {
        return rating;
    }

    public void setRating(Byte rating) {
        this.rating = rating;
    }

    public boolean getDeletedInd() {
        return deletedInd;
    }

    public void setDeletedInd(boolean deletedInd) {
        this.deletedInd = deletedInd;
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

    public Date getFileLastModifiedDate() {
        return fileLastModifiedDate;
    }

    public void setFileLastModifiedDate(Date fileLastModifiedDate) {
        this.fileLastModifiedDate = fileLastModifiedDate;
    }

    @Override
    public String toString() {
        return "Track{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", location='" + location + '\'' +
                ", hash='" + hash + '\'' +
                ", album='" + album + '\'' +
                ", artist='" + artist + '\'' +
                ", albumArtist='" + albumArtist + '\'' +
                ", genre='" + genre + '\'' +
                ", year='" + year + '\'' +
                ", discNumber=" + discNumber +
                ", trackNumber=" + trackNumber +
                ", comment='" + comment + '\'' +
                ", deletedInd=" + deletedInd +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", fileLastModifiedDate=" + fileLastModifiedDate +
                '}';
    }
}
