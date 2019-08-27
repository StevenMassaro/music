package music.model;

import java.util.List;
import java.util.stream.Collectors;

public class SongVO {

    private String artist;
    private String albumArtist;
    private String album;
    private String discNumber;
    private String trackNumber;
    private String songTitle;
    private String songPath;
    private String extension;
    private Integer year;
    private String genre;
    private Integer fileLength;
    private Integer songLength;
    private Integer rating;
    private Integer bitrate;
    private Integer samplingFrequency;
    private Integer playCounter;
    private Double lastTimePlayed;
    //filemodified
    //trackmodified
    //dateadded
    //skipcount
    //tracktype
    //lyrics
    private Integer id;
    private Integer idmedia;
    private Integer idalbum;
    private Integer idfolder;

    public SongVO() {
    }
//
    public SongVO(Song song) {
        this.artist = song.getArtist();
        this.albumArtist = song.getAlbumArtist();
        this.album = song.getAlbum();
        this.discNumber = song.getDiscNumber();
        this.trackNumber = song.getTrackNumber();
        this.songTitle = song.getSongTitle();
        this.songPath = song.getSongPath();
        this.extension = song.getExtension();
        this.year = song.getYear();
        this.genre = song.getGenre();
        this.fileLength = song.getFileLength();
        this.songLength = song.getSongLength();
        this.rating = song.getRating();
        this.bitrate = song.getBitrate();
        this.samplingFrequency = song.getSamplingFrequency();
        this.playCounter = song.getPlayCounter();
        this.lastTimePlayed = song.getLastTimePlayed();
        this.id = song.getID();
        this.idmedia = song.getIDMedia();
        this.idalbum = song.getIDAlbum();
        this.idfolder = song.getIDFolder();
    }

    public static List<SongVO> toList(List<Song> songs){
        return songs.stream().map(SongVO::new).collect(Collectors.toList());
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

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDiscNumber() {
        return discNumber;
    }

    public void setDiscNumber(String discNumber) {
        this.discNumber = discNumber;
    }

    public String getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(String trackNumber) {
        this.trackNumber = trackNumber;
    }

    public String getSongTitle() {
        return songTitle;
    }

    public void setSongTitle(String songTitle) {
        this.songTitle = songTitle;
    }

    public String getSongPath() {
        return songPath;
    }

    public void setSongPath(String songPath) {
        this.songPath = songPath;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Integer getFileLength() {
        return fileLength;
    }

    public void setFileLength(Integer fileLength) {
        this.fileLength = fileLength;
    }

    public Integer getSongLength() {
        return songLength;
    }

    public void setSongLength(Integer songLength) {
        this.songLength = songLength;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public Integer getBitrate() {
        return bitrate;
    }

    public void setBitrate(Integer bitrate) {
        this.bitrate = bitrate;
    }

    public Integer getSamplingFrequency() {
        return samplingFrequency;
    }

    public void setSamplingFrequency(Integer samplingFrequency) {
        this.samplingFrequency = samplingFrequency;
    }

    public Integer getPlayCounter() {
        return playCounter;
    }

    public void setPlayCounter(Integer playCounter) {
        this.playCounter = playCounter;
    }

    public Double getLastTimePlayed() {
        return lastTimePlayed;
    }

    public void setLastTimePlayed(Double lastTimePlayed) {
        this.lastTimePlayed = lastTimePlayed;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getIdmedia() {
        return idmedia;
    }

    public void setIdmedia(Integer idmedia) {
        this.idmedia = idmedia;
    }

    public Integer getIdalbum() {
        return idalbum;
    }

    public void setIdalbum(Integer idalbum) {
        this.idalbum = idalbum;
    }

    public Integer getIdfolder() {
        return idfolder;
    }

    public void setIdfolder(Integer idfolder) {
        this.idfolder = idfolder;
    }
}
