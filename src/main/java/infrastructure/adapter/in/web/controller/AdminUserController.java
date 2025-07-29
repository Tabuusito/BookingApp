package infrastructure.adapter.in.web.controller;

import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.util.UuidValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController extends AbstractBaseController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;
    private final UuidValidator uuidValidator;

    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers() {

        List<User> users = userService.getAllUsers();
        List<UserResponseDTO> responseDTOs = users.stream()
                .map(userDTOMapper::toDTO).toList();
        return new ResponseEntity<>(responseDTOs, HttpStatus.OK);
    }

    @GetMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable ("uuid") String userUuid) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(userUuid);

        User user = userService.findUserByUuid(uuid);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody AdminUserCreationDTO adminUserCreationDTO) {

        User newUser = userDTOMapper.toDomain(adminUserCreationDTO);
        User createdUser = userService.createUser(newUser);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(createdUser);
        return new ResponseEntity<>(responseDTO, HttpStatus.CREATED);
    }

    @PutMapping("/{uuid}")
    public ResponseEntity<UserResponseDTO> updateUser(@PathVariable ("uuid") String userUuid,
                                                      @Valid @RequestBody UserUpdateDTO updatedUserDTO) {

        User userToUpdate = userDTOMapper.toDomain(updatedUserDTO);
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(userUuid);
        userToUpdate.setUuid(uuid);
        User user = userService.updateUser(userToUpdate);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> deleteUser(@PathVariable ("uuid") String userUuid) {
        UUID uuid = uuidValidator.UUIDvalidateAndConvert(userUuid);
        userService.deleteUserByUuid(uuid);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
