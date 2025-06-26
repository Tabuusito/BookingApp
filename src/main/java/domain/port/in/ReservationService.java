package domain.port.in;

import domain.model.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationService {

    /**
     * Crea una nueva reserva.
     *
     * @aram reservationDetails Objeto de dominio Reservation parcialmente llenado con los detalles de la reserva (ej. startTime, endTime, notes).
     *                           Los campos User y OfferedService se cargarán usando los IDs.
     * @aram userId             ID del usuario que realiza la reserva.
     * @aram serviceId          ID del servicio que se está reservando.
     * @return La reserva creada.
     * @hrows UserNotFoundException Si el userId no corresponde a un usuario existente.
     * @hrows OfferedServiceNotFoundException Si el serviceId no corresponde a un servicio existente.
     * @hrows ServiceNotAvailableException Si el servicio no está disponible en el horario solicitado.
     * @hrows InvalidReservationTimeException Si startTime es después de endTime, etc.
     */
    Reservation createReservation(Reservation reservationDetails, Long userId, Long serviceId, Optional<Long> requestingUserId);


    /**
     * Busca una reserva por su ID.
     *
     * @aram reservationId El ID de la reserva.
     * @aram requestingUserId (Opcional) El ID del usuario que solicita la información, para comprobaciones de autorización.
     * @return Un Optional con la reserva si se encuentra y el usuario tiene permiso, o Optional.empty().
     */
    Optional<Reservation> findReservationById(Long reservationId, Optional<Long> requestingUserId);


    /**
     * Actualiza una reserva existente.
     *
     * @aram reservationId  El ID de la reserva a actualizar.
     * @aram updateRequestDTO DTO con los campos a actualizar. El servicio mapeará esto o tomará los campos necesarios.
     *                         Alternativamente, podría tomar un objeto Reservation de dominio con los campos actualizados.
     * @aram requestingUserId (Opcional) El ID del usuario que realiza la actualización, para autorización.
     * @return Un Optional con la reserva actualizada, o Optional.empty() si no se encuentra o no se permite la actualización.
     * @hrows ReservationNotFoundException Si la reserva no existe.
     * @hrows AccessDeniedException Si el usuario no tiene permiso para actualizar.
     * @hrows ServiceNotAvailableException Si los nuevos tiempos entran en conflicto.
     */
    Optional<Reservation> updateReservation(Long reservationId, Reservation updatedReservationData, Optional<Long> requestingUserId);


    /**
     * Elimina una reserva.
     *
     * @aram reservationId El ID de la reserva a eliminar.
     * @aram requestingUserId (Opcional) El ID del usuario que intenta eliminar, para autorización.
     * @return true si se eliminó, false en caso contrario (ej. no encontrada o sin permiso).
     * @hrows AccessDeniedException Si el usuario no tiene permiso.
     */
    boolean deleteReservation(Long reservationId, Optional<Long> requestingUserId);

    /**
     * Lista todas las reservas (generalmente para administradores).
     *
     * @return Lista de todas las reservas.
     */
    List<Reservation> findAllReservations(Optional<Long> authenticatedUserId);

    /**
     * Lista todas las reservas para un usuario específico.
     *
     * @aram userId El ID del usuario.
     * @return Lista de reservas del usuario.
     */
    List<Reservation> findReservationsByUserId(Long userId, Optional<Long> authenticatedUserId);

    /**
     * Lista todas las reservas para un servicio específico.
     *
     * @aram serviceId El ID del servicio.
     * @return Lista de reservas para el servicio.
     */
    List<Reservation> findReservationsByServiceId(Long serviceId, Optional<Long> authenticatedUserId);

    /**
     * Lista reservas dentro de un rango de fechas.
     *
     * @aram startDate Fecha de inicio del rango.
     * @aram endDate   Fecha de fin del rango.
     * @return Lista de reservas en el rango.
     */
    List<Reservation> findReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Optional<Long> authenticatedUserId);


    /**
     * Confirma una reserva pendiente.
     * @aram reservationId El ID de la reserva a confirmar.
     * @aram confirmingUserId (Opcional) El ID del usuario que confirma (para auditoría/autorización, usualmente un admin).
     * @return La reserva confirmada.
     * @hrows ReservationNotFoundException
     * @hrows IllegalReservationStateException Si la reserva no está en un estado que permita confirmación.
     */
    Reservation confirmReservation(Long reservationId, Optional<Long> confirmingUserId);

    /**
     * Cancela una reserva.
     * @aram reservationId El ID de la reserva a cancelar.
     * @aram cancellingUserId (Opcional) El ID del usuario que cancela (para auditoría/autorización).
     * @return La reserva cancelada.
     * @hrows ReservationNotFoundException
     * @hrows IllegalReservationStateException Si la reserva no se puede cancelar (ej. ya pasó o política de cancelación).
     * @hrows AccessDeniedException Si el usuario no tiene permiso.
     */
    Reservation cancelReservation(Long reservationId, Optional<Long> cancellingUserId);

}
