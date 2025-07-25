package infrastructure.adapter.out.persistence.repository;

import domain.model.OfferedService;
import domain.model.Reservation;
import domain.model.ReservationStatus;
import domain.model.User;
import domain.port.out.ReservationPersistencePort;
import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import infrastructure.adapter.out.persistence.entity.ReservationEntity;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import infrastructure.adapter.out.persistence.mapper.OfferedServiceMapper;
import infrastructure.adapter.out.persistence.mapper.ReservationMapper;
import infrastructure.adapter.out.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class ReservationPersistenceAdapter implements ReservationPersistencePort {

    private final ReservationJpaRepository reservationJpaRepository;
    private final ReservationMapper reservationMapper;
    private final UserMapper userMapper;
    private final OfferedServiceMapper offeredServiceMapper;


    @Override
    public Reservation save(Reservation reservation) {
        ReservationEntity reservationEntity = reservationMapper.toEntity(reservation);
        // reservationEntity.setUser(userMapper.toEntity(reservation.getUser()));
        // reservationEntity.setService(offeredServiceMapper.toEntity(reservation.getService()));
        // Esto ya debería estar cubierto por ReservationMapper al usar UserMapper y OfferedServiceMapper.
        ReservationEntity savedEntity = reservationJpaRepository.save(reservationEntity);
        return reservationMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Reservation> findByUuid(UUID reservationUuid) {
        return reservationJpaRepository.findByUuid(reservationUuid)
                .map(reservationMapper::toDomain);
    }

    @Override
    public void deleteByUuid(UUID reservationUuid) {
        reservationJpaRepository.deleteByUuid(reservationUuid);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
                .map(reservationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByUser(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        List<ReservationEntity> entities = reservationJpaRepository.findByOwner(userEntity);
        return reservationMapper.toDomainList(entities);
    }


    @Override
    public List<Reservation> findByOfferedService(OfferedService service) {
        OfferedServiceEntity serviceEntity = offeredServiceMapper.toEntity(service);
        List<ReservationEntity> entities = reservationJpaRepository.findByService(serviceEntity);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findByDateRange(Instant rangeStart, Instant rangeEnd) {
        List<ReservationEntity> entities = reservationJpaRepository.findActiveInDateRange(rangeStart, rangeEnd);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findOverlappingReservations(UUID serviceUuid, Instant startTime, Instant endTime, Optional<UUID> excludeReservationUuidOpt) {
        UUID excludeUuid = excludeReservationUuidOpt.orElse(null); // JpaRepository espera el valor o null
        List<ReservationEntity> entities = reservationJpaRepository.findOverlappingReservations(
                serviceUuid, startTime, endTime, excludeUuid
        );
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findByStatus(ReservationStatus status) {
        ReservationStatus statusEntity = ReservationStatus.valueOf(status.name());
        List<ReservationEntity> entities = reservationJpaRepository.findByStatus(statusEntity);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findFutureReservationsByOwner(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        List<ReservationEntity> entities = reservationJpaRepository.findByOwnerAndStartTimeAfter(userEntity, LocalDateTime.now());
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findFutureReservationsByOwnerId(Long userId) {
        List<ReservationEntity> entities = reservationJpaRepository.findByOwnerIdAndStartTimeAfter(userId, Instant.now());
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public long countActiveReservationsForServiceInSlot(UUID serviceUuid, Instant startTime, Instant endTime) {
        return reservationJpaRepository.countActiveReservationsForServiceInSlot(
                serviceUuid,
                startTime,
                endTime,
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );
    }

    @Override
    public List<Reservation> findFutureReservationsByOfferedServiceUuid(UUID serviceUuid) {
        List<ReservationEntity> futureReservationEntities =
                reservationJpaRepository.findByServiceUuidAndStartTimeAfter(serviceUuid, Instant.now());

        return reservationMapper.toDomainList(futureReservationEntities);
    }

    @Override
    public List<Reservation> findReservationsByFilters(Optional<Long> ownerIdParam, Optional<UUID> serviceUuid, Instant startDate, Instant endDate) {
        List<ReservationEntity> entities = reservationJpaRepository.findReservationsByFilters(
                ownerIdParam.orElse(null),
                serviceUuid.orElse(null),
                startDate,
                endDate
        );
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findFutureReservationsByOwnerIdAndService(Long ownerId, OfferedService service, Instant currentTime) {
        OfferedServiceEntity serviceEntity = offeredServiceMapper.toEntity(service);
        List<ReservationEntity> entities = reservationJpaRepository.findByOwnerIdAndServiceAndStartTimeAfter(ownerId, serviceEntity, currentTime);
        return reservationMapper.toDomainList(entities);
    }


    @Override
    public List<Reservation> findFutureReservationsByOwnerIdAndDateRange(Long ownerId, Instant startDate, Instant endDate) {
        List<ReservationEntity> entities = reservationJpaRepository.findByOwnerIdAndStartTimeBetween(ownerId, startDate, endDate);
        return reservationMapper.toDomainList(entities);
    }
}
