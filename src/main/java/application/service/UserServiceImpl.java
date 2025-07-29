package application.service;

import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;


    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + username));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(User newUser) {
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

        if (newUser.getRoles() == null || newUser.getRoles().equals(Collections.emptySet())) {
            newUser.setRoles(new HashSet<>(Collections.singleton(Role.CLIENT)));
        }

        return userPersistencePort.save(newUser);
    }

    @Override
    @Transactional
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isSelf(authentication, #userToUpdate.getUuid())")
    public User updateUser(User userToUpdate) {
        UUID userUuidToUpdate = userToUpdate.getUuid();
        if (userUuidToUpdate == null) {
            throw new IllegalArgumentException("Debe proveerse una UUID para actualizar.");
        }

        User existingUser = userPersistencePort.findByUuid(userUuidToUpdate)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con uuid: " + userUuidToUpdate));

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        if (!isAdmin) {
            if (userToUpdate.getRoles() != null && !userToUpdate.getRoles().isEmpty()
                    && !userToUpdate.getRoles().equals(existingUser.getRoles())) {
                throw new AccessDeniedException("No tienes permiso para actualizar roles.");
            }
            if (userToUpdate.getActive() != null && !userToUpdate.getActive().equals(existingUser.getActive())) {
                throw new AccessDeniedException("No tienes permiso para cambiar el estado activo.");
            }
        }

        return applyUserUpdates(existingUser, userToUpdate, isAdmin);
    }

    @Override
    @Transactional
    @PreAuthorize("@customSecurity.isSelf(authentication, #userToUpdate.getUuid())")
    public User updateMyProfile(User userToUpdate) {
        User existingUser = userPersistencePort.findByUuid(userToUpdate.getUuid())
                .orElseThrow(() -> new UserNotFoundException("El perfil del usuario autenticado no fue encontrado.")); // Inconsistencia de datos

        boolean isAdmin = SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"));

        // Aplicar los cambios. Las comprobaciones de rol se harán en applyUserUpdates.
        // Un usuario normal (no admin) no puede cambiar su propio rol o estado 'active'.
        return applyUserUpdates(existingUser, userToUpdate, isAdmin);
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
            if (updateData.getRoles() != null && !updateData.getRoles().equals(Collections.emptySet())) {
                existingUser.setRoles(updateData.getRoles());
            }
            if (updateData.getActive() != null) {
                existingUser.setActive(updateData.getActive());
            }
        }

        return userPersistencePort.save(existingUser);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public User findUserById(Long id) {
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isSelf(authentication, #uuid)")
    public User findUserByUuid(UUID uuid) {
        return userPersistencePort.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el UUID: " + uuid));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public void deleteUserById(Long id) {
        userPersistencePort.deleteById(id);
    }

    @Override
    @PreAuthorize("hasRole('ADMIN') or @customSecurity.isSelf(authentication, #uuid)")
    public void deleteUserByUuid(UUID uuid) {
        User userToDelete = userPersistencePort.findByUuid(uuid)
                .orElseThrow(() -> new UserNotFoundException("No se encontró un usuario con el UUID: " + uuid));

        userPersistencePort.deleteByUuid(uuid);
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userPersistencePort.findAll();
    }


    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("isAuthenticated()")
    public User getMyProfile() {
        UUID myUserUuid = ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();

        return userPersistencePort.findByUuid(myUserUuid)
                .orElseThrow(() -> new UserNotFoundException("El perfil del usuario autenticado no fue encontrado."));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteMyProfile() {
        UUID myUserUuid = ((SpringSecurityUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUuid();

        deleteUserByUuid(myUserUuid);
    }
}