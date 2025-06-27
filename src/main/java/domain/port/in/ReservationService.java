package domain.port.in;

import domain.model.Reservation;
import infrastructure.adapter.in.web.dto.UpdateReservationRequestDTO;
import domain.exception.UserNotFoundException;
import domain.exception.ReservationNotFoundException;
import domain.exception.OfferedServiceNotFoundException;
import domain.exception.ServiceNotAvailableException;

import org.springframework.security.access.AccessDeniedException;


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
     * Este método se encarga de validar la disponibilidad, calcular el precio y persistir la reserva.
     *
     * @param reservationDetails Objeto de dominio con los detalles iniciales de la reserva (ej. startTime, endTime, notes).
     * @param userId             ID del usuario para quien se crea la reserva.
     * @param serviceId          ID del servicio que se reserva.
     * @param requestingUserId   ID del usuario que realiza la petición, para validaciones de permisos.
     * @return La reserva creada y persistida.
     * @throws UserNotFoundException Si el 'userId' no corresponde a un usuario existente.
     * @throws OfferedServiceNotFoundException Si el 'serviceId' no corresponde a un servicio existente.
     * @throws ServiceNotAvailableException Si el servicio no está disponible en el horario solicitado.
     * @throws AccessDeniedException Si el 'requestingUserId' no tiene permiso para crear una reserva para el 'userId' especificado.
     */
    Reservation createReservation(Reservation reservationDetails, Long userId, Long serviceId, Optional<Long> requestingUserId);

    /**
     * Busca una reserva por su ID, aplicando reglas de autorización.
     *
     * @param reservationId El ID de la reserva a buscar.
     * @param requestingUserId (Opcional) El ID del usuario que solicita la información. El servicio verificará si este usuario
     *                         (o un administrador) tiene permiso para ver la reserva.
     * @return Un {@link Optional} con la reserva si se encuentra y está autorizada, o un {@link Optional#empty()} en caso contrario.
     */
    Optional<Reservation> findReservationById(Long reservationId, Optional<Long> requestingUserId);

    /**
     * Actualiza una reserva existente.
     *
     * @param reservationId El ID de la reserva a actualizar.
     * @param updateRequest Datos con los campos a actualizar. El servicio aplicará estos cambios.
     * @param requestingUserId ID del usuario que realiza la petición, para validaciones de permisos.
     * @return Un {@link Optional} con la reserva actualizada si se encuentra y la operación es exitosa y autorizada.
     * @throws ReservationNotFoundException Si la reserva con el ID especificado no existe.
     * @throws AccessDeniedException Si el 'requestingUserId' no tiene permiso para modificar esta reserva.
     * @throws ServiceNotAvailableException Si los nuevos tiempos solicitados entran en conflicto con otras reservas.
     */
    Optional<Reservation> updateReservation(Long reservationId, Reservation updateRequest, Optional<Long> requestingUserId);

    /**
     * Elimina una reserva, aplicando reglas de autorización.
     *
     * @param reservationId El ID de la reserva a eliminar.
     * @param requestingUserId ID del usuario que realiza la petición.
     * @return true si la reserva fue encontrada y eliminada exitosamente, false si no se encontró.
     * @throws AccessDeniedException Si el 'requestingUserId' no tiene permiso para eliminar esta reserva.
     * @throws IllegalStateException Si la reserva no se puede eliminar por reglas de negocio (ej. ya está en curso).
     */
    boolean deleteReservation(Long reservationId, Optional<Long> requestingUserId);

    /**
     * Lista todas las reservas del sistema. Operación restringida (típicamente a administradores).
     *
     * @param requestingUserId ID del usuario que realiza la petición, para verificar permisos de administrador.
     * @return Una lista de todas las reservas.
     * @throws AccessDeniedException Si el usuario no es un administrador.
     */
    List<Reservation> findAllReservations(Optional<Long> requestingUserId);

    /**
     * Lista todas las reservas para un usuario específico, con validación de permisos.
     *
     * @param userId El ID del usuario cuyas reservas se quieren listar.
     * @param requestingUserId ID del usuario que realiza la petición.
     * @return Una lista de reservas para el 'userId' especificado.
     * @throws AccessDeniedException Si el 'requestingUserId' no es el mismo que 'userId' y no es un administrador.
     */
    List<Reservation> findReservationsByUserId(Long userId, Optional<Long> requestingUserId);

    /**
     * Lista todas las reservas para un servicio específico.
     *
     * @param serviceId El ID del servicio.
     * @param requestingUserId ID del usuario que realiza la petición (para posibles futuras reglas de visibilidad).
     * @return Una lista de reservas para el servicio.
     */
    List<Reservation> findReservationsByServiceId(Long serviceId, Optional<Long> requestingUserId);

    /**
     * Lista reservas que están activas dentro de un rango de fechas.
     *
     * @param startDate Fecha y hora de inicio del rango.
     * @param endDate   Fecha y hora de fin del rango.
     * @param requestingUserId ID del usuario que realiza la petición (para posibles futuras reglas de visibilidad).
     * @return Una lista de reservas en el rango especificado.
     */
    List<Reservation> findReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, Optional<Long> requestingUserId);

    /**
     * Confirma una reserva que está en estado pendiente.
     *
     * @param reservationId El ID de la reserva a confirmar.
     * @param confirmingUserId ID del usuario que realiza la petición, usualmente un administrador.
     * @return La reserva con el estado actualizado a CONFIRMED.
     * @throws ReservationNotFoundException Si la reserva no existe.
     * @throws IllegalStateException Si la reserva no está en estado PENDING.
     * @throws AccessDeniedException Si el usuario no tiene permisos para confirmar reservas.
     */
    Reservation confirmReservation(Long reservationId, Optional<Long> confirmingUserId);

    /**
     * Cancela una reserva.
     *
     * @param reservationId El ID de la reserva a cancelar.
     * @param cancellingUserId ID del usuario que realiza la petición.
     * @return La reserva con el estado actualizado a CANCELLED.
     * @throws ReservationNotFoundException Si la reserva no existe.
     * @throws IllegalStateException Si la reserva no se puede cancelar debido a su estado actual o políticas de tiempo.
     * @throws AccessDeniedException Si el usuario no tiene permiso para cancelar esta reserva.
     */
    Reservation cancelReservation(Long reservationId, Optional<Long> cancellingUserId);
}
