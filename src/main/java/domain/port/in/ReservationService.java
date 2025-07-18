package domain.port.in;

import domain.model.Reservation;
import infrastructure.adapter.in.web.security.RequesterContext;
import org.springframework.security.access.AccessDeniedException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Puerto de entrada para gestionar la lógica de negocio de las reservas.
 * Define los casos de uso disponibles para las reservas en la aplicación.
 */
public interface ReservationService {

    /**
     * Crea una nueva reserva.
     * @param reservationDetails Objeto de dominio con los detalles de la reserva.
     * @param userId ID del usuario para quien se crea la reserva.
     * @param serviceId ID del servicio que se reserva.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return La reserva creada y persistida.
     * @throws domain.exception.UserNotFoundException
     * @throws domain.exception.OfferedServiceNotFoundException
     * @throws domain.exception.ServiceNotAvailableException
     * @throws AccessDeniedException
     */
    Reservation createReservation(Reservation reservationDetails, Long userId, Long serviceId, RequesterContext requester);

    /**
     * Busca una reserva por su ID, aplicando reglas de autorización.
     * @param reservationId El ID de la reserva a buscar.
     * @param requester El contexto de seguridad del usuario que solicita la información.
     * @return Un {@link Optional} con la reserva si se encuentra y está autorizada.
     */
    Optional<Reservation> findReservationById(Long reservationId, RequesterContext requester);

    /**
     * Actualiza una reserva existente.
     * @param reservationId El ID de la reserva a actualizar.
     * @param updateData Objeto de dominio con los campos actualizados.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Un {@link Optional} con la reserva actualizada.
     * @throws domain.exception.ReservationNotFoundException
     * @throws AccessDeniedException
     * @throws domain.exception.ServiceNotAvailableException
     */
    Optional<Reservation> updateReservation(Long reservationId, Reservation updateData, RequesterContext requester);

    /**
     * Elimina una reserva, aplicando reglas de autorización.
     * @param reservationId El ID de la reserva a eliminar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return `true` si se eliminó, `false` si no se encontró.
     * @throws AccessDeniedException
     * @throws IllegalStateException Si la reserva no se puede eliminar por reglas de negocio.
     */
    boolean deleteReservation(Long reservationId, RequesterContext requester);


    List<Reservation> findAllReservationsForAdmin(Optional<Long> ownerId, Optional<Long> serviceId, Instant startDate, Instant endDate, RequesterContext requester);
    
    /**
     * Lista todas las reservas para un servicio específico.
     * @param serviceId El ID del servicio.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de reservas para el servicio.
     */
    List<Reservation> findReservationsByServiceId(Long serviceId, RequesterContext requester);

    /**
     * Lista reservas activas dentro de un rango de fechas.
     * @param startDate Fecha de inicio del rango.
     * @param endDate   Fecha de fin del rango.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de reservas en el rango.
     */
    List<Reservation> findReservationsByDateRange(Instant startDate, Instant endDate, RequesterContext requester);


    /**
     * Lista todas las reservas de un usuario específico.
     * Es el método que el MyReservationController llamará para "mis reservas".
     *
     * @param ownerId El ID del usuario propietario de las reservas a listar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de reservas que pertenecen al 'ownerId'.
     * @throws AccessDeniedException Si el solicitante no es el dueño de 'ownerId' y tampoco es admin.
     */
    List<Reservation> findReservationsByOwnerId(Long ownerId, RequesterContext requester);

    /**
     * Lista las reservas de un usuario para un servicio específico.
     *
     * @param ownerId El ID del usuario propietario.
     * @param serviceId El ID del servicio.
     * @param requester El contexto de seguridad del usuario.
     * @return Una lista de reservas.
     * @throws AccessDeniedException Si el solicitante no es el dueño de 'ownerId' y tampoco es admin.
     */
    List<Reservation> findMyReservationsByServiceId(Long ownerId, Long serviceId, RequesterContext requester);

    /**
     * Lista las reservas de un usuario dentro de un rango de fechas.
     *
     * @param ownerId El ID del usuario propietario.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @param requester El contexto de seguridad del usuario.
     * @return Una lista de reservas.
     * @throws AccessDeniedException Si el solicitante no es el dueño de 'ownerId' y tampoco es admin.
     */
    List<Reservation> findMyReservationsByDateRange(Long ownerId, Instant startDate, Instant endDate, RequesterContext requester);

    /**
     * Confirma una reserva pendiente.
     * @param reservationId El ID de la reserva a confirmar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return La reserva confirmada.
     * @throws domain.exception.ReservationNotFoundException
     * @throws IllegalStateException Si la reserva no está en estado PENDING.
     * @throws AccessDeniedException
     */
    Reservation confirmReservation(Long reservationId, RequesterContext requester);

    /**
     * Cancela una reserva.
     * @param reservationId El ID de la reserva a cancelar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return La reserva cancelada.
     * @throws domain.exception.ReservationNotFoundException
     * @throws IllegalStateException Si la reserva no se puede cancelar.
     * @throws AccessDeniedException
     */
    Reservation cancelReservation(Long reservationId, RequesterContext requester);
}