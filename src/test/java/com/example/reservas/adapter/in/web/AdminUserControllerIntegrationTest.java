package com.example.reservas.adapter.in.web;
/*
import domain.model.Role;
import domain.model.User;
import domain.port.out.UserPersistencePort; // Necesitarás acceso al repositorio para preparar los datos
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder; // Necesitarás el PasswordEncoder real
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional; // Para revertir cambios en la DB

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK) // Carga el contexto completo
@AutoConfigureMockMvc // Configura MockMvc para tests de integración
@ActiveProfiles("test") // Usa el perfil de test (importante para la DB en memoria)
@Transactional // Cada test se ejecutará en una transacción que se revertirá al final
class AdminUserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserPersistencePort userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        // Limpia la base de datos antes de cada test para asegurar aislamiento
        if(user1 != null && user1.getId() != null)
            userRepository.deleteById(user1.getId());
        if(user2 != null && user2.getId() != null)
            userRepository.deleteById(user2.getId());

        // Crea y guarda usuarios de prueba
        user1 = new User(null, "testuser1", "test1@example.com", passwordEncoder.encode("password"), true, Role.USER);
        user2 = new User(null, "testuser2", "test2@example.com", passwordEncoder.encode("password"), true, Role.ADMIN);

        user1 = userRepository.save(user1);
        user2 = userRepository.save(user2);
    }

    // --- Tests para GET /api/admin/users ---
    @Test
    @DisplayName("GET /api/admin/users - Should return all users and HTTP 200 OK (as Admin)")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"}) // Simula un ADMIN
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser1")))
                .andExpect(jsonPath("$[1].username", is("testuser2")));
    }

    @Test
    @DisplayName("GET /api/admin/users - Should return 403 Forbidden if not Admin")
    @WithMockUser(username = "normalUser", roles = {"USER"})
    void getAllUsers_NotAdmin_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }

    // --- Tests para POST /api/admin/users ---
    @Test
    @DisplayName("POST /api/admin/users - Should create user and return HTTP 201 Created")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void createUser_ShouldReturnCreatedUser() throws Exception {
        AdminUserCreationDTO creationDTO = new AdminUserCreationDTO();
        creationDTO.setUsername("newuser");
        creationDTO.setEmail("new@example.com");
        creationDTO.setPassword("password");
        creationDTO.setRole("USER");
        creationDTO.setActive(true);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username", is("newuser")))
                .andExpect(jsonPath("$.id").isNumber());

        // Verifica que el usuario fue realmente creado en la DB
        assertTrue(userRepository.findByUsername("newuser").isPresent());
    }

    @Test
    @DisplayName("POST /api/admin/users - Should return 409 Conflict if username already exists")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void createUser_DuplicateUsername_ShouldReturnConflict() throws Exception {
        AdminUserCreationDTO creationDTO = new AdminUserCreationDTO();
        creationDTO.setUsername("testuser1"); // Username que ya existe (creado en setUp)
        creationDTO.setEmail("newemail@example.com");
        creationDTO.setPassword("password");
        creationDTO.setRole("USER");
        creationDTO.setActive(true);

        mockMvc.perform(post("/api/admin/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isConflict());
    }

    // --- Tests para DELETE /api/admin/users/{id} ---
    @Test
    @DisplayName("DELETE /api/admin/users/{id} - Should delete user and return HTTP 204 No Content")
    @WithMockUser(username = "adminUser", roles = {"ADMIN"})
    void deleteUser_ShouldReturnNoContent() throws Exception {
        long userIdToDelete = user1.getId();

        mockMvc.perform(delete("/api/admin/users/" + userIdToDelete))
                .andExpect(status().isNoContent());

        // Verifica que el usuario fue realmente eliminado de la DB
        assertFalse(userRepository.findById(userIdToDelete).isPresent());
    }
}
*/