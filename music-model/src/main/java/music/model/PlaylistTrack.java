package music.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

@Entity
public class PlaylistTrack implements Serializable {
	private long playlistId;
	private long trackId;
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long sequenceId;
	@NotNull
	private Date dateAdded;

	public long getPlaylistId() {
		return playlistId;
	}

	public void setPlaylistId(long playlistId) {
		this.playlistId = playlistId;
	}

	public long getTrackId() {
		return trackId;
	}

	public void setTrackId(long trackId) {
		this.trackId = trackId;
	}

	public Long getSequenceId() {
		return sequenceId;
	}

	public void setSequenceId(Long sequenceId) {
		this.sequenceId = sequenceId;
	}

	public Date getDateAdded() {
		return dateAdded;
	}

	public void setDateAdded(Date dateAdded) {
		this.dateAdded = dateAdded;
	}

	public PlaylistTrack(long playlistId, long trackId, Long sequenceId, @NotNull Date dateAdded) {
		this.playlistId = playlistId;
		this.trackId = trackId;
		this.sequenceId = sequenceId;
		this.dateAdded = dateAdded;
	}

	public PlaylistTrack() {
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		PlaylistTrack that = (PlaylistTrack) o;
		return playlistId == that.playlistId && trackId == that.trackId;
	}

	@Override
	public int hashCode() {
		return Objects.hash(playlistId, trackId);
	}
}
