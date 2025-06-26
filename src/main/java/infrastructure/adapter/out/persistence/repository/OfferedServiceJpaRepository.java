package infrastructure.adapter.out.persistence.repository;

import infrastructure.adapter.out.persistence.entity.OfferedServiceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferedServiceJpaRepository extends JpaRepository<OfferedServiceEntity, Long> {

    List<OfferedServiceEntity> findByIsActiveTrue();

    List<OfferedServiceEntity> findByNameContainingIgnoreCase(String name);

    boolean existsByNameIgnoreCase(String name);


    @Modifying
    @Query("UPDATE OfferedServiceEntity s SET s.isActive = :isActive WHERE s.serviceId = :serviceId")
    int updateActiveStatus(@Param("serviceId") Long serviceId, @Param("isActive") boolean isActive);
    // Este método devolverá el número de filas afectadas. El adaptador necesitará verificar si fue 1
    // y luego, opcionalmente, recargar la entidad para devolver el objeto actualizado si el puerto lo requiere.

}
