package application.service;

import domain.exception.OfferedServiceNotFoundException;
import domain.exception.TimeSlotClashException;
import domain.exception.TimeSlotNotFoundException;
import domain.model.OfferedService;
import domain.model.TimeSlot;
import domain.model.TimeSlotStatus;
import domain.port.in.TimeSlotService;
import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.TimeSlotPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class TimeSlotServiceImpl implements TimeSlotService {

    private final TimeSlotPersistencePort timeSlotPersistencePort;
    private final OfferedServicePersistencePort offeredServicePersistencePort;

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isServiceOwner(#serviceUuid)")
    public TimeSlot createTimeSlot(TimeSlot timeSlot, UUID serviceUuid) {
        // 1. Validar que el servicio asociado existe
        OfferedService service = offeredServicePersistencePort.findByUuid(serviceUuid)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found."));

        // 2. Comprobar si hay solapamiento de horarios para este proveedor
        UUID providerUuid = service.getOwner().getUuid();
        if (!timeSlotPersistencePort.findOverlappingSlotsForProvider(providerUuid, timeSlot.getStartTime(), timeSlot.getEndTime(), Optional.empty()).isEmpty()) {
            throw new TimeSlotClashException("The proposed time slot clashes with an existing one for this provider.");
        }

        // 3. Completar los datos del TimeSlot
        timeSlot.setOfferedService(service);
        timeSlot.setStatus(TimeSlotStatus.AVAILABLE);

        // Si no se especifica capacidad o precio en el slot, heredar del servicio
        if (timeSlot.getCapacity() == null) {
            timeSlot.setCapacity(service.getCapacity());
        }
        if (timeSlot.getPrice() == null) {
            timeSlot.setPrice(service.getPricePerReservation());
        }

        return timeSlotPersistencePort.save(timeSlot);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isTimeSlotProvider(#timeSlotUuid)")
    public TimeSlot cancelTimeSlot(UUID timeSlotUuid) {
        TimeSlot timeSlot = timeSlotPersistencePort.findByUuid(timeSlotUuid)
                .orElseThrow(() -> new TimeSlotNotFoundException("TimeSlot with UUID " + timeSlotUuid + " not found."));

        // Cambiar estado del TimeSlot
        timeSlot.setStatus(TimeSlotStatus.CANCELLED);

        // Lógica de negocio adicional: Cancelar todos los bookings asociados
        // y notificar a los clientes. Esto podría delegarse a otro servicio (BookingService)
        // o a un manejador de eventos para desacoplar.
        timeSlot.getBookings().forEach(booking -> {
            // Suponiendo que el Booking tiene un método para ser cancelado por el proveedor
            // booking.cancelByProvider();
        });

        return timeSlotPersistencePort.save(timeSlot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TimeSlot> findTimeSlotByUuid(UUID timeSlotUuid) {
        return timeSlotPersistencePort.findByUuid(timeSlotUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TimeSlot> findAvailableTimeSlots(UUID serviceUuid, Instant from, Instant to) {
        // Podríamos filtrar adicionalmente para no devolver slots llenos.
        return timeSlotPersistencePort.findByServiceUuidAndStartTimeBetween(serviceUuid, from, to);
    }

}
