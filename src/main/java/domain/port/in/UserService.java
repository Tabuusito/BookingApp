package domain.port.in;

import domain.model.User;
import infrastructure.adapter.in.web.dto.RegisterRequestDTO;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public User findByUsername(String username);

    public User createUser(User user);

    public User updateUser(User user);

    public User findUserById(Long id);

    public void deleteUser(Long id);

    public List<User> getAllUsers();

}
