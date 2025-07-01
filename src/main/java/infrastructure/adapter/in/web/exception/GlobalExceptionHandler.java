package infrastructure.adapter.in.web.exception;

import domain.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // --- Manejadores para Errores del Cliente (4xx) ---

    /**
     * Maneja excepciones de recursos no encontrados (404 Not Found).
     * Agrupa todas las excepciones que indican que una entidad específica no fue encontrada.
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            ReservationNotFoundException.class,
            OfferedServiceNotFoundException.class
    })
    public ResponseEntity<Object> handleResourceNotFoundException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, request);
    }

    /**
     * Maneja excepciones de conflicto (409 Conflict).
     * Agrupa excepciones donde la petición es válida pero entra en conflicto con el estado actual del servidor.
     */
    @ExceptionHandler({
            DuplicateUserInfoException.class,
            DuplicateServiceNameException.class,
            ServiceInUseException.class,
            ServiceNotAvailableException.class
    })
    public ResponseEntity<Object> handleConflictException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.CONFLICT, request);
    }

    /**
     * Maneja excepciones de petición incorrecta (400 Bad Request).
     * Usado para lógica de negocio inválida, como fechas incorrectas.
     */
    @ExceptionHandler(InvalidReservationTimeException.class)
    public ResponseEntity<Object> handleBadRequestException(RuntimeException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, request);
    }

    /**
     * Maneja errores de validación de DTOs (anotaciones @Valid).
     * Proporciona una respuesta más detallada con los errores de cada campo.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, WebRequest request) {
        Map<String, Object> body = buildBaseErrorBody(HttpStatus.BAD_REQUEST, request);

        // Extrae los errores de campo específicos
        Map<String, String> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(
                        fieldError -> fieldError.getField(),
                        fieldError -> fieldError.getDefaultMessage() != null ? fieldError.getDefaultMessage() : "Invalid value"
                ));
        body.put("errors", fieldErrors);
        body.put("message", "Validation failed. Check the 'errors' field for details.");

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // --- Manejadores para Errores de Seguridad (4xx) ---

    /**
     * Maneja excepciones de acceso denegado (403 Forbidden).
     * Ocurre cuando un usuario está autenticado pero no tiene los roles/permisos necesarios.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, "Access Denied. You do not have permission to perform this action.", HttpStatus.FORBIDDEN, request);
    }

    // --- Manejador General para Errores del Servidor (5xx) ---

    /**
     * Manejador de último recurso para cualquier excepción no capturada.
     * Devuelve un error genérico 500 Internal Server Error para no exponer detalles internos.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGlobalException(Exception ex, WebRequest request) {
        System.err.println("An unexpected error occurred: " + ex.getClass().getName());
        ex.printStackTrace();

        return buildErrorResponse(ex, "An unexpected internal server error occurred.", HttpStatus.INTERNAL_SERVER_ERROR, request);
    }


    // --- Métodos de Ayuda para Construir Respuestas de Error Consistentes ---

    private ResponseEntity<Object> buildErrorResponse(Exception ex, HttpStatus status, WebRequest request) {
        return buildErrorResponse(ex, ex.getMessage(), status, request);
    }

    private ResponseEntity<Object> buildErrorResponse(Exception ex, String message, HttpStatus status, WebRequest request) {
        Map<String, Object> body = buildBaseErrorBody(status, request);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    private Map<String, Object> buildBaseErrorBody(HttpStatus status, WebRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return body;
    }
}
