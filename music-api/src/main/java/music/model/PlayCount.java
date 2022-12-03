package music.model;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class PlayCount {
	@Id
	private long songId;
	private long deviceId;
	private boolean imported;
	private long playCount;

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

	public long getPlayCount() {
		return playCount;
	}

	public void setPlayCount(long playCount) {
		this.playCount = playCount;
	}
}
