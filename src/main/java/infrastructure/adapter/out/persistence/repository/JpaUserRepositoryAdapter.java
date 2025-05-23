package infrastructure.adapter.out.persistence.repository;

import domain.model.User;
import domain.port.out.UserRepositoryPort;
import jakarta.persistence.*;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    @PersistenceContext // Inyecta el EntityManager, que gestiona la persistencia
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        String jpql = "SELECT u FROM Usuario u WHERE u.email = :email";

        try {
            TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
            query.setParameter("email", email);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
