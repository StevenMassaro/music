package music.model;

import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.flac.FlacTag;
import org.jaudiotagger.tag.id3.AbstractID3v2Tag;

public class Track {

    private long id;
    private String title;
    private String album;
    private String artist;
    private String albumArtist;
    private String genre;
    private String year;

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

    public Track(AbstractID3v2Tag v2tag) {
        this.title = v2tag.getFirst(FieldKey.TITLE);
        this.album = v2tag.getFirst(FieldKey.ALBUM);
        this.artist = v2tag.getFirst(FieldKey.ARTIST);
        this.albumArtist = v2tag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = v2tag.getFirst(FieldKey.GENRE);
        this.year = v2tag.getFirst(FieldKey.YEAR);
    }

    public Track(FlacTag flacTag) {
        this.title = flacTag.getFirst(FieldKey.TITLE);
        this.album = flacTag.getFirst(FieldKey.ALBUM);
        this.artist = flacTag.getFirst(FieldKey.ARTIST);
        this.albumArtist = flacTag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = flacTag.getFirst(FieldKey.GENRE);
        this.year = flacTag.getFirst(FieldKey.YEAR);
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
