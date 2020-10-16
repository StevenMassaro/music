package music.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "skips")
public class Skip extends BaseSongEvent {
	private Date skipDate;
	private Double secondsPlayed;

	public Date getSkipDate() {
		return skipDate;
	}

	public void setSkipDate(Date skipDate) {
		this.skipDate = skipDate;
	}

	public Double getSecondsPlayed() {
		return secondsPlayed;
	}

	public void setSecondsPlayed(Double secondsPlayed) {
		this.secondsPlayed = secondsPlayed;
	}
}
