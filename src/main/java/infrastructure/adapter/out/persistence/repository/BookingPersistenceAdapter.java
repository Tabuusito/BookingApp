package infrastructure.adapter.out.persistence.repository;

import domain.model.Booking;
import domain.model.TimeSlot;
import domain.model.User;
import domain.port.out.BookingPersistencePort;
import infrastructure.adapter.out.persistence.entity.BookingEntity;
import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import infrastructure.adapter.out.persistence.mapper.BookingMapper;
import infrastructure.adapter.out.persistence.mapper.TimeSlotMapper;
import infrastructure.adapter.out.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class BookingPersistenceAdapter implements BookingPersistencePort {

    private final BookingJpaRepository bookingJpaRepository;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public Booking save(Booking booking) {
        BookingEntity entity = bookingMapper.toEntity(booking);
        BookingEntity savedEntity = bookingJpaRepository.save(entity);
        return bookingMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<Booking> findByUuid(UUID bookingUuid) {
        return bookingJpaRepository.findByUuid(bookingUuid)
                .map(bookingMapper::toDomain);
    }

    @Override
    public void deleteByUuid(UUID bookingUuid) {
        bookingJpaRepository.deleteByUuid(bookingUuid);
    }

    @Override
    public List<Booking> findByClient(User client) {
        UserEntity clientEntity = userMapper.toEntity(client);
        List<BookingEntity> entities = bookingJpaRepository.findByClient(clientEntity);
        return bookingMapper.toDomainList(entities);
    }

    @Override
    public List<Booking> findByTimeSlot(TimeSlot timeSlot) {
        TimeSlotEntity timeSlotEntity = timeSlotMapper.toEntity(timeSlot);
        List<BookingEntity> entities = bookingJpaRepository.findByTimeSlot(timeSlotEntity);
        return bookingMapper.toDomainList(entities);
    }

    @Override
    public long countByTimeSlot(TimeSlot timeSlot) {
        TimeSlotEntity timeSlotEntity = timeSlotMapper.toEntity(timeSlot);
        return bookingJpaRepository.countByTimeSlot(timeSlotEntity);
    }

    @Override
    public boolean existsByClientAndTimeSlot(User client, TimeSlot timeSlot) {
        UserEntity clientEntity = userMapper.toEntity(client);
        TimeSlotEntity timeSlotEntity = timeSlotMapper.toEntity(timeSlot);
        return bookingJpaRepository.existsByClientAndTimeSlot(clientEntity, timeSlotEntity);
    }
}