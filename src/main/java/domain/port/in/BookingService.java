package domain.port.in;

import domain.model.Booking;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Puerto de entrada para la gestión de Bookings (reservas de clientes).
 * La autorización se maneja de forma declarativa mediante anotaciones de seguridad.
 */
public interface BookingService {

    /**
     * Crea un nuevo booking para el usuario autenticado en un TimeSlot específico.
     * @param timeSlotUuid el UUID del TimeSlot al que el cliente desea unirse.
     * @param notes notas adicionales del cliente para el booking.
     * @return el Booking creado y confirmado.
     * @throws // ... excepciones de negocio
     */
    Booking createBooking(UUID timeSlotUuid, String notes);

    /**
     * Busca un booking por su UUID.
     * @param bookingUuid el UUID del booking a buscar.
     * @return un Optional con el booking si se encuentra.
     */
    Optional<Booking> findBookingByUuid(UUID bookingUuid);

    /**
     * Obtiene todos los bookings del usuario autenticado.
     * @return una lista de los bookings del cliente.
     */
    List<Booking> findMyBookings();

    /**
     * Permite a un cliente cancelar uno de sus bookings.
     * @param bookingUuid el UUID del booking a cancelar.
     * @return el booking actualizado con el estado CANCELLED.
     */
    Booking cancelMyBooking(UUID bookingUuid);
}