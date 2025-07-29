package infrastructure.adapter.out.persistence.repository;

import infrastructure.adapter.out.persistence.entity.BookingEntity;
import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface BookingJpaRepository extends JpaRepository<BookingEntity, Long> {

    Optional<BookingEntity> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    /**
     * Busca todos los bookings de un cliente específico.
     */
    List<BookingEntity> findByClient(UserEntity client);

    /**
     * Busca todos los bookings de un TimeSlot específico.
     */
    List<BookingEntity> findByTimeSlot(TimeSlotEntity timeSlot);

    /**
     * Cuenta el número de bookings para un TimeSlot. Mucho más eficiente que traer la lista y contarla.
     */
    long countByTimeSlot(TimeSlotEntity timeSlot);

    /**
     * Verifica si existe un booking para una combinación de cliente y TimeSlot.
     */
    boolean existsByClientAndTimeSlot(UserEntity client, TimeSlotEntity timeSlot);
}