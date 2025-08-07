package infrastructure.adapter.out.persistence.repository;

import domain.model.Role;
import domain.model.User;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.out.persistence.entity.UserEntity;
import infrastructure.adapter.out.persistence.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserPersistenceAdapter implements UserPersistencePort {

    private final UserJpaRepository userJpaRepository;
    private final UserMapper userMapper;

    @Override
    public User save(User user) {
        UserEntity userEntity = userMapper.toEntity(user);
        UserEntity savedEntity = userJpaRepository.save(userEntity);
        return userMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<User> findById(Long id) {
        return userJpaRepository.findById(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUuid(UUID id) {
        return userJpaRepository.findByUuid(id).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userJpaRepository.findByUsername(username).map(userMapper::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userJpaRepository.findByEmail(email).map(userMapper::toDomain);
    }

    @Override
    public List<User> findAll() {
        return userJpaRepository.findAll().stream()
                .map(userMapper::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        userJpaRepository.deleteById(id);
    }

    @Override
    public void deleteByUuid(UUID uuid) {
        userJpaRepository.deleteByUuid(uuid);
    }

    @Override
    public Boolean existsByUsername(String username) {
        return userJpaRepository.existsByUsername(username);
    }

    @Override
    public Boolean existsByEmail(String email) {
        return userJpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findUsersByRoleAndUsernameContaining(Role role, String usernameQuery) {
        List<UserEntity> userEntities = userJpaRepository.findByRoleAndUsernameContaining(role, usernameQuery);

        return userMapper.toDomainList(userEntities);
    }
}

