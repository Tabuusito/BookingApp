package domain.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Se lanza cuando se intenta crear un TimeSlot que se solapa en el tiempo
 * con otro ya existente para el mismo proveedor.
 * Mapea a un código de estado HTTP 409 Conflict.
 */
@ResponseStatus(HttpStatus.CONFLICT)
public class TimeSlotClashException extends RuntimeException {
    public TimeSlotClashException(String message) {
        super(message);
    }
}