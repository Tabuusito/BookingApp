package infrastructure.adapter.in.web.controller;

import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/me/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyProfileController extends AbstractBaseController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getMyProfile() {
        User user = userService.getMyProfile();
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserUpdateDTO updatedUserDTO) {

        User userToUpdate = userDTOMapper.toDomain(updatedUserDTO);
        User user = userService.updateMyProfile(userToUpdate);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyProfile() {
        userService.deleteMyProfile();
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
