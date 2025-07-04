package infrastructure.adapter.in.web.controller;

import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController extends AbstractBaseController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);

        List<User> users = userService.getAllUsers(requester);
        List<UserResponseDTO> responseDTOs = users.stream()
                .map(userDTOMapper::toDTO).toList();
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);

        User user = userService.findUserById(id, requester);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AdminUserCreationDTO adminUserCreationDTO,
                                                      Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);

        User newUser = userDTOMapper.toDomain(adminUserCreationDTO);
        User createdUser = userService.createUser(newUser, requester);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(createdUser);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable Long id,
                                                      @Valid @RequestBody UserUpdateDTO updatedUserDTO,
                                                      Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        updatedUserDTO.setId(id);
        User userToUpdate = userDTOMapper.toDomain(updatedUserDTO);
        User user = userService.updateUser(userToUpdate, requester);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id, Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        userService.deleteUser(id, requester);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
