package domain.port.out;


import domain.model.User;

import java.util.Optional;

public interface UserRepositoryPort {

    Optional<User> findByUsername(String username);
    User save(User user);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
}