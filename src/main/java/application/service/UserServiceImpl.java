package application.service;

import domain.model.User;
import domain.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserPersistencePort userPersistencePort;

    public User findByUsername(String username) {
        return userPersistencePort.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado con nombre: " + username));
    }

    public User saveUser(User user) {
        if (userPersistencePort.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Nombre de usuario ya existe");
        }
        if (userPersistencePort.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email ya existe");
        }
        return userPersistencePort.save(user);
    }

    public User findUserById(Long id){
        return userPersistencePort.findById(id)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con id: " + id));
    }

    public List<User> getAllUsers(){
        return userPersistencePort.findAll();
    }
}