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
    private String album_artist;
    private String genre;
    private String year;
    private Long disc_no;
    private Long track;
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
        this.album_artist = v2tag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = v2tag.getFirst(FieldKey.GENRE);
        this.year = v2tag.getFirst(FieldKey.YEAR);
        this.disc_no = FieldUtils.getLongOrNull(v2tag, FieldKey.DISC_NO);
        this.track = FieldUtils.getLongOrNull(v2tag, FieldKey.TRACK);
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

    public String getAlbum_artist() {
        return album_artist;
    }

    public void setAlbum_artist(String album_artist) {
        this.album_artist = album_artist;
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

    public Long getDisc_no() {
        return disc_no;
    }

    public void setDisc_no(Long disc_no) {
        this.disc_no = disc_no;
    }

    public Long getTrack() {
        return track;
    }

    public void setTrack(Long track) {
        this.track = track;
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
                ", album_artist='" + album_artist + '\'' +
                ", genre='" + genre + '\'' +
                ", year='" + year + '\'' +
                ", disc_no=" + disc_no +
                ", track=" + track +
                ", comment='" + comment + '\'' +
                ", deletedInd=" + deletedInd +
                ", dateCreated=" + dateCreated +
                ", dateUpdated=" + dateUpdated +
                ", fileLastModifiedDate=" + fileLastModifiedDate +
                '}';
    }
}
