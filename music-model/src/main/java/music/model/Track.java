package music.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.builder.DiffBuilder;
import org.apache.commons.lang3.builder.DiffResult;
import org.apache.commons.lang3.builder.Diffable;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.springframework.util.ReflectionUtils;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.Date;
import java.util.Objects;


public class Track implements Diffable<Track> {

    private long id;
    private String title;
	/**
	 * The non-absolute path of the file. This represents the path of the file within the library.
	 */
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
    private long skips;

    @Min(0)
	@Max(10)
    private Byte rating;
    private String comment;
    @JsonIgnore
    private boolean deletedInd = false;
    private long bitrate;
    private String encoding;
    private int sampleRate;
    private int duration;
    private Date dateCreated;
    @JsonIgnore
    private Date dateUpdated;
    @JsonIgnore
    private Date fileLastModifiedDate;
    private Date lastPlayedDate;
    @JsonIgnore
    private Library library;

    public Track(){
    }

    public Track(Tag v2tag, AudioHeader audioHeader, String location, File file, Date fileLastModifiedDate, Library library) throws IOException {
        this.title = v2tag.getFirst(FieldKey.TITLE);
        this.album = v2tag.getFirst(FieldKey.ALBUM);
        this.artist = v2tag.getFirst(FieldKey.ARTIST);
        this.album_artist = v2tag.getFirst(FieldKey.ALBUM_ARTIST);
        this.genre = v2tag.getFirst(FieldKey.GENRE);
        this.year = v2tag.getFirst(FieldKey.YEAR);
        this.disc_no = getLongOrNull(v2tag, FieldKey.DISC_NO);
        this.track = getLongOrNull(v2tag, FieldKey.TRACK);
        this.comment = v2tag.getFirst(FieldKey.COMMENT);
        this.location = location;
        this.fileLastModifiedDate = fileLastModifiedDate;
		this.library = library;
		if(file != null){
            InputStream inputStream = FileUtils.openInputStream(file);
            this.hash = DigestUtils.sha512Hex(inputStream);
            inputStream.close();
        }
        this.bitrate = audioHeader.getBitRateAsNumber();//kbps
        this.encoding = audioHeader.getEncodingType();//FLAC 16 bits (example)
        this.sampleRate = audioHeader.getSampleRateAsNumber();//hz
        this.duration = audioHeader.getTrackLength();//seconds
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

    public long getBitrate() {
        return bitrate;
    }

    public void setBitrate(long bitrate) {
        this.bitrate = bitrate;
    }

    public String getEncoding() {
        return encoding;
    }

    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public int getSampleRate() {
        return sampleRate;
    }

    public void setSampleRate(int sampleRate) {
        this.sampleRate = sampleRate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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

	public Date getLastPlayedDate() {
		return lastPlayedDate;
	}

	public void setLastPlayedDate(Date lastPlayedDate) {
		this.lastPlayedDate = lastPlayedDate;
	}

	public long getSkips() {
		return skips;
	}

	public void setSkips(long skips) {
		this.skips = skips;
	}

	public Library getLibrary() {
		return library;
	}

	public void setLibrary(Library library) {
		this.library = library;
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
                ", lastPlayedDate=" + lastPlayedDate +
                '}';
    }

    public boolean id3Equals(Object o) {
        if (this == o) return true;
        Track track1 = (Track) o;
        return Objects.equals(title, track1.title) &&
                Objects.equals(location, track1.location) &&
                Objects.equals(album, track1.album) &&
                Objects.equals(artist, track1.artist) &&
                Objects.equals(album_artist, track1.album_artist) &&
                Objects.equals(genre, track1.genre) &&
                Objects.equals(year, track1.year) &&
                Objects.equals(disc_no, track1.disc_no) &&
                Objects.equals(track, track1.track);
    }

	@Override
	public DiffResult diff(Track track) {
		DiffBuilder diffBuilder = new DiffBuilder(this, track, ToStringStyle.SHORT_PREFIX_STYLE);
		for (ModifyableTags modifyableTag : ModifyableTags.values()) {
			Field field = ReflectionUtils.findField(Track.class, modifyableTag.getPropertyName());
			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				Object trackVal = ReflectionUtils.getField(field, track);
				Object thisVal = ReflectionUtils.getField(field, this);
				diffBuilder.append(modifyableTag.getPropertyName(), thisVal, trackVal);
			}
		}
		return diffBuilder.build();
	}

	/**
	 * Get the path of the file, including the library subfolder. This appends the library subfolder to the location.
	 */
	@JsonIgnore
	public String getLibraryPath() {
		return library.getSubfolder() + File.separator + location;
	}

	@JsonIgnore
	public String getFilename() {
    	return FilenameUtils.getName(location);
	}

	@JsonIgnore
	public String getExtension() {
    	return FilenameUtils.getExtension(location);
	}

	private static Long getLongOrNull(Tag tag, FieldKey fieldKey){
		String val = tag.getFirst(fieldKey);
		return (val == null || val.isEmpty()) ? null : Long.valueOf(val);
	}
}
