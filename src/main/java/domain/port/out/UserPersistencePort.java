package domain.port.out;


import domain.model.User;

import java.util.List;
import java.util.Optional;

public interface UserPersistencePort {

    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}