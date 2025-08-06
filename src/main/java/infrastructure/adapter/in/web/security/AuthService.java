package infrastructure.adapter.in.web.security;

import infrastructure.adapter.in.web.dto.AuthResponseDTO;
import infrastructure.adapter.in.web.dto.LoginRequestDTO;
import infrastructure.adapter.in.web.dto.RegisterRequestDTO;
import domain.model.Role;
import domain.model.User;
import domain.port.out.UserPersistencePort;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserPersistencePort userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public User registerUser(RegisterRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está registrado");
        }

        Set<Role> domainRoles = request.getRoles().stream()
                .map(roleString -> {
                    if (roleString.equalsIgnoreCase("ADMIN")) {
                        throw new IllegalArgumentException("Cannot self-assign ADMIN role during registration.");
                    }
                    try {
                        return Role.valueOf(roleString.toUpperCase());
                    } catch (IllegalArgumentException e) {
                        throw new IllegalArgumentException("Invalid role provided: " + roleString + ". Valid roles are: " + getValidPublicRoles());
                    }
                })
                .collect(Collectors.toSet());

        if (domainRoles.isEmpty()) {
            throw new IllegalArgumentException("At least one valid role must be selected.");
        }

        User newUser = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .active(Boolean.TRUE) // Podría requerir confirmación por email en un futuro
                .roles(domainRoles)
                .build();

        return userRepository.save(newUser);
    }

    private String getValidPublicRoles() {
        return Arrays.stream(Role.values())
                .filter(role -> role != Role.ADMIN)
                .map(Role::name)
                .collect(Collectors.joining(", "));
    }

    public AuthResponseDTO loginUser(LoginRequestDTO request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        String jwt = jwtTokenProvider.generateToken(authentication);
        return new AuthResponseDTO(jwt);
    }

    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }

}
