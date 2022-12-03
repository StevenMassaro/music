package music.model;


import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseSongEvent {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;
	private long songId;
	private long deviceId;
	private boolean imported;

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getSongId() {
		return songId;
	}

	public void setSongId(long songId) {
		this.songId = songId;
	}

	public long getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(long deviceId) {
		this.deviceId = deviceId;
	}

	public boolean isImported() {
		return imported;
	}

	public void setImported(boolean imported) {
		this.imported = imported;
	}
}
