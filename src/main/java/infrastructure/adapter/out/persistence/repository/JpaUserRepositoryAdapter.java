package infrastructure.adapter.out.persistence.repository;

import domain.model.User;
import domain.port.out.UserRepositoryPort;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    @PersistenceContext // Inyecta el EntityManager, que gestiona la persistencia
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        String jpql = "SELECT u FROM usuarios u WHERE u.username = :username";

        try {
            TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
            query.setParameter("username", username);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
