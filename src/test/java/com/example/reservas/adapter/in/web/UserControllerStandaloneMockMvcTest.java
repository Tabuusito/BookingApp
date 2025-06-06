package com.example.reservas.adapter.in.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.UserController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Importante

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.hasSize;

@ExtendWith(MockitoExtension.class)
class UserControllerStandaloneMockMvcTest {

    private MockMvc mockMvc; // Declarado aquí

    @Mock
    private UserService userServiceMock;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper = new ObjectMapper();

    private User user1;
    private User user2;
    private User newUserRequest;
    private User updateUserRequest;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .build();

        user1 = new User();
        user1.setId(1L);
        user1.setUsername("testuser1");
        user1.setEmail("test1@example.com");
        user1.setRole(Role.USER);

        user2 = new User();
        user2.setId(2L);
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setRole(Role.ADMIN);

        newUserRequest = new User();
        newUserRequest.setUsername("newuser");
        newUserRequest.setEmail("new@example.com");
        newUserRequest.setRole(Role.USER);
        newUserRequest.setPasswordHash("password123");

        updateUserRequest = new User();
        updateUserRequest.setUsername("updateduser");
        updateUserRequest.setEmail("updated@example.com");
    }
    @Test
    @DisplayName("GET /api/user - Should return all users and HTTP 200 OK")
    void getAllUsers_ShouldReturnAllUsers() throws Exception {
        List<User> users = Arrays.asList(user1, user2);
        when(userServiceMock.getAllUsers()).thenReturn(users);
        mockMvc.perform(get("/api/user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("testuser1")));
        verify(userServiceMock).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/user - Should return HTTP 500 if service throws general exception")
    void getAllUsers_ServiceThrowsGeneralException_ShouldReturnInternalServerError() throws Exception {
        when(userServiceMock.getAllUsers()).thenThrow(new NullPointerException("Simulated service error"));
        mockMvc.perform(get("/api/user").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(userServiceMock).getAllUsers();
    }

    // --- Tests para GET /api/user/{id} (getUserById) ---
    @Test
    @DisplayName("GET /api/user/{id} - Should return user and HTTP 200 OK when user found")
    void getUserById_WhenUserFound_ShouldReturnUser() throws Exception {
        when(userServiceMock.findUserById(1L)).thenReturn(user1);
        mockMvc.perform(get("/api/user/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser1")));
        verify(userServiceMock).findUserById(1L);
    }

    @Test
    @DisplayName("GET /api/user/{id} - Should return HTTP 404 Not Found when user not found")
    void getUserById_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        when(userServiceMock.findUserById(99L)).thenReturn(null);
        mockMvc.perform(get("/api/user/99").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userServiceMock).findUserById(99L);
    }

    @Test
    @DisplayName("GET /api/user/{id} - Should return HTTP 500 if service throws general exception")
    void getUserById_ServiceThrowsGeneralException_ShouldReturnInternalServerError() throws Exception {
        when(userServiceMock.findUserById(1L)).thenThrow(new NullPointerException("Simulated service error"));
        mockMvc.perform(get("/api/user/1").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isInternalServerError());
        verify(userServiceMock).findUserById(1L);
    }


    // --- Tests para POST /api/user (createUser) ---
    @Test
    @DisplayName("POST /api/user - Should create user and return HTTP 201 Created")
    void createUser_ShouldReturnCreatedUser() throws Exception { // Línea 127 original del error
        User createdUser = new User();
        createdUser.setId(3L);
        createdUser.setUsername(newUserRequest.getUsername());
        // ... resto de los campos ...

        when(userServiceMock.createUser(any(User.class))).thenReturn(createdUser);

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(3)))
                .andExpect(jsonPath("$.username", is("newuser")));
        verify(userServiceMock).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/user - Should return HTTP 409 Conflict if service throws RuntimeException (e.g., duplicate user)")
    void createUser_WhenDuplicateUser_ShouldReturnConflict() throws Exception {
        when(userServiceMock.createUser(any(User.class))).thenThrow(new RuntimeException("User already exists"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isConflict());
        verify(userServiceMock).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/user - Should return HTTP 409 Conflict if service throws unexpected RuntimeException")
    void createUser_ServiceThrowsGeneralException_ShouldReturnInternalServerError() throws Exception {
        when(userServiceMock.createUser(any(User.class))).thenThrow(new NullPointerException("Simulated service error"));

        mockMvc.perform(post("/api/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newUserRequest)))
                .andExpect(status().isConflict());
        verify(userServiceMock).createUser(any(User.class));
    }


    // --- Tests para PUT /api/user/{id} (updateUser) ---
    @Test
    @DisplayName("PUT /api/user/{id} - Should update user and return HTTP 200 OK")
    void updateUser_ShouldReturnUpdatedUser() throws Exception { // Línea 161 original del error
        long userId = 1L;
        User userAfterUpdate = new User();
        userAfterUpdate.setId(userId);
        userAfterUpdate.setUsername(updateUserRequest.getUsername());

        when(userServiceMock.updateUser(any(User.class))).thenAnswer(invocation -> {
            User userArg = invocation.getArgument(0);
            if (userArg.getId().equals(userId)) {
                return userAfterUpdate;
            }
            return null;
        });

        mockMvc.perform(put("/api/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is((int)userId)))
                .andExpect(jsonPath("$.username", is("updateduser")));

        verify(userServiceMock).updateUser(argThat(user ->
                user.getId().equals(userId) &&
                        user.getUsername().equals(updateUserRequest.getUsername())
        ));
    }

    @Test
    @DisplayName("PUT /api/user/{id} - Should return HTTP 404 Not Found if service throws RuntimeException (e.g., user not found)")
    void updateUser_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        long nonExistentUserId = 99L;
        when(userServiceMock.updateUser(argThat(user -> user.getId().equals(nonExistentUserId))))
                .thenThrow(new RuntimeException("User not found with ID: " + nonExistentUserId));

        mockMvc.perform(put("/api/user/" + nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateUserRequest)))
                .andExpect(status().isNotFound());
        verify(userServiceMock).updateUser(any(User.class));
    }


    // --- Tests para DELETE /api/user/{id} (deleteUser) ---
    @Test
    @DisplayName("DELETE /api/user/{id} - Should delete user and return HTTP 204 No Content")
    void deleteUser_ShouldReturnNoContent() throws Exception {
        long userId = 1L;
        doNothing().when(userServiceMock).deleteUser(userId);

        mockMvc.perform(delete("/api/user/" + userId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
        verify(userServiceMock).deleteUser(userId);
    }

    @Test
    @DisplayName("DELETE /api/user/{id} - Should return HTTP 404 Not Found if service throws RuntimeException (e.g., user not found)")
    void deleteUser_WhenUserNotFound_ShouldReturnNotFound() throws Exception {
        long nonExistentUserId = 99L;
        doThrow(new RuntimeException("User not found with ID: " + nonExistentUserId))
                .when(userServiceMock).deleteUser(nonExistentUserId);

        mockMvc.perform(delete("/api/user/" + nonExistentUserId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
        verify(userServiceMock).deleteUser(nonExistentUserId);
    }

}