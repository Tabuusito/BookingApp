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
    private final TimeSlotJpaRepository timeSlotJpaRepository;
    private final BookingMapper bookingMapper;
    private final UserMapper userMapper;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public Booking save(Booking booking) {
        // 1. Obtener la entidad "padre" (TimeSlot) desde la base de datos
        // Asumimos que el objeto de dominio 'booking' tiene el TimeSlot con su UUID
        UUID timeSlotUuid = booking.getTimeSlot().getUuid();
        TimeSlotEntity timeSlotEntity = timeSlotJpaRepository.findByUuid(timeSlotUuid)
                .orElseThrow(() -> new IllegalStateException("TimeSlot no encontrado para el booking. Inconsistencia de datos."));

        // 2. Mapear el objeto de dominio 'booking' a una entidad
        BookingEntity bookingEntity = bookingMapper.toEntity(booking);

        // 3.Encapsulamos la sincronización bidireccional
        timeSlotEntity.addBooking(bookingEntity);

        // 4. Guardar la entidad. Como la relación tiene CascadeType.PERSIST (o ALL),
        // al guardar el TimeSlot actualizado, también se insertará/actualizará el Booking.
        // O podemos guardar el booking directamente, ya que la entidad TimeSlot ya está
        // en el contexto de persistencia y Hibernate detectará su cambio.
        BookingEntity savedEntity = bookingJpaRepository.save(bookingEntity);

        // 5. Devolver el dominio mapeado
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