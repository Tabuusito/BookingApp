package domain.port.in;

import domain.model.TimeSlot;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para la gestión de TimeSlots (ofertas de eventos).
 * La autorización se maneja de forma declarativa mediante anotaciones de seguridad.
 */
public interface TimeSlotService {

    /**
     * Crea un nuevo TimeSlot para un servicio.
     * @param timeSlot el objeto TimeSlot con los detalles a crear.
     * @param serviceUuid el UUID del OfferedService al que se asocia este slot.
     * @return el TimeSlot creado.
     */
    // @PreAuthorize("hasRole('ADMIN') or @customSecurity.isServiceOwner(#serviceUuid)")
    TimeSlot createTimeSlot(TimeSlot timeSlot, UUID serviceUuid);

    /**
     * Cancela un TimeSlot.
     * @param timeSlotUuid el UUID del TimeSlot a cancelar.
     * @return el TimeSlot actualizado con estado CANCELLED.
     */
    // @PreAuthorize("hasRole('ADMIN') or @customSecurity.isTimeSlotProvider(#timeSlotUuid)")
    TimeSlot cancelTimeSlot(UUID timeSlotUuid);

    /**
     * Busca un TimeSlot por su UUID (público).
     * @param timeSlotUuid el UUID del slot a buscar.
     * @return un Optional con el TimeSlot si se encuentra.
     */
    // No requiere @PreAuthorize, es público
    Optional<TimeSlot> findTimeSlotByUuid(UUID timeSlotUuid);

    /**
     * Busca y devuelve una lista de TimeSlots disponibles (público).
     * @param serviceUuid el UUID del servicio a consultar.
     * @param from la fecha de inicio para la búsqueda.
     * @param to la fecha de fin para la búsqueda.
     * @return una lista de TimeSlots disponibles.
     */
    // No requiere @PreAuthorize, es público
    List<TimeSlot> findAvailableTimeSlots(UUID serviceUuid, Instant from, Instant to);
}