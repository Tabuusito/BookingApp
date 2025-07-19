package application.service;

import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.RequesterContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional; // Asegúrate de este import
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    // Métodos de ayuda (si siguen siendo usados solo aquí)
    private User getUserById(Long userId) {
        return userPersistencePort.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User with ID " + userId + " not found."));
    }

    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + username));
    }

    @Override
    public User createUser(User newUser, RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("No tienes permiso para crear usuarios.");
        }

        if (userPersistencePort.existsByUsername(newUser.getUsername())) {
            throw new DuplicateUserInfoException("Nombre de usuario ya existe");
        }
        if (userPersistencePort.existsByEmail(newUser.getEmail())) {
            throw new DuplicateUserInfoException("Email ya existe");
        }

        if (newUser.getPasswordHash() != null && !newUser.getPasswordHash().isBlank()) {
            newUser.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
        } else {
            throw new IllegalArgumentException("La contraseña no puede estar vacía.");
        }

        if (newUser.getRole() == null) {
            newUser.setRole(Role.USER);
        }

        return userPersistencePort.save(newUser);
    }

    @Override
    @Transactional
    public User updateUser(User userToUpdate, RequesterContext requester) {
        Long userIdToUpdate = userToUpdate.getId();
        if (userIdToUpdate == null) {
            throw new IllegalArgumentException("Debe proveerse una ID para actualizar.");
        }

        User existingUser = userPersistencePort.findById(userIdToUpdate)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + userIdToUpdate));

        // Autorización: ADMIN puede actualizar cualquier usuario. Usuario normal solo su propio perfil.
        // Si no es admin Y no es el propietario, se deniega el acceso.
        if (!requester.isAdmin() && !requester.isOwner(userIdToUpdate)) {
            throw new AccessDeniedException("No tienes permiso para actualizar este usuario.");
        }

        // Un usuario normal no puede cambiarse a sí mismo a inactivo o cambiar roles
        // La validación de cambio de rol o estado 'active' solo si es admin.
        if (!requester.isAdmin()) {
            if (userToUpdate.getRole() != null && userToUpdate.getRole() != existingUser.getRole()) {
                throw new AccessDeniedException("No tienes permiso para actualizar roles.");
            }
            if (userToUpdate.getActive() != null && userToUpdate.getActive() != existingUser.getActive()) {
                throw new AccessDeniedException("No tienes permiso para cambiar el estado activo.");
            }
        }
        // Si el rol viene en userToUpdate y no es admin, se ignora o se lanza excepción
        // Si el active viene en userToUpdate y no es admin, se ignora o se lanza excepción

        // Aplicar los cambios y validar unicidad
        return applyUserUpdates(existingUser, userToUpdate, requester.isAdmin());
    }

    @Override
    @Transactional
    public User updateMyProfile(User userToUpdate, RequesterContext requester) {
        // Asegurarse de que el usuario esté autenticado para llamar a "mi perfil"
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado para actualizar su perfil."));

        // Forzar que la actualización sea sobre el ID del propio usuario
        userToUpdate.setId(myUserId);

        // Cargar el usuario existente
        User existingUser = userPersistencePort.findById(myUserId)
                .orElseThrow(() -> new UserNotFoundException("El perfil del usuario autenticado no fue encontrado.")); // Inconsistencia de datos

        // Aplicar los cambios. Las comprobaciones de rol se harán en applyUserUpdates.
        // Un usuario normal (no admin) no puede cambiar su propio rol o estado 'active'.
        return applyUserUpdates(existingUser, userToUpdate, requester.isAdmin());
    }

    private User applyUserUpdates(User existingUser, User updateData, boolean isAdmin) {
        if (updateData.getUsername() != null && !updateData.getUsername().isBlank() && !updateData.getUsername().equals(existingUser.getUsername())) {
            if (userPersistencePort.existsByUsername(updateData.getUsername())) {
                throw new DuplicateUserInfoException("El username '" + updateData.getUsername() + "' ya está en uso.");
            }
            existingUser.setUsername(updateData.getUsername());
        }

        if (updateData.getEmail() != null && !updateData.getEmail().isBlank() && !updateData.getEmail().equals(existingUser.getEmail())) {
            if (userPersistencePort.existsByEmail(updateData.getEmail())) {
                throw new DuplicateUserInfoException("El email '" + updateData.getEmail() + "' ya está en uso.");
            }
            existingUser.setEmail(updateData.getEmail());
        }

        if (updateData.getPasswordHash() != null && !updateData.getPasswordHash().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(updateData.getPasswordHash()));
        }

        // Solo admin puede cambiar el rol o el estado activo
        if (isAdmin) {
            if (updateData.getRole() != null) {
                existingUser.setRole(updateData.getRole());
            }
            if (updateData.getActive() != null) {
                existingUser.setActive(updateData.getActive());
            }
        }
        // Si no es admin, se ignoran los campos de rol y active del updateData
        // o se podrían añadir excepciones si intenta cambiarlos sin permiso.
        // Aquí no se lanza excepcion si un no-admin intenta cambiar rol/active.
        // La lógica de 'no tienes permiso para actualizar roles' se ha movido al principio de updateUser.

        return userPersistencePort.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserById(Long id, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public User findUserByUuid(UUID uuid, RequesterContext requester) {
        User userToReturn = userPersistencePort.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el UUID: " + uuid));

        if (!requester.isAdmin() && !requester.isOwner(userToReturn.getId())) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }
        return userToReturn;
    }

    @Override
    public void deleteUserById(Long id, RequesterContext requester) {
        if (!requester.isAdmin() && !requester.isOwner(id)) {
            throw new AccessDeniedException("No tienes permiso para eliminar este usuario.");
        }
        userPersistencePort.deleteById(id);
    }

    @Override
    public void deleteUserByUuid(UUID uuid, RequesterContext requester) {
        User userToDelete = userPersistencePort.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el UUID: " + uuid));

        if (!requester.isAdmin() && !requester.isOwner(userToDelete.getId())) {
            throw new AccessDeniedException("No tiene permiso para eliminar a este usuario.");
        }

        userPersistencePort.deleteByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> getAllUsers(RequesterContext requester) {
        if (!requester.isAdmin()) {
            throw new AccessDeniedException("No tienes permiso para listar los usuarios.");
        }
        return userPersistencePort.findAll();
    }

    // --- Implementación de los nuevos métodos findMyProfile y deleteMyProfile ---

    @Override
    @Transactional(readOnly = true)
    public User getMyProfile(RequesterContext requester) {
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado para ver su perfil."));
        return userPersistencePort.findById(myUserId)
                .orElseThrow(() -> new UserNotFoundException("El perfil del usuario autenticado no fue encontrado."));
    }

    @Override
    public void deleteMyProfile(RequesterContext requester) {
        Long myUserId = requester.userId()
                .orElseThrow(() -> new AccessDeniedException("Usuario no autenticado para eliminar su perfil."));

        deleteUserById(myUserId, requester); // Reutiliza la lógica de deleteUser
    }
}