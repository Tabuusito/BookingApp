package domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidUuidFormatException extends RuntimeException {

  public InvalidUuidFormatException(String message) {
    super(message);
  }

  public InvalidUuidFormatException(String message, Throwable cause) {
    super(message, cause);
  }
}
