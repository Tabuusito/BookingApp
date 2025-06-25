package infrastructure.adapter.in.web.controller;

import domain.exception.UserNotFoundException;
import domain.port.in.UserService;
import domain.model.User;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    private final UserDTOMapper userDTOMapper;

    @GetMapping()
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {
        try {
            return new ResponseEntity<>(userService.getAllUsers().stream().map(userDTOMapper::toDTO).toList(), HttpStatus.OK);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        try {
            User user = userService.findUserById(id);
            UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
            if (user != null){
                return new ResponseEntity<>(responseDTO, HttpStatus.OK);
            }
            else return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch(UserNotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AdminUserCreationDTO adminUserCreationDTO) {
        try {

            User newUser = userDTOMapper.toDomain(adminUserCreationDTO);
            User createdUser = userService.createUser(newUser);
            UserResponseDTO responseDTO = userDTOMapper.toDTO(createdUser);
            return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
        } catch(AccessDeniedException e) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,@Valid @RequestBody UserUpdateDTO updatedUserDTO) {
        try {
            updatedUserDTO.setId(id);
            User userToUpdate = userDTOMapper.toDomain(updatedUserDTO);
            User user = userService.updateUser(userToUpdate);
            UserResponseDTO responseDTO = userDTOMapper.toDTO(user);

            return new ResponseEntity<>(responseDTO, HttpStatus.OK);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (UserNotFoundException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch(AccessDeniedException e) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
