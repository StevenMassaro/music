package music.model;

import org.jetbrains.annotations.NotNull;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "plays")
public final class Play extends BaseSongEvent {
	private Date playDate;

	@NotNull
	public Date getPlayDate() {
		return playDate;
	}

	public void setPlayDate(@NotNull Date playDate) {
		this.playDate = playDate;
	}
}
