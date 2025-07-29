package infrastructure.adapter.in.web.controller;

import domain.model.Booking;
import domain.model.TimeSlot;
import domain.port.in.BookingService;
import domain.port.in.TimeSlotService;
import infrastructure.adapter.in.web.dto.BookingResponseDTO;
import infrastructure.adapter.in.web.dto.CreateBookingRequestDTO;
import infrastructure.adapter.in.web.dto.TimeSlotResponseDTO;
import infrastructure.adapter.in.web.mapper.BookingDTOMapper;
import infrastructure.adapter.in.web.mapper.TimeSlotDTOMapper;
import infrastructure.adapter.in.web.util.UuidValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api") // Ruta base para ambos tipos de endpoints
@RequiredArgsConstructor
public class MyBookingController extends AbstractBaseController {

    private final BookingService bookingService;
    private final TimeSlotService timeSlotService;
    private final BookingDTOMapper bookingMapper;
    private final TimeSlotDTOMapper timeSlotMapper;
    private final UuidValidator uuidValidator;

    // --- Endpoints Públicos para ver la Oferta ---

    @GetMapping("/timeslots")
    public ResponseEntity<List<TimeSlotResponseDTO>> findAvailableTimeSlots(
            @RequestParam("serviceUuid") String serviceUuidStr,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {

        UUID serviceUuid = uuidValidator.UUIDvalidateAndConvert(serviceUuidStr);
        List<TimeSlot> slots = timeSlotService.findAvailableTimeSlots(serviceUuid, from, to);
        List<TimeSlotResponseDTO> response = slots.stream()
                .map(timeSlotMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // --- Endpoints Protegidos para Clientes Autenticados ---

    @PostMapping("/me/bookings")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<BookingResponseDTO> createMyBooking(@Valid @RequestBody CreateBookingRequestDTO requestDTO) {
        UUID timeSlotUuid = uuidValidator.UUIDvalidateAndConvert(requestDTO.getTimeSlotUuid());
        Booking createdBooking = bookingService.createBooking(timeSlotUuid, requestDTO.getNotes());
        BookingResponseDTO response = bookingMapper.toResponseDTO(createdBooking);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/me/bookings")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<BookingResponseDTO>> getMyBookings() {
        List<Booking> bookings = bookingService.findMyBookings();
        List<BookingResponseDTO> response = bookings.stream()
                .map(bookingMapper::toResponseDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me/bookings/{bookingUuid}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> getMyBookingByUuid(@PathVariable("bookingUuid") String bookingUuidStr) {
        UUID bookingUuid = uuidValidator.UUIDvalidateAndConvert(bookingUuidStr);
        // La autorización (@PreAuthorize en el servicio) se encarga de verificar que el usuario es el dueño.
        return bookingService.findBookingByUuid(bookingUuid)
                .map(bookingMapper::toResponseDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/me/bookings/{bookingUuid}/cancel")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BookingResponseDTO> cancelMyBooking(@PathVariable("bookingUuid") String bookingUuidStr) {
        UUID bookingUuid = uuidValidator.UUIDvalidateAndConvert(bookingUuidStr);
        Booking cancelledBooking = bookingService.cancelMyBooking(bookingUuid);
        return ResponseEntity.ok(bookingMapper.toResponseDTO(cancelledBooking));
    }
}