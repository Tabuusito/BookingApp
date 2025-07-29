package infrastructure.adapter.out.persistence.repository;

import domain.model.TimeSlot;
import domain.port.out.TimeSlotPersistencePort;
import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import infrastructure.adapter.out.persistence.mapper.TimeSlotMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class TimeSlotPersistenceAdapter implements TimeSlotPersistencePort {

    private final TimeSlotJpaRepository timeSlotJpaRepository;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public TimeSlot save(TimeSlot timeSlot) {
        TimeSlotEntity entity = timeSlotMapper.toEntity(timeSlot);
        TimeSlotEntity savedEntity = timeSlotJpaRepository.save(entity);
        return timeSlotMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<TimeSlot> findByUuid(UUID timeSlotUuid) {
        return timeSlotJpaRepository.findByUuid(timeSlotUuid)
                .map(timeSlotMapper::toDomain);
    }

    @Override
    public void deleteByUuid(UUID timeSlotUuid) {
        timeSlotJpaRepository.deleteByUuid(timeSlotUuid);
    }

    @Override
    public List<TimeSlot> findByServiceUuidAndStartTimeBetween(UUID serviceUuid, Instant rangeStart, Instant rangeEnd) {
        List<TimeSlotEntity> entities = timeSlotJpaRepository.findByOfferedService_UuidAndStartTimeBetween(serviceUuid, rangeStart, rangeEnd);
        return timeSlotMapper.toDomainList(entities);
    }

    @Override
    public List<TimeSlot> findOverlappingSlotsForProvider(UUID providerUuid, Instant startTime, Instant endTime, Optional<UUID> excludeTimeSlotUuid) {
        List<TimeSlotEntity> entities = timeSlotJpaRepository.findOverlappingSlotsForProvider(
                providerUuid,
                startTime,
                endTime,
                excludeTimeSlotUuid.orElse(null) // JpaRepository espera el valor o null
        );
        return timeSlotMapper.toDomainList(entities);
    }

    @Override
    public boolean hasFutureTimeSlots(UUID serviceUuid) {
        return timeSlotJpaRepository.existsByOfferedService_UuidAndStartTimeAfter(serviceUuid, Instant.now());
    }
}