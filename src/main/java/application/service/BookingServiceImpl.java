package application.service;

import domain.exception.*;
import domain.model.*;
import domain.port.in.BookingService;
import domain.port.out.BookingPersistencePort;
import domain.port.out.TimeSlotPersistencePort;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class BookingServiceImpl implements BookingService {

    private final BookingPersistencePort bookingPersistencePort;
    private final TimeSlotPersistencePort timeSlotPersistencePort;
    private final UserPersistencePort userPersistencePort;

    @Override
    @PreAuthorize("hasRole('CLIENT')")
    public Booking createBooking(UUID timeSlotUuid, String notes) {
        // 1. Obtener el cliente desde el contexto de seguridad
        UUID clientUuid = ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();
        User client = userPersistencePort.findByUuid(clientUuid)
                .orElseThrow(() -> new UserNotFoundException("Authenticated client not found in database. Critical error."));

        // 2. Obtener el TimeSlot y validar su estado
        TimeSlot timeSlot = timeSlotPersistencePort.findByUuid(timeSlotUuid)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot with UUID " + timeSlotUuid + " not found."));

        if (timeSlot.getStatus() != TimeSlotStatus.AVAILABLE) {
            throw new ServiceNotAvailableException("This time slot is not available for booking. Status: " + timeSlot.getStatus());
        }

        // 3. Validar capacidad
        long currentBookings = bookingPersistencePort.countByTimeSlot(timeSlot);
        if (currentBookings >= timeSlot.getCapacity()) {
            throw new ServiceNotAvailableException("This time slot is full.");
        }

        // 4. Validar que el cliente no tenga ya una reserva
        if (bookingPersistencePort.existsByClientAndTimeSlot(client, timeSlot)) {
            throw new DuplicateBookingException("You already have a booking for this time slot.");
        }

        // 5. Crear el nuevo Booking
        Booking newBooking = Booking.builder()
                .client(client)
                .timeSlot(timeSlot)
                .notes(notes)
                .pricePaid(timeSlot.getPrice()) // Asumimos que el precio es el del slot
                .status(BookingStatus.CONFIRMED) // O PENDING_PAYMENT si hay un flujo de pago
                .build();

        Booking savedBooking = bookingPersistencePort.save(newBooking);

        // 6. Opcional: Si se ha llenado la última plaza, actualizar el estado del TimeSlot
        if (currentBookings + 1 >= timeSlot.getCapacity()) {
            timeSlot.setStatus(TimeSlotStatus.FULL);
            timeSlotPersistencePort.save(timeSlot);
        }

        return savedBooking;
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isBookingParticipant(#bookingUuid)")
    public Optional<Booking> findBookingByUuid(UUID bookingUuid) {
        return bookingPersistencePort.findByUuid(bookingUuid);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public List<Booking> findMyBookings() {
        UUID clientUuid = ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();
        User client = userPersistencePort.findByUuid(clientUuid)
                .orElseThrow(() -> new UserNotFoundException("Authenticated client not found."));

        return bookingPersistencePort.findByClient(client);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isBookingClient(#bookingUuid)")
    public Booking cancelMyBooking(UUID bookingUuid) {
        Booking booking = bookingPersistencePort.findByUuid(bookingUuid)
                .orElseThrow(() -> new BookingNotFoundException("Booking with UUID " + bookingUuid + " not found."));

        // Regla de negocio: No se puede cancelar una reserva que ya ha pasado
        if (booking.getTimeSlot().getStartTime().isBefore(Instant.now())) {
            throw new IllegalStateException("Cannot cancel a booking for a past event.");
        }

        booking.cancel(); // Usamos el método de dominio

        Booking savedBooking = bookingPersistencePort.save(booking);

        // Lógica de negocio adicional: Si el slot estaba lleno, ahora vuelve a estar disponible
        TimeSlot timeSlot = booking.getTimeSlot();
        if (timeSlot.getStatus() == TimeSlotStatus.FULL) {
            timeSlot.setStatus(TimeSlotStatus.AVAILABLE);
            timeSlotPersistencePort.save(timeSlot);
        }

        return savedBooking;
    }
}