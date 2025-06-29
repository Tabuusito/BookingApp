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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ReservationServiceImpl implements ReservationService {

    private final ReservationPersistencePort reservationPersistencePort;
    private final UserPersistencePort userPersistencePort;
    private final OfferedServicePersistencePort offeredServicePersistencePort;

    @Override
    public Reservation createReservation(Reservation reservationDetails, Long userId, Long serviceId, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(userId)) {
            throw new AccessDeniedException("You do not have permission to create a reservation for this user.");
        }

        User user = userPersistencePort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
        OfferedService service = offeredServicePersistencePort.findById(serviceId)
                .orElseThrow(() -> new OfferedServiceNotFoundException("Service with ID " + serviceId + " not found."));

        if (!service.getIsActive()) {
            throw new ServiceNotAvailableException("Service '" + service.getName() + "' is not active.");
        }
        if (reservationDetails.getStartTime().isAfter(reservationDetails.getEndTime())) {
            throw new InvalidReservationTimeException("Start time must be before end time.");
        }
        if (!reservationPersistencePort.findOverlappingReservations(serviceId, reservationDetails.getStartTime(), reservationDetails.getEndTime(), Optional.empty()).isEmpty()) {
            throw new ServiceNotAvailableException("The selected time slot is not available for this service.");
        }

        reservationDetails.setUser(user);
        reservationDetails.setService(service);
        reservationDetails.setStatus(ReservationStatus.PENDING);
        if (reservationDetails.getPrice() == null) {
            reservationDetails.setPrice(service.getPricePerReservation());
        }

        return reservationPersistencePort.save(reservationDetails);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Reservation> findReservationById(Long reservationId, RequesterContext requester) {
        return reservationPersistencePort.findById(reservationId)
                .filter(reservation -> requester.isAdmin() || requester.isOwner(reservation.getUser().getId()));
    }

    @Override
    public Optional<Reservation> updateReservation(Long reservationId, Reservation updateData, RequesterContext requester) {
        return reservationPersistencePort.findById(reservationId)
                .map(existingReservation -> {
                    if (!requester.isAdmin() && !requester.isOwner(existingReservation.getUser().getId())) {
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
                    if(requester.isAdmin()){
                        if(updateData.getStatus() != null){
                            existingReservation.setStatus(updateData.getStatus());
                        }
                    }
                    if (timeChanged) {
                        if (existingReservation.getStartTime().isAfter(existingReservation.getEndTime())) {
                            throw new InvalidReservationTimeException("Start time must be before end time.");
                        }
                        if (!reservationPersistencePort.findOverlappingReservations(
                                existingReservation.getService().getServiceId(),
                                existingReservation.getStartTime(),
                                existingReservation.getEndTime(),
                                Optional.of(reservationId)).isEmpty()) {
                            throw new ServiceNotAvailableException("The new time slot is not available.");
                        }
                    }

                    return reservationPersistencePort.save(existingReservation);
                });
    }

    @Override
    public boolean deleteReservation(Long reservationId, RequesterContext requester) {
        return reservationPersistencePort.findById(reservationId)
                .map(reservation -> {
                    if (!requester.isAdmin() && !requester.isOwner(reservation.getUser().getId())) {
                        throw new AccessDeniedException("You do not have permission to delete this reservation.");
                    }
                    reservationPersistencePort.deleteById(reservationId);
                    return true;
                }).orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findAllReservations(RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can view all reservations.");
        }
        return reservationPersistencePort.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByUserId(Long userId, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(userId)) {
            throw new AccessDeniedException("You do not have permission to view reservations for this user.");
        }
        return reservationPersistencePort.findFutureReservationsByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByServiceId(Long serviceId, RequesterContext requester) {
        // No se aplica lógica de autorización aquí, asumiendo que es público.
        return reservationPersistencePort.findFutureReservationsByOfferedServiceId(serviceId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Reservation> findReservationsByDateRange(LocalDateTime startDate, LocalDateTime endDate, RequesterContext requester) {
        // No se aplica lógica de autorización aquí, asumiendo que es público.
        return reservationPersistencePort.findByDateRange(startDate, endDate);
    }

    @Override
    public Reservation confirmReservation(Long reservationId, RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("Only administrators can confirm reservations.");
        }
        Reservation reservation = reservationPersistencePort.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));

        reservation.confirm();
        return reservationPersistencePort.save(reservation);
    }

    @Override
    public Reservation cancelReservation(Long reservationId, RequesterContext requester) {
        Reservation reservation = reservationPersistencePort.findById(reservationId)
                .orElseThrow(() -> new ReservationNotFoundException("Reservation with ID " + reservationId + " not found."));

        if (!requester.isAdmin() && !requester.isOwner(reservation.getUser().getId())) {
            throw new AccessDeniedException("You do not have permission to cancel this reservation.");
        }

        reservation.cancel();
        return reservationPersistencePort.save(reservation);
    }
}