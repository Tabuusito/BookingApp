package infrastructure.adapter.out.persistence.repository;

import domain.model.User;
import domain.port.out.UserRepositoryPort;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepositoryPort {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        String jpql = "SELECT u FROM User u WHERE u.username = :username";

        try {
            TypedQuery<User> query = entityManager.createQuery(jpql, User.class);
            query.setParameter("username", username);

            return Optional.of(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Transactional
    public User save(User user){
        entityManager.persist(user);
        return user;
    }

    @Transactional(readOnly = true)
    public Boolean existsByEmail(String email){
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.email = :email";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("email", email);

        return query.getSingleResult() > 0L;
    }

    @Transactional(readOnly = true)
    public Boolean existsByUsername(String username){
        String jpql = "SELECT COUNT(u) FROM User u WHERE u.username = :username";

        TypedQuery<Long> query = entityManager.createQuery(jpql, Long.class);
        query.setParameter("username", username);

        return query.getSingleResult() > 0L;
    }
}
