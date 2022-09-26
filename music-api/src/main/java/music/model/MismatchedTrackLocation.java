package music.model;

import lombok.Data;

@Data
public class MismatchedTrackLocation {

	private final long trackId;
	private final String currentLocation;
	private final String correctLocation;
}
