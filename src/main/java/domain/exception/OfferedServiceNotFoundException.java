package domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class OfferedServiceNotFoundException extends RuntimeException {
    public OfferedServiceNotFoundException(String message) {
        super(message);
    }
}