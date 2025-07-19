package infrastructure.adapter.out.persistence.repository;

import infrastructure.adapter.out.persistence.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}
