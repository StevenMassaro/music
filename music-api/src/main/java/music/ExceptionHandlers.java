package music;

import music.exception.TrackAlreadyExistsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionHandlers {

	@ExceptionHandler(value = TrackAlreadyExistsException.class)
	public ResponseEntity<Object> exception(TrackAlreadyExistsException exception) {
		return new ResponseEntity<>(HttpStatus.CONFLICT);
	}
}
