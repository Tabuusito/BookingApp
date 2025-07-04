package infrastructure.adapter.in.web.controller;

import domain.model.User;
import domain.port.in.UserService;
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


@RestController
@RequestMapping("/api/me/profile")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class MyProfileController extends AbstractBaseController {

    private final UserService userService;
    private final UserDTOMapper userDTOMapper;

    @GetMapping
    public ResponseEntity<UserResponseDTO> getMyProfile(Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        User user = userService.getMyProfile(requester); // El servicio obtiene el perfil del requester
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @PutMapping
    public ResponseEntity<UserResponseDTO> updateMyProfile(@Valid @RequestBody UserUpdateDTO updatedUserDTO,
                                                           Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);

        User userToUpdate = userDTOMapper.toDomain(updatedUserDTO);
        User user = userService.updateMyProfile(userToUpdate, requester);
        UserResponseDTO responseDTO = userDTOMapper.toDTO(user);
        return new ResponseEntity<>(responseDTO, HttpStatus.OK);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyProfile(Authentication authentication) {
        RequesterContext requester = createRequesterContext(authentication);
        userService.deleteMyProfile(requester);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
