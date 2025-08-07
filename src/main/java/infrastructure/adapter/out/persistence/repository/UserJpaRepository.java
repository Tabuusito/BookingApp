package infrastructure.adapter.out.persistence.repository;

import domain.model.Role;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserJpaRepository extends JpaRepository<UserEntity, Long> {
    Optional<UserEntity> findByUsername(String username);
    Optional<UserEntity> findByEmail(String email);
    Optional<UserEntity> findByUuid(UUID uuid);
    void deleteByUuid(UUID uuid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    @Query("SELECT u FROM UserEntity u " +
            "WHERE :role MEMBER OF u.roles " +
            "AND LOWER(u.username) LIKE LOWER(CONCAT('%', :usernameQuery, '%'))")
    List<UserEntity> findByRoleAndUsernameContaining(
            @Param("role") Role role,
            @Param("usernameQuery") String usernameQuery
    );
}
