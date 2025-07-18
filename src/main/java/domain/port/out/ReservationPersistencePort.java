package domain.port.out;

import domain.model.OfferedService;
import domain.model.Reservation;
import domain.model.ReservationStatus;
import domain.model.User;

import java.time.Instant;
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
    List<Reservation> findByDateRange(Instant rangeStart, Instant rangeEnd);

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
    List<Reservation> findOverlappingReservations(Long serviceId, Instant startTime, Instant endTime, Optional<Long> excludeReservationId);

    /**
     * Busca todas las reservas con un estado específico.
     * @param status el estado de la reserva.
     * @return una lista de reservas con ese estado.
     */
    List<Reservation> findByStatus(ReservationStatus status);


    /**
     * Busca reservas futuras para un usuario.
     * @param user el usuario.
     * @return una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByOwner(User user);

    /**
     * Cuenta el número de reservas activas (PENDING o CONFIRMED) para un servicio en un slot de tiempo.
     * Útil si un servicio tiene una capacidad limitada (ej. múltiples personas pueden reservar el mismo slot).
     * @param serviceId el ID del servicio.
     * @param startTime el tiempo de inicio del slot.
     * @param endTime el tiempo de fin del slot.
     * @return el número de reservas activas.
     */
    long countActiveReservationsForServiceInSlot(Long serviceId, Instant startTime, Instant endTime);

    /**
     * Busca reservas futuras asociadas a un servicio.
     * @param serviceId el id del servicio ofrecido.
     * @return una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByOfferedServiceId(Long serviceId);

    /**
     * Busca reservas futuras para un usuario específico usando su ID.
     * @param userId El ID del usuario.
     * @return Una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByOwnerId(Long userId);

    /**
     * Busca reservas según filtros combinados (para administradores).
     * @param ownerIdParam ID del propietario a filtrar (opcional).
     * @param serviceId ID del servicio a filtrar (opcional).
     * @param startDate Fecha de inicio del rango (opcional).
     * @param endDate Fecha de fin del rango (opcional).
     * @return Una lista de reservas que coinciden con los filtros.
     */
    List<Reservation> findReservationsByFilters(Optional<Long> ownerIdParam, Optional<Long> serviceId, Instant startDate, Instant endDate);

    /**
     * Busca reservas futuras para un propietario y servicio específicos.
     * @param ownerId El ID del usuario propietario.
     * @param service El objeto de servicio.
     * @param currentTime La hora actual para filtrar por futuras.
     * @return Una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByOwnerIdAndService(Long ownerId, OfferedService service, Instant currentTime);

    /**
     * Busca reservas futuras para un propietario dentro de un rango de fechas.
     * @param ownerId El ID del usuario propietario.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @return Una lista de reservas futuras.
     */
    List<Reservation> findFutureReservationsByOwnerIdAndDateRange(Long ownerId, Instant startDate, Instant endDate);
}
