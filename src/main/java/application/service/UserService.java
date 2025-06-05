package application.service;

import domain.model.User;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface UserService {

    public User findByUsername(String username);

    public User saveUser(User user);

    public User findUserById(Long id);

    public List<User> getAllUsers();



}
