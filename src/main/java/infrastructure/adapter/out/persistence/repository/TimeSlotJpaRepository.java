package infrastructure.adapter.out.persistence.repository;

import infrastructure.adapter.out.persistence.entity.TimeSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TimeSlotJpaRepository extends JpaRepository<TimeSlotEntity, Long> {

    Optional<TimeSlotEntity> findByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    /**
     * Busca TimeSlots para un servicio específico que comiencen dentro de un rango de fechas.
     */
    List<TimeSlotEntity> findByOfferedService_UuidAndStartTimeBetween(UUID serviceUuid, Instant rangeStart, Instant rangeEnd);

    /**
     * Busca TimeSlots de un proveedor que se solapen con un rango de tiempo dado.
     * La condición de solapamiento es: (StartA < EndB) y (StartB < EndA).
     * El UUID de exclusión es para permitir la actualización de un slot existente sin que colisione consigo mismo.
     */
    @Query("SELECT ts FROM TimeSlotEntity ts " +
            "WHERE ts.offeredService.owner.uuid = :providerUuid " +
            "AND ts.startTime < :endTime " +
            "AND ts.endTime > :startTime " +
            "AND (:excludeUuid IS NULL OR ts.uuid <> :excludeUuid)")
    List<TimeSlotEntity> findOverlappingSlotsForProvider(
            @Param("providerUuid") UUID providerUuid,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime,
            @Param("excludeUuid") UUID excludeUuid
    );

    /**
     * Comprueba si existe al menos un TimeSlot para el UUID de un servicio
     * y cuya hora de inicio sea posterior a la fecha y hora proporcionadas.
     *
     * @param serviceUuid El UUID del OfferedService.
     * @param now La fecha y hora actual.
     * @return true si existe al menos un registro, false si no.
     */
    boolean existsByOfferedService_UuidAndStartTimeAfter(UUID serviceUuid, Instant now);
}