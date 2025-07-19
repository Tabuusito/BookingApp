package infrastructure.adapter.out.persistence.repository;

import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OfferedServiceJpaRepository extends JpaRepository<OfferedServiceEntity, Long> {

    Optional<OfferedServiceEntity> findByUuid(UUID uuid);

    boolean existsByUuid(UUID uuid);

    void deleteByUuid(UUID uuid);

    @Modifying
    @Query("UPDATE OfferedServiceEntity s SET s.isActive = :isActive WHERE s.uuid = :uuid")
    int updateActiveStatusByUuid(@Param("uuid") UUID uuid, @Param("isActive") boolean isActive);

    List<OfferedServiceEntity> findByIsActiveTrue();

    List<OfferedServiceEntity> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);


    @Modifying
    @Query("UPDATE OfferedServiceEntity s SET s.isActive = :isActive WHERE s.uuid = :serviceUuid")
    int updateActiveStatus(@Param("serviceUuid") UUID serviceUuid, @Param("isActive") boolean isActive);

    List<OfferedServiceEntity> findByNameContainingIgnoreCaseAndIsActive(String name, boolean isActive);

    boolean existsByNameIgnoreCaseAndOwnerId(String name, Long ownerId);


    List<OfferedServiceEntity> findByOwnerIdAndIsActive(Long ownerId, boolean isActive);

    List<OfferedServiceEntity> findByOwnerId(Long ownerUuid);

    List<OfferedServiceEntity> findByNameContainingIgnoreCaseAndOwnerIdAndIsActive(String nameFragment, Long ownerId, boolean isActive);
}
