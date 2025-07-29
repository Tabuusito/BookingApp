package infrastructure.adapter.in.web.controller;

import domain.model.Booking;
import domain.port.in.BookingService;
import infrastructure.adapter.in.web.dto.BookingResponseDTO;
import infrastructure.adapter.in.web.mapper.BookingDTOMapper;
import infrastructure.adapter.in.web.util.UuidValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Toda la clase requiere rol de ADMIN
public class AdminBookingController extends AbstractBaseController {

    private final BookingService bookingService;
    private final BookingDTOMapper bookingMapper;
    private final UuidValidator uuidValidator;

    // NOTA: La creación de bookings por un admin es un caso de uso complejo.
    // Un admin normalmente crearía un TimeSlot, y luego, si acaso, haría un booking
    // en nombre de un cliente. Para simplificar, asumimos que los admins solo gestionan
    // bookings existentes. Si necesitaran crear bookings, habría que definir un DTO
    // y un método de servicio específicos para ello.

    @GetMapping
    public ResponseEntity<List<BookingResponseDTO>> getAllBookings(/* Posibles filtros como @RequestParam */) {
        // La implementación de un "findAll" para admin requeriría un nuevo método en el puerto/servicio.
        // Ejemplo: bookingService.findAllBookings(filterCriteria);
        // Por ahora, lo dejamos pendiente ya que no estaba en los puertos definidos.
        // Devolver una lista vacía o un 501 Not Implemented es una opción.
        return ResponseEntity.status(501).build();
    }

    @GetMapping("/{bookingUuid}")
    public ResponseEntity<BookingResponseDTO> getBookingByUuid(@PathVariable("bookingUuid") String bookingUuidStr) {
        UUID bookingUuid = uuidValidator.UUIDvalidateAndConvert(bookingUuidStr);
        // El @PreAuthorize en el servicio (o aquí) se asegura de que es un admin.
        return bookingService.findBookingByUuid(bookingUuid)
                .map(bookingMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{bookingUuid}/cancel")
    public ResponseEntity<BookingResponseDTO> cancelBookingAsAdmin(@PathVariable("bookingUuid") String bookingUuidStr) {
        UUID bookingUuid = uuidValidator.UUIDvalidateAndConvert(bookingUuidStr);
        // Necesitaríamos un método específico en el servicio para la cancelación por parte de un admin,
        // ya que la lógica podría ser diferente (ej. no comprobar si el evento ha pasado).
        // Ejemplo: bookingService.cancelBookingAsAdmin(bookingUuid);
        return ResponseEntity.status(501).build();
    }

    @DeleteMapping("/{bookingUuid}")
    public ResponseEntity<Void> deleteBooking(@PathVariable("bookingUuid") String bookingUuidStr) {
        // El borrado físico de un booking debería ser una operación muy rara y controlada.
        // bookingService.deleteBooking(bookingUuid);
        return ResponseEntity.status(501).build();
    }
}