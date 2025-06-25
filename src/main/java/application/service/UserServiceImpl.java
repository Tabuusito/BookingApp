package application.service;

import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import domain.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserPersistencePort userPersistencePort;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + username));
    }


    public User createUser(User newUser) {

        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userPersistencePort.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado."));
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        if (!isAdmin) {
            throw new AccessDeniedException("No tienes permiso para crear usuarios.");
        }
        if (userPersistencePort.existsByUsername(newUser.getUsername())) {
            throw new RuntimeException("Nombre de usuario ya existe");
        }
        if (userPersistencePort.existsByEmail(newUser.getEmail())) {
            throw new RuntimeException("Email ya existe");
        }
        User userToPersist = new User();

        if (newUser.getUsername() != null && !newUser.getUsername().isBlank()) {
            userToPersist.setUsername(newUser.getUsername());
        }
        if (newUser.getEmail() != null && !newUser.getEmail().isBlank()) {
            userToPersist.setEmail(newUser.getEmail());
        }
        if (newUser.getPasswordHash() != null && !newUser.getPasswordHash().isBlank()) {
            userToPersist.setPasswordHash(passwordEncoder.encode(newUser.getPasswordHash()));
        }
        if (newUser.getRole() != null) {
            userToPersist.setRole(newUser.getRole());
        }
        if (newUser.getActive() != null) {
            userToPersist.setActive(newUser.getActive());
        }
        return userPersistencePort.save(userToPersist);
    }

    @Transactional
    public User updateUser(User userToUpdate) {
        Long userIdToUpdate = userToUpdate.getId();
        if (userIdToUpdate == null) {
            throw new IllegalArgumentException("Debe proveerse una ID para actualizar.");
        }

        User existingUser = userPersistencePort.findById(userIdToUpdate)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con id: " + userIdToUpdate));


        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userPersistencePort.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado."));
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        if (!isAdmin && !Objects.equals(authenticatedUser.getId(), userIdToUpdate)) {
            throw new AccessDeniedException("No tienes permiso para actualizar este usuario.");
        }
        if(!isAdmin && (userToUpdate.getRole() != Role.USER)){
            throw new AccessDeniedException("No tienes permiso para actualizar roles.");
        }


        if (userToUpdate.getUsername() != null && !userToUpdate.getUsername().isBlank() && !userToUpdate.getUsername().equals(existingUser.getUsername())) {
            if (userPersistencePort.existsByUsername(userToUpdate.getUsername())) {
                throw new DuplicateUserInfoException("El username '" + userToUpdate.getUsername() + "' ya está en uso.");
            }
            existingUser.setUsername(userToUpdate.getUsername());
        }

        if (userToUpdate.getEmail() != null && !userToUpdate.getEmail().isBlank() && !userToUpdate.getEmail().equals(existingUser.getEmail())) {
            if (userPersistencePort.existsByEmail(userToUpdate.getEmail())) {
                throw new DuplicateUserInfoException("El email '" + userToUpdate.getEmail() + "' ya está en uso.");
            }
            existingUser.setEmail(userToUpdate.getEmail());
        }

        if (userToUpdate.getPasswordHash() != null && !userToUpdate.getPasswordHash().isBlank()) {
            existingUser.setPasswordHash(passwordEncoder.encode(userToUpdate.getPasswordHash()));
        }


        if (isAdmin && userToUpdate.getRole() != null) {
           existingUser.setRole(userToUpdate.getRole());
        }

        if (isAdmin && userToUpdate.getActive() != null) {
            existingUser.setActive(userToUpdate.getActive());
        }

        return userPersistencePort.save(existingUser);
    }

    @Transactional(readOnly = true)
    public User findUserById(Long id){

        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userPersistencePort.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado."));
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        if (!isAdmin && !Objects.equals(authenticatedUser.getId(), id)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }

        return userPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public void deleteUser(Long id){
        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userPersistencePort.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado."));
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        if (!isAdmin && !Objects.equals(authenticatedUser.getId(), id)) {
            throw new AccessDeniedException("No tienes permiso para acceder a este usuario.");
        }
        userPersistencePort.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<User> getAllUsers(){

        String authenticatedUsername = SecurityContextHolder.getContext().getAuthentication().getName();
        User authenticatedUser = userPersistencePort.findByUsername(authenticatedUsername)
                .orElseThrow(() -> new UserNotFoundException("Usuario autenticado no encontrado."));
        boolean isAdmin = authenticatedUser.getRole() == Role.ADMIN;
        if (!isAdmin) {
            throw new AccessDeniedException("No tienes permiso para listar los usuarios.");
        }

        return userPersistencePort.findAll();
    }
}