package music.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.jetbrains.annotations.NotNull;

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
