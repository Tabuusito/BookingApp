package infrastructure.adapter.out.persistence.repository;

import domain.model.ReservationStatus;
import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import infrastructure.adapter.out.persistence.entity.ReservationEntity;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationJpaRepository extends JpaRepository<ReservationEntity, Long> {

    List<ReservationEntity> findByOwner(UserEntity userEntity);

    List<ReservationEntity> findByService(OfferedServiceEntity offeredServiceEntity);

    // Reservas que INICIAN dentro del rango.
    List<ReservationEntity> findByStartTimeBetween(LocalDateTime rangeStart, LocalDateTime rangeEnd);

    // Reservas que están ACTIVAS (se solapan) en cualquier punto del rango.
    @Query("SELECT r FROM ReservationEntity r WHERE r.startTime < :rangeEnd AND r.endTime > :rangeStart")
    List<ReservationEntity> findActiveInDateRange(@Param("rangeStart") LocalDateTime rangeStart, @Param("rangeEnd") LocalDateTime rangeEnd);


    // Solapamiento: (StartA < EndB) and (StartB < EndA)
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE r.service.serviceId = :serviceId " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "AND (:excludeReservationId IS NULL OR r.reservationId <> :excludeReservationId)")
    List<ReservationEntity> findOverlappingReservations(
            @Param("serviceId") Long serviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("excludeReservationId") Long excludeReservationId // Pasar null si no se excluye nada
    );
    // Es más seguro que el adaptador maneje el Optional y pase null si está vacío.


    List<ReservationEntity> findByStatus(ReservationStatus status);


    List<ReservationEntity> findByOwnerAndStartTimeAfter(UserEntity userEntity, LocalDateTime currentTime);


    @Query("SELECT COUNT(r) FROM ReservationEntity r " +
            "WHERE r.service.serviceId = :serviceId " +
            "AND r.startTime < :endTime " +
            "AND r.endTime > :startTime " +
            "AND (r.status = :pendingStatus OR r.status = :confirmedStatus)")
    long countActiveReservationsForServiceInSlot(
            @Param("serviceId") Long serviceId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            @Param("pendingStatus") ReservationStatus pendingStatus,
            @Param("confirmedStatus") ReservationStatus confirmedStatus
    );

    /**
     * Busca entidades de reserva para un ID de servicio específico y cuya hora de inicio sea posterior a la hora actual.
     * @param serviceId El ID del servicio.
     * @param dateTime La fecha y hora actual para comparar.
     * @return Una lista de entidades de reserva futuras para ese servicio.
     */
    List<ReservationEntity> findByServiceServiceIdAndStartTimeAfter(Long serviceId, LocalDateTime dateTime);

    /**
     * Busca entidades de reserva futuras para un ID de usuario específico.
     * Atraviesa la relación ReservationEntity -> UserEntity -> id.
     * @param userId El ID del usuario.
     * @param currentTime La fecha y hora actual para comparar.
     * @return Una lista de entidades de reserva futuras para ese usuario.
     */
    List<ReservationEntity> findByOwnerIdAndStartTimeAfter(Long userId, LocalDateTime currentTime);

    /**
     * Busca reservas por filtros combinados para administradores.
     * Permite filtrar opcionalmente por ownerId, serviceId, y rango de fechas.
     */
    @Query("SELECT r FROM ReservationEntity r " +
            "WHERE (:ownerId IS NULL OR r.owner.id = :ownerId) " +
            "AND (:serviceId IS NULL OR r.service.serviceId = :serviceId) " +
            "AND (:startDate IS NULL OR r.startTime >= :startDate) " +
            "AND (:endDate IS NULL OR r.endTime <= :endDate)")
    List<ReservationEntity> findReservationsByFilters(
            @Param("ownerId") Long ownerId,
            @Param("serviceId") Long serviceId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    /**
     * Busca reservas futuras para un propietario y servicio específicos.
     * @param ownerId ID del usuario propietario.
     * @param serviceEntity Entidad de servicio.
     * @param currentTime La hora actual.
     * @return Lista de reservas.
     */
    List<ReservationEntity> findByOwnerIdAndServiceAndStartTimeAfter(Long ownerId, OfferedServiceEntity serviceEntity, LocalDateTime currentTime);

    /**
     * Busca reservas futuras para un propietario dentro de un rango de fechas.
     * @param ownerId ID del usuario propietario.
     * @param startDate Fecha de inicio del rango.
     * @param endDate Fecha de fin del rango.
     * @return Lista de reservas.
     */
    List<ReservationEntity> findByOwnerIdAndStartTimeBetween(Long ownerId, LocalDateTime startDate, LocalDateTime endDate);
}

