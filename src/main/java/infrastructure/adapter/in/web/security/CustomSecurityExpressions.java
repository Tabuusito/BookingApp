package infrastructure.adapter.in.web.security;

import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.BookingPersistencePort;
import domain.port.out.TimeSlotPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component("customSecurity")
@RequiredArgsConstructor
public class CustomSecurityExpressions {

    private final OfferedServicePersistencePort offeredServicePersistencePort;
    private final BookingPersistencePort bookingPersistencePort;
    private final TimeSlotPersistencePort timeSlotPersistencePort;

    // --- Métodos de Autorización ---

    /**
     * Verifica si el usuario autenticado es el propietario del servicio especificado.
     */
    public boolean isServiceOwner(UUID serviceUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RequesterContext requester = createRequesterContext(authentication);

        // Buscamos el servicio y comprobamos si su dueño coincide con el requester
        return offeredServicePersistencePort.findByUuid(serviceUuid)
                .map(service -> service.getOwner().getUuid().equals(requester.userUuid().orElse(null)))
                .orElse(false); // Si el servicio no existe, no es el dueño
    }

    /**
     * Verifica si el usuario autenticado es el cliente de un booking específico.
     */
    public boolean isBookingClient(UUID bookingUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RequesterContext requester = createRequesterContext(authentication);

        return bookingPersistencePort.findByUuid(bookingUuid)
                .map(booking -> booking.getClient().getUuid().equals(requester.userUuid().orElse(null)))
                .orElse(false);
    }

    public boolean isBookingParticipant(UUID bookingUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        // Es participante si es el cliente O el proveedor del servicio del slot
        return isBookingClient(bookingUuid) || isProviderOfBooking(bookingUuid);
    }

    /**
     * Verifica si el usuario autenticado es el proveedor del servicio asociado a un booking.
     */
    public boolean isProviderOfBooking(UUID bookingUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RequesterContext requester = createRequesterContext(authentication);

        return bookingPersistencePort.findByUuid(bookingUuid)
                .map(booking -> booking.getTimeSlot().getOfferedService().getOwner().getUuid().equals(requester.userUuid().orElse(null)))
                .orElse(false);
    }

    /**
     * Verifica si el usuario autenticado es él mismo (para perfiles de usuario).
     */
    public boolean isSelf(UUID userUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RequesterContext requester = createRequesterContext(authentication);
        return requester.isOwner(userUuid);
    }


    private RequesterContext createRequesterContext(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof SpringSecurityUser)) {
            return new RequesterContext(Optional.empty(), Set.of());
        }
        SpringSecurityUser springUser = (SpringSecurityUser) authentication.getPrincipal();
        Set<String> roles = springUser.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .collect(Collectors.toSet());
        return new RequesterContext(Optional.of(springUser.getUuid()), roles);
    }

    public boolean isTimeSlotProvider(UUID timeSlotUuid) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        RequesterContext requester = createRequesterContext(authentication);
        return timeSlotPersistencePort.findByUuid(timeSlotUuid)
                .map(ts -> ts.getOfferedService().getOwner().getUuid().equals(requester.userUuid().orElse(null)))
                .orElse(false);
    }
}