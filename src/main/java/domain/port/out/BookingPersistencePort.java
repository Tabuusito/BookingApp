package domain.port.out;

import domain.model.Booking;
import domain.model.TimeSlot;
import domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de persistencia para gestionar las entidades Booking.
 * Define las operaciones de base de datos necesarias para la lógica de negocio de las reservas.
 */
public interface BookingPersistencePort {

    /**
     * Guarda un nuevo booking o actualiza uno existente.
     * @param booking el booking a guardar.
     * @return el booking guardado.
     */
    Booking save(Booking booking);

    /**
     * Busca un booking por su UUID.
     * @param bookingUuid el UUID del booking.
     * @return un Optional conteniendo el booking si se encuentra.
     */
    Optional<Booking> findByUuid(UUID bookingUuid);

    /**
     * Elimina un booking de la persistencia por su UUID.
     * Nota: Normalmente se prefiere un cambio de estado (cancelación) a un borrado físico.
     * @param bookingUuid el UUID del booking a eliminar.
     */
    void deleteByUuid(UUID bookingUuid);

    /**
     * Busca todos los bookings realizados por un cliente específico.
     * @param client el usuario cliente.
     * @return una lista de bookings para ese cliente.
     */
    List<Booking> findByClient(User client);

    /**
     * Busca todos los bookings asociados a un TimeSlot específico.
     * @param timeSlot el slot de tiempo.
     * @return una lista de bookings para ese slot.
     */
    List<Booking> findByTimeSlot(TimeSlot timeSlot);

    /**
     * Cuenta el número de bookings existentes para un TimeSlot específico.
     * Es crucial para validar la capacidad del slot antes de crear un nuevo booking.
     * @param timeSlot el slot de tiempo a consultar.
     * @return el número de bookings asociados.
     */
    long countByTimeSlot(TimeSlot timeSlot);

    /**
     * Verifica si un cliente específico ya tiene un booking para un TimeSlot determinado.
     * Evita que un mismo cliente se apunte dos veces a la misma clase.
     * @param client el usuario cliente.
     * @param timeSlot el slot de tiempo.
     * @return true si ya existe un booking, false en caso contrario.
     */
    boolean existsByClientAndTimeSlot(User client, TimeSlot timeSlot);
}
