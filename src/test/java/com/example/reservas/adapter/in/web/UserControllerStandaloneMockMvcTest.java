package com.example.reservas.adapter.in.web;


import com.fasterxml.jackson.databind.ObjectMapper;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.UserController;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.RegisterRequestDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Importante

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class UserControllerStandaloneMockMvcTest {

    private MockMvc mockMvc;

    @Mock
    private UserDTOMapper userDTOMapperMock;

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private UserController userController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UserResponseDTO userResponse1;
    private UserResponseDTO userResponse2;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();

        // Modelos de dominio
        user1  = new User(1L, "testuser1", "test1@example.com", "hash1", true, Role.USER);
        user2 = new User(2L, "testuser2", "test2@example.com", "hash2", true, Role.ADMIN);

        // DTOs de respuesta que simulará el mapper
        userResponse1 = new UserResponseDTO(1L, "testuser1", "test1@example.com", "USER", true);
        userResponse2 = new UserResponseDTO(2L, "testuser2", "test2@example.com", "ADMIN", true);

        // ... configuración de otros DTOs de request
    }

    // --- Tests para GET /api/user ---

    @Test
    @DisplayName("GET /api/user - Should return all users and HTTP 200 OK")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        // Arrange
        List<User> domainUsers = Arrays.asList(user1, user2);
        when(userServiceMock.getAllUsers()).thenReturn(domainUsers);

        // Mocks específicos
        when(userDTOMapperMock.toDTO(eq(user1))).thenReturn(userResponse1);
        when(userDTOMapperMock.toDTO(eq(user2))).thenReturn(userResponse2);

        System.out.println("Test - Mocks para userDTOMapper definidos.");

        // Act & Assert
        mockMvc.perform(get("/api/user").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser1")))
                .andExpect(jsonPath("$[1].username", is("testuser2")));

        // Verify
        verify(userServiceMock).getAllUsers();
        verify(userDTOMapperMock).toDTO(eq(user1));
        verify(userDTOMapperMock).toDTO(eq(user2));
    }

    // --- Tests para GET /api/user/{id} ---

    @Test
    @DisplayName("GET /api/user/{id} - Should return user and HTTP 200 OK when user found")
    void getUserById_WhenUserFound_ShouldReturnUser() throws Exception {
        // Arrange
        when(userServiceMock.findUserById(1L)).thenReturn(user1);
        when(userDTOMapperMock.toDTO(user1)).thenReturn(userResponse1);

        // Act & Assert
        mockMvc.perform(get("/api/user/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser1")));

        // Verify
        verify(userServiceMock).findUserById(1L);
        verify(userDTOMapperMock).toDTO(user1);
    }

    // El test para "user not found" es ahora más específico
    @Test
    @DisplayName("GET /api/user/{id} - Should return HTTP 404 Not Found when service throws UserNotFoundException")
    void getUserById_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        // Arrange: Simulamos que el servicio lanza una excepción porque no encuentra el usuario
        when(userServiceMock.findUserById(99L)).thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        mockMvc.perform(get("/api/user/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        // Verify
        verify(userServiceMock).findUserById(99L);
    }

    // --- Tests para POST /api/user ---

    @Test
    @DisplayName("POST /api/user - Should create user and return HTTP 201 Created")
    void createUser_ShouldReturnCreatedUser() throws Exception {
        // Arrange
        AdminUserCreationDTO creationDTO = new AdminUserCreationDTO("newuser", "password123", "new@example.com", "USER", true);
        User userToCreate = new User(null, "newuser", "password123", "new@example.com", true, Role.USER);
        User createdUser = new User(3L, "newuser", "hashed_password", "new@example.com", true, Role.USER);
        UserResponseDTO responseDTO = new UserResponseDTO(3L, "newuser", "new@example.com", "USER", true);

        when(userDTOMapperMock.toDomain(any(AdminUserCreationDTO.class))).thenReturn(userToCreate);
        when(userServiceMock.createUser(userToCreate)).thenReturn(createdUser);
        when(userDTOMapperMock.toDTO(createdUser)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(creationDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("newuser")));

        // Verify
        verify(userDTOMapperMock).toDomain(any(AdminUserCreationDTO.class));
        verify(userServiceMock).createUser(userToCreate);
        verify(userDTOMapperMock).toDTO(createdUser);
    }

    // --- Tests para PUT /api/user/{id} ---

    @Test
    @DisplayName("PUT /api/user/{id} - Should update user and return HTTP 200 OK")
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        // Arrange
        long userId = 1L;
        UserUpdateDTO updateDTO = UserUpdateDTO.builder()
                .id(userId)
                .username("updateduser")
                .email("updated@example.com")
                .role("USER")
                .active(true)
                .build();
        User userToUpdate = new User(userId, "updateduser", "updated@example.com", null, true, Role.USER);
        User updatedUser = new User(userId, "updateduser", "updated@example.com", "hash1", true, Role.USER);
        UserResponseDTO responseDTO = new UserResponseDTO(userId, "updateduser", "updated@example.com", "USER", true);

        when(userDTOMapperMock.toDomain(any(UserUpdateDTO.class))).thenReturn(userToUpdate);
        when(userServiceMock.updateUser(userToUpdate)).thenReturn(updatedUser);
        when(userDTOMapperMock.toDTO(updatedUser)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(put("/api/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("updateduser")));

        // Verify
        verify(userDTOMapperMock).toDomain(any(UserUpdateDTO.class));
        verify(userServiceMock).updateUser(userToUpdate);
        verify(userDTOMapperMock).toDTO(updatedUser);
    }

    // --- Tests para DELETE /api/user/{id} ---

    @Test
    @DisplayName("DELETE /api/user/{id} - Should delete user and return HTTP 204 No Content")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        // Arrange
        long userId = 1L;
        doNothing().when(userServiceMock).deleteUser(userId);

        // Act & Assert
        mockMvc.perform(delete("/api/user/" + userId))
                .andExpect(status().isNoContent());

        // Verify
        verify(userServiceMock).deleteUser(userId);
    }
}