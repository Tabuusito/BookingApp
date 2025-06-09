package infrastructure.adapter.in.web.controller;

import infrastructure.adapter.in.web.dto.AuthResponseDTO;
import infrastructure.adapter.in.web.dto.LoginRequestDTO;
import infrastructure.adapter.in.web.dto.RegisterRequestDTO;
import infrastructure.adapter.in.web.security.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody RegisterRequestDTO request) {
        try {
            authService.registerUser(request);
            return new ResponseEntity<>("Usuario registrado exitosamente!", HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return new ResponseEntity<>("Error al registrar usuario: " + e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO authResponse = authService.loginUser(request);
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
}
