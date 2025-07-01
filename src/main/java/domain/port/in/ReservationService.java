package domain.port.in; // Ajusta tu paquete

import domain.model.Reservation;
import infrastructure.adapter.in.web.security.RequesterContext;

import java.time.LocalDateTime;
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
     * @throws org.springframework.security.access.AccessDeniedException
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
     * @throws org.springframework.security.access.AccessDeniedException
     * @throws domain.exception.ServiceNotAvailableException
     */
    Optional<Reservation> updateReservation(Long reservationId, Reservation updateData, RequesterContext requester);

    /**
     * Elimina una reserva, aplicando reglas de autorización.
     * @param reservationId El ID de la reserva a eliminar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return `true` si se eliminó, `false` si no se encontró.
     * @throws org.springframework.security.access.AccessDeniedException
     * @throws IllegalStateException Si la reserva no se puede eliminar por reglas de negocio.
     */
    boolean deleteReservation(Long reservationId, RequesterContext requester);

    /**
     * Lista todas las reservas del sistema.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de todas las reservas.
     * @throws org.springframework.security.access.AccessDeniedException Si el usuario no es administrador.
     */
    List<Reservation> findAllReservations(RequesterContext requester);

    /**
     * Lista todas las reservas para un usuario específico.
     * @param ownerId El ID del usuario cuyas reservas de las que es propietario se quieren listar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return Una lista de reservas para el usuario especificado.
     * @throws org.springframework.security.access.AccessDeniedException Si el solicitante no tiene permiso.
     */
    List<Reservation> findReservationsByOwnerId(Long ownerId, RequesterContext requester);

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
    List<Reservation> findReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, RequesterContext requester);

    /**
     * Confirma una reserva pendiente.
     * @param reservationId El ID de la reserva a confirmar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return La reserva confirmada.
     * @throws domain.exception.ReservationNotFoundException
     * @throws IllegalStateException Si la reserva no está en estado PENDING.
     * @throws org.springframework.security.access.AccessDeniedException
     */
    Reservation confirmReservation(Long reservationId, RequesterContext requester);

    /**
     * Cancela una reserva.
     * @param reservationId El ID de la reserva a cancelar.
     * @param requester El contexto de seguridad del usuario que realiza la petición.
     * @return La reserva cancelada.
     * @throws domain.exception.ReservationNotFoundException
     * @throws IllegalStateException Si la reserva no se puede cancelar.
     * @throws org.springframework.security.access.AccessDeniedException
     */
    Reservation cancelReservation(Long reservationId, RequesterContext requester);
}