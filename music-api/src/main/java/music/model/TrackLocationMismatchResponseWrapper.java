package music.model;

import java.util.Map;

public class TrackLocationMismatchResponseWrapper<T> {
	private final T data;
	private final Map<Track, Exception> failures;

	public TrackLocationMismatchResponseWrapper(T data, Map<Track, Exception> failures) {
		this.data = data;
		this.failures = failures;
	}

	public T getData() {
		return data;
	}

	public Map<Track, Exception> getFailures() {
		return failures;
	}
}
