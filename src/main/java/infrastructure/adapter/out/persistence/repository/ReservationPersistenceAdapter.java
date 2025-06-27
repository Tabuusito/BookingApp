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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
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
    public Optional<Reservation> findById(Long reservationId) {
        return reservationJpaRepository.findById(reservationId)
                .map(reservationMapper::toDomain);
    }

    @Override
    public void deleteById(Long reservationId) {
        reservationJpaRepository.deleteById(reservationId);
    }

    @Override
    public List<Reservation> findAll() {
        return reservationJpaRepository.findAll().stream()
                .map(reservationMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Reservation> findByUser(User user) {
        UserEntity userEntity = userMapper.toEntity(user); // Mapear el User del dominio a UserEntity
        List<ReservationEntity> entities = reservationJpaRepository.findByUser(userEntity);
        return reservationMapper.toDomainList(entities);
    }


    @Override
    public List<Reservation> findByOfferedService(OfferedService service) {
        OfferedServiceEntity serviceEntity = offeredServiceMapper.toEntity(service);
        List<ReservationEntity> entities = reservationJpaRepository.findByService(serviceEntity);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findByDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        List<ReservationEntity> entities = reservationJpaRepository.findActiveInDateRange(rangeStart, rangeEnd);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public List<Reservation> findOverlappingReservations(Long serviceId, LocalDateTime startTime, LocalDateTime endTime, Optional<Long> excludeReservationIdOpt) {
        Long excludeId = excludeReservationIdOpt.orElse(null); // JpaRepository espera el valor o null
        List<ReservationEntity> entities = reservationJpaRepository.findOverlappingReservations(
                serviceId, startTime, endTime, excludeId
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
    public List<Reservation> findFutureReservationsByUser(User user, LocalDateTime currentTime) {
        UserEntity userEntity = userMapper.toEntity(user);
        List<ReservationEntity> entities = reservationJpaRepository.findByUserAndStartTimeAfter(userEntity, currentTime);
        return reservationMapper.toDomainList(entities);
    }

    @Override
    public long countActiveReservationsForServiceInSlot(Long serviceId, LocalDateTime startTime, LocalDateTime endTime) {
        return reservationJpaRepository.countActiveReservationsForServiceInSlot(
                serviceId,
                startTime,
                endTime,
                ReservationStatus.PENDING,
                ReservationStatus.CONFIRMED
        );
    }

    @Override
    public List<Reservation> findFutureReservationsByOfferedServiceId(Long serviceId) {
        List<ReservationEntity> futureReservationEntities =
                reservationJpaRepository.findByServiceServiceIdAndStartTimeAfter(serviceId, LocalDateTime.now());

        return reservationMapper.toDomainList(futureReservationEntities);
    }
}
