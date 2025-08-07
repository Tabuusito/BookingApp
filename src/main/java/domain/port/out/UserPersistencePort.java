package domain.port.out;


import domain.model.Role;
import domain.model.User;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserPersistencePort {

    User save(User user);
    Optional<User> findById(Long id);
    Optional<User> findByUuid(UUID uuid);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    List<User> findAll();
    void deleteById(Long id);
    void deleteByUuid(UUID uuid);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);
    List<User> findUsersByRoleAndUsernameContaining(Role role, String usernameQuery);
}