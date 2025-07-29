package domain.port.out;

import domain.model.TimeSlot;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia para gestionar las entidades TimeSlot.
 * Define las operaciones de base de datos para la oferta de eventos en el calendario.
 */
public interface TimeSlotPersistencePort {

    /**
     * Guarda un nuevo TimeSlot o actualiza uno existente.
     * @param timeSlot el slot de tiempo a guardar.
     * @return el slot de tiempo guardado.
     */
    TimeSlot save(TimeSlot timeSlot);

    /**
     * Busca un TimeSlot por su UUID.
     * @param timeSlotUuid el UUID del slot.
     * @return un Optional conteniendo el slot si se encuentra.
     */
    Optional<TimeSlot> findByUuid(UUID timeSlotUuid);

    /**
     * Elimina un TimeSlot por su UUID.
     * @param timeSlotUuid el UUID del slot a eliminar.
     */
    void deleteByUuid(UUID timeSlotUuid);

    /**
     * Busca todos los TimeSlots para un servicio ofrecido específico dentro de un rango de fechas.
     * Útil para mostrar a los clientes los slots disponibles.
     * @param serviceUuid el UUID del servicio ofrecido.
     * @param rangeStart la fecha de inicio del rango de búsqueda.
     * @param rangeEnd la fecha de fin del rango de búsqueda.
     * @return una lista de TimeSlots que coinciden con los criterios.
     */
    List<TimeSlot> findByServiceUuidAndStartTimeBetween(UUID serviceUuid, Instant rangeStart, Instant rangeEnd);

    /**
     * Busca slots de un proveedor que se solapen con un rango de tiempo dado.
     * Esencial para evitar que un proveedor cree slots que se pisen en su propio calendario.
     * @param providerUuid el UUID del proveedor (dueño del servicio).
     * @param startTime el tiempo de inicio del nuevo slot propuesto.
     * @param endTime el tiempo de fin del nuevo slot propuesto.
     * @param excludeTimeSlotUuid (opcional) el UUID de un slot existente a excluir de la comprobación (útil al actualizar).
     * @return una lista de TimeSlots que se solapan.
     */
    List<TimeSlot> findOverlappingSlotsForProvider(UUID providerUuid, Instant startTime, Instant endTime, Optional<UUID> excludeTimeSlotUuid);

    /**
     * Verifica si existen TimeSlots futuros para un OfferedService específico.
     * Un "TimeSlot futuro" es aquel cuya hora de inicio es posterior al momento actual.
     * Este método es muy eficiente ya que solo comprueba la existencia, sin traer datos.
     *
     * @param serviceUuid el UUID del OfferedService a consultar.
     * @return true si existe al menos un TimeSlot futuro, false en caso contrario.
     */
    boolean hasFutureTimeSlots(UUID serviceUuid);
}