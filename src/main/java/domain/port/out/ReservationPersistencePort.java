package domain.port.out;

import domain.model.OfferedService;
import domain.model.Reservation;
import domain.model.ReservationStatus;
import domain.model.User;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

public interface ReservationPersistencePort {

    /**
     * Guarda una nueva reserva o actualiza una existente.
     * @param reservation la reserva a guardar.
     * @return la reserva guardada (con el ID asignado si es nueva).
     */
    Reservation save(Reservation reservation);

    /**
     * Busca una reserva por su ID.
     * @param reservationId el ID de la reserva.
     * @return un Optional conteniendo la reserva si se encuentra, o un Optional vacío.
     */
    Optional<Reservation> findById(Long reservationId);

    /**
     * Elimina una reserva por su ID.
     * @param reservationId el ID de la reserva a eliminar.
     */
    void deleteById(Long reservationId);

    /**
     * Obtiene todas las reservas.
     * (Considerar la paginación).
     * @return una lista de todas las reservas.
     */
    List<Reservation> findAll();

    /**
     * Busca todas las reservas realizadas por un usuario específico.
     * @param user el usuario.
     * @return una lista de reservas para ese usuario.
     */
    List<Reservation> findByUser(User user);

    /**
     * Busca todas las reservas para un servicio ofrecido específico.
     * @param service el servicio ofrecido.
     * @return una lista de reservas para ese servicio.
     */
    List<Reservation> findByOfferedService(OfferedService service);

    /**
     * Busca todas las reservas dentro de un rango de fechas específico.
     * Útil para calendarios o vistas de disponibilidad.
     * @param rangeStart el inicio del rango de fechas.
     * @param rangeEnd el fin del rango de fechas.
     * @return una lista de reservas dentro del rango.
     */
    List<Reservation> findByDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd);

    /**
     * Busca reservas que se solapan con un rango de tiempo dado para un servicio específico.
     * Crucial para la verificación de disponibilidad.
     * @param serviceId el ID del servicio.
     * @param startTime el tiempo de inicio propuesto.
     * @param endTime el tiempo de fin propuesto.
     * @param excludeReservationId (opcional) el ID de una reserva existente a excluir de la comprobación
     *                           (útil al actualizar una reserva).
     * @return una lista de reservas que se solapan.
     */
    List<Reservation> findOverlappingReservations(Long serviceId, LocalDateTime startTime, LocalDateTime endTime, Optional<Long> excludeReservationId);

    /**
     * Busca todas las reservas con un estado específico.
     * @param status el estado de la reserva.
     * @return una lista de reservas con ese estado.
     */
    List<Reservation> findByStatus(ReservationStatus status);

    // --- Métodos adicionales que podrían ser útiles ---

    /**
     * Busca reservas futuras para un usuario.
     * @param user el usuario.
     * @param currentTime la fecha y hora actual, para determinar qué es "futuro".
     * @return una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByUser(User user, LocalDateTime currentTime);

    /**
     * Cuenta el número de reservas activas (PENDING o CONFIRMED) para un servicio en un slot de tiempo.
     * Útil si un servicio tiene una capacidad limitada (ej. múltiples personas pueden reservar el mismo slot).
     * @param serviceId el ID del servicio.
     * @param startTime el tiempo de inicio del slot.
     * @param endTime el tiempo de fin del slot.
     * @return el número de reservas activas.
     */
    long countActiveReservationsForServiceInSlot(Long serviceId, LocalDateTime startTime, LocalDateTime endTime);

}
