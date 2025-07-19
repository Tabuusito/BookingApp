package infrastructure.adapter.in.web.util;

import domain.exception.InvalidUuidFormatException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class UuidValidator {

    /**
     * Convierte una cadena de texto a un objeto UUID.
     * Si la cadena es nula, vacía o no tiene un formato UUID válido, lanza una InvalidUuidFormatException.
     *
     * @param uuidStr La cadena a convertir.
     * @return El objeto UUID correspondiente.
     * @throws InvalidUuidFormatException si la cadena no es un UUID válido.
     */
    public UUID UUIDvalidateAndConvert(String uuidStr) {
        if (uuidStr == null || uuidStr.isBlank()) {
            throw new InvalidUuidFormatException("El UUID no puede ser nulo o vacío.");
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidFormatException("El formato del UUID proporcionado no es válido: " + uuidStr, e);
        }
    }

    /**
     * Convierte una cadena de texto a un objeto UUID, pero permite que sea nula o vacía.
     *
     * @param uuidStr La cadena a convertir.
     * @return El objeto UUID correspondiente, o null si la cadena de entrada es nula o vacía.
     * @throws InvalidUuidFormatException si la cadena no está vacía pero no es un UUID válido.
     */
    public UUID validateAndConvertOptional(String uuidStr) {
        if (uuidStr == null || uuidStr.isBlank()) {
            return null;
        }
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            throw new InvalidUuidFormatException("El formato del UUID proporcionado no es válido: " + uuidStr, e);
        }
    }
}