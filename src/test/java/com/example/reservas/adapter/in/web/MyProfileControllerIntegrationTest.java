package com.example.reservas.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.model.Role;
import domain.model.User;
import domain.port.out.UserPersistencePort;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.security.SpringSecurityUser; // Importa tu UserDetails custom
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors; // Import estático para 'user'
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MyProfileControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserPersistencePort userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User authenticatedUser;
    private SpringSecurityUser authenticatedPrincipal;

    @BeforeEach
    void setUp() {

        if(authenticatedUser != null && authenticatedUser.getId() != null)
            userRepository.deleteById(authenticatedUser.getId());

        // 1. Crea la entidad con ID nulo para que la DB lo genere.
        User newUser = new User(null, "mytestuser", "mytestuser@example.com", passwordEncoder.encode("password"), true, Role.USER);

        // 2. Guarda la entidad y obtén la versión gestionada con el ID ya asignado.
        this.authenticatedUser = userRepository.save(newUser);

        // 3. Crea el objeto Principal que usaremos para simular la autenticación.
        this.authenticatedPrincipal = new SpringSecurityUser(this.authenticatedUser);
    }

    @Test
    @DisplayName("GET /api/me/profile - Should return 401 Unauthorized if no user is authenticated")
    void getMyProfile_NotAuthenticated_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/me/profile"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/me/profile - Should return authenticated user's profile with HTTP 200 OK")
    void getMyProfile_Authenticated_ShouldReturnProfile() throws Exception {
        mockMvc.perform(get("/api/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(authenticatedPrincipal))) // <-- Simula el usuario
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(authenticatedUser.getId().intValue())))
                .andExpect(jsonPath("$.username", is(authenticatedUser.getUsername())));
    }

    @Test
    @DisplayName("PUT /api/me/profile - Should update the authenticated user's profile")
    void updateMyProfile_ShouldSucceed() throws Exception {
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("new-username")
                .role("ADMIN")
                .build();

        mockMvc.perform(put("/api/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(authenticatedPrincipal))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("new-username")))
                .andExpect(jsonPath("$.role", is("USER")));

        assertTrue(userRepository.findByUsername("new-username").isPresent());
        assertFalse(userRepository.findByUsername(authenticatedUser.getUsername()).isPresent());
    }

    @Test
    @DisplayName("PUT /api/me/profile - Should fail with 409 Conflict if username is taken")
    void updateMyProfile_WhenUsernameIsTaken_ShouldReturnConflict() throws Exception {
        // Crear otro usuario con el username que queremos usar
        userRepository.save(new User(null, "taken_username", "taken@example.com", passwordEncoder.encode("pass"), true, Role.USER));

        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .username("taken_username")
                .role("USER")
                .build();

        mockMvc.perform(put("/api/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(authenticatedPrincipal)) // <-- Simula el usuario
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE /api/me/profile - Should delete the authenticated user's profile")
    void deleteMyProfile_ShouldSucceed() throws Exception {
        assertTrue(userRepository.findById(authenticatedUser.getId()).isPresent());

        mockMvc.perform(delete("/api/me/profile")
                        .with(SecurityMockMvcRequestPostProcessors.user(authenticatedPrincipal))) // <-- Simula el usuario
                .andExpect(status().isNoContent());

        assertFalse(userRepository.findById(authenticatedUser.getId()).isPresent());
    }
}