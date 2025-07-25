package application.service;

import domain.exception.*;
import domain.model.OfferedService;
import domain.model.Reservation;
import domain.model.User;
import domain.model.ReservationStatus;
import domain.port.in.ReservationService;
import domain.port.out.OfferedServicePersistencePort;
import domain.port.out.ReservationPersistencePort;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.RequesterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationPersistencePort reservationPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final OfferedServicePersistencePort offeredServicePersistencePort;

    private User getUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
    }

    @Override
    public Reservation createReservation(Reservation reservationDetails, Long ownerId, UUID serviceUuid, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(ownerId)) {
            throw new AccessDeniedException("You do not have permission to create a reservation for user ID " + ownerId + ".");
        }

        User user = getUserById(ownerId); // El usuario para quien se crea la reserva
        OfferedService service = offeredServicePersistencePort.findByUuid(serviceUuid)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found."));

        if (!service.getIsActive()) {
            throw new ServiceNotAvailableException("Service '" + service.getName() + "' is not active.");
        }
        if (reservationDetails.getStartTime().isAfter(reservationDetails.getEndTime())) {
            throw new InvalidReservationTimeException("Start time must be before end time.");
        }
        // Comprobación de solapamiento
        if (!reservationPersistencePort.findOverlappingReservations(serviceUuid, reservationDetails.getStartTime(), reservationDetails.getEndTime(), Optional.empty()).isEmpty()) {
            throw new ServiceNotAvailableException("The selected time slot is not available for this service.");
        }

        reservationDetails.setOwner(user);
        reservationDetails.setService(service);
        reservationDetails.setStatus(ReservationStatus.PENDING);
        if (reservationDetails.getPrice() == null) {
            reservationDetails.setPrice(service.getPricePerReservation());
        }

        return reservationPersistencePort.save(reservationDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findReservationByUuid(UUID reservationUuid, RequesterContext requester) {
        return reservationPersistencePort.findByUuid(reservationUuid)
                .filter(reservation -> requester.isAdmin() || requester.isOwner(reservation.getOwner().getId()));
    }

    @Override
    public Optional<Reservation> updateReservation(UUID reservationUuid, Reservation updateData, RequesterContext requester) {
        return reservationPersistencePort.findByUuid(reservationUuid)
                .map(existingReservation -> {
                    if (!requester.isAdmin() && !requester.isOwner(existingReservation.getOwner().getId())) {
                        throw new AccessDeniedException("You do not have permission to update this reservation.");
                    }

                    boolean timeChanged = false;
                    if (updateData.getStartTime() != null) {
                        existingReservation.setStartTime(updateData.getStartTime());
                        timeChanged = true;
                    }
                    if (updateData.getEndTime() != null) {
                        existingReservation.setEndTime(updateData.getEndTime());
                        timeChanged = true;
                    }
                    if (updateData.getNotes() != null) {
                        existingReservation.setNotes(updateData.getNotes());
                    }
                    if (requester.isAdmin() && updateData.getOwner() != null) {
                        existingReservation.setOwner(getUserById(updateData.getOwner().getId()));
                    }
                    if (requester.isAdmin() && updateData.getService() != null) {
                        existingReservation.setService(offeredServicePersistencePort.findByUuid(updateData.getService().getUuid())
                            .orElseThrow(() -> new OfferedServiceNotFoundException("New service not found")));
                    }
                    if (updateData.getPrice() != null) { existingReservation.setPrice(updateData.getPrice()); } // Si price es actualizable


                    if (timeChanged) {
                        if (existingReservation.getStartTime().isAfter(existingReservation.getEndTime())) {
                            throw new InvalidReservationTimeException("Start time must be before end time.");
                        }
                        if (!reservationPersistencePort.findOverlappingReservations(
                                existingReservation.getService().getUuid(),
                                existingReservation.getStartTime(),
                                existingReservation.getEndTime(),
                                Optional.of(reservationUuid)).isEmpty()) {
                            throw new ServiceNotAvailableException("The new time slot is not available.");
                        }
                    }

                    return reservationPersistencePort.save(existingReservation);
                });
    }

    @Override
    public boolean deleteReservation(UUID reservationUuid, RequesterContext requester) {
        return reservationPersistencePort.findByUuid(reservationUuid)
                .map(reservation -> {
                    if (!requester.isAdmin() && !requester.isOwner(reservation.getOwner().getId())) {
                        throw new AccessDeniedException("You do not have permission to delete this reservation.");
                    }
                    if (reservation.getStartTime().isAfter(Instant.now()) && (reservation.getStatus() == ReservationStatus.PENDING || reservation.getStatus() == ReservationStatus.CONFIRMED)) {
                        throw new IllegalStateException("Cannot delete active or future reservations.");
                    }
                    reservationPersistencePort.deleteByUuid(reservationUuid);
                    return true;
                }).orElse(false);
    }

    // --- Métodos para AdminReservationController ---

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllReservationsForAdmin(Optional<Long>  ownerIdParam, Optional<UUID> serviceUuid, Instant startDate, Instant endDate, RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can list reservations using admin filters.");
        }

        return reservationPersistencePort.findReservationsByFilters(
                ownerIdParam, serviceUuid, startDate, endDate
        );
    }

    // --- Métodos compartidos por AdminReservationController y MyReservationController ---

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByOwnerId(Long ownerId, RequesterContext requester) {
        // Autorización: Admin puede ver cualquier usuario. Usuario normal solo sus propias reservas.
        if (!requester.isAdmin() && !requester.isOwner(ownerId)) {
            throw new AccessDeniedException("No tienes permiso para ver las reservas de este usuario.");
        }
        User user = getUserById(ownerId); // Validar que el usuario propietario exista
        return reservationPersistencePort.findByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByServiceUuid(UUID serviceUuid, RequesterContext requester) {
        OfferedService service = offeredServicePersistencePort.findByUuid(serviceUuid)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found."));
        return reservationPersistencePort.findByOfferedService(service);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByDateRange(Instant startDate, Instant endDate, RequesterContext requester) {
        return reservationPersistencePort.findByDateRange(startDate, endDate);
    }

    // --- Métodos de Acción ---

    @Override
    public Reservation confirmReservation(UUID reservationUuid, RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can confirm reservations.");
        }
        Reservation reservation = reservationPersistencePort.findByUuid(reservationUuid)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationUuid + " not found."));

        reservation.confirm();
        return reservationPersistencePort.save(reservation);
    }

    @Override
    public Reservation cancelReservation(UUID reservationUuid, RequesterContext requester) {
        Reservation reservation = reservationPersistencePort.findByUuid(reservationUuid)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationUuid + " not found."));

        if (!requester.isAdmin() && !requester.isOwner(reservation.getOwner().getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this reservation.");
        }

        reservation.cancel();
        return reservationPersistencePort.save(reservation);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findMyReservationsByServiceUuid(Long ownerId, UUID serviceUuid, RequesterContext requester) {
        if (!requester.isOwner(ownerId)) {
            throw new AccessDeniedException("You can only view your own reservations.");
        }
        OfferedService service = offeredServicePersistencePort.findByUuid(serviceUuid)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with UUID " + serviceUuid + " not found."));

        return reservationPersistencePort.findFutureReservationsByOwnerIdAndService(ownerId, service, Instant.now());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findMyReservationsByDateRange(Long ownerId, Instant startDate, Instant endDate, RequesterContext requester) {
        // Autorización: Asegurarse de que el usuario está viendo sus propias reservas
        if (!requester.isOwner(ownerId)) {
            throw new AccessDeniedException("You can only view your own reservations.");
        }
        return reservationPersistencePort.findFutureReservationsByOwnerIdAndDateRange(ownerId, startDate, endDate);
    }

}