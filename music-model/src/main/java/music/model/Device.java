package music.model;

import java.util.Date;

public class Device {

    private long id;
    private String name;
    private String format;
    private Integer bitrate;
    private Integer sampleRate;
    private Integer channels;
    // max size of artwork
    private Integer artsize;
    private Date dateCreated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public Integer getBitrate() {
		return bitrate;
	}

	public void setBitrate(Integer bitrate) {
		this.bitrate = bitrate;
	}

	public Integer getSampleRate() {
		return sampleRate;
	}

	public void setSampleRate(Integer sampleRate) {
		this.sampleRate = sampleRate;
	}

	public Integer getChannels() {
		return channels;
	}

	public void setChannels(Integer channels) {
		this.channels = channels;
	}

	public Integer getArtsize() {
		return artsize;
	}

	public void setArtsize(Integer artsize) {
		this.artsize = artsize;
	}

	public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }
}
