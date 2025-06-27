package domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateServiceNameException extends RuntimeException {
    public DuplicateServiceNameException(String message) {
        super(message);
    }
}