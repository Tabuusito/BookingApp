package com.example.reservas.adapter.in.web;

import domain.exception.DuplicateUserInfoException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.UserController;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userServiceMock;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserController userController;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {

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
    }

    @Test
    @DisplayName("GET /api/user - Should return all users and HTTP 200 OK")
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(user1, user2);
        when(userServiceMock.getAllUsers()).thenReturn(users);

        ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("testuser1", response.getBody().getFirst().getUsername());
        verify(userServiceMock, times(1)).getAllUsers(); // Verifica que el método del mock fue llamado
    }

    @Test
    @DisplayName("GET /api/user - Should return HTTP 500 if service throws exception")
    void getAllUsers_ServiceThrowsException_ShouldReturnInternalServerError() {

        when(userServiceMock.getAllUsers()).thenThrow(new RuntimeException("Service error"));

        ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody()); // No debería haber cuerpo en un error 500 genérico aquí
        verify(userServiceMock, times(1)).getAllUsers();
    }


    @Test
    @DisplayName("GET /api/user/{id} - Should return user and HTTP 200 OK when user found")
    void getUserById_WhenUserFound_ShouldReturnUser() {

        when(userServiceMock.findUserById(1L)).thenReturn(user1);

        ResponseEntity<UserResponseDTO> response = userController.getUserById(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("testuser1", response.getBody().getUsername());
        verify(userServiceMock, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("GET /api/user/{id} - Should return HTTP 404 Not Found when user not found")
    void getUserById_WhenUserNotFound_ShouldReturnNotFound() {

        when(userServiceMock.findUserById(99L)).thenReturn(null);

        ResponseEntity<UserResponseDTO> response = userController.getUserById(99L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(userServiceMock, times(1)).findUserById(99L);
    }

    @Test
    @DisplayName("GET /api/user/{id} - Should return HTTP 500 if service throws exception")
    void getUserById_ServiceThrowsException_ShouldReturnInternalServerError() {
        // Arrange
        when(userServiceMock.findUserById(1L)).thenThrow(new RuntimeException("Service error"));

        // Act
        ResponseEntity<UserResponseDTO> response = userController.getUserById(1L);

        // Assert
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        verify(userServiceMock, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("POST /api/user - Should create user and return HTTP 201 Created")
    void createUser_ShouldReturnCreatedUser() {
        AdminUserCreationDTO newUserRequest = new AdminUserCreationDTO(); // Simula el request body
        newUserRequest.setUsername("newuser");
        newUserRequest.setEmail("new@example.com");
        newUserRequest.setPassword("password");
        newUserRequest.setRole("ADMIN");
        newUserRequest.setActive(true);

        User createdUser = new User(); // Simula el usuario devuelto por el servicio
        createdUser.setId(3L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@example.com");
        createdUser.setPasswordHash(passwordEncoder.encode("password"));
        createdUser.setRole(Role.USER);

        when(userServiceMock.createUser(any(User.class))).thenReturn(createdUser);

        ResponseEntity<UserResponseDTO> response = userController.createUser(newUserRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getId());
        assertEquals("newuser", response.getBody().getUsername());
        verify(userServiceMock, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/user - Should return HTTP 409 Conflict if service throws RuntimeException (e.g., duplicate)")
    void createUser_ServiceThrowsRuntimeException_ShouldReturnConflict() {
        AdminUserCreationDTO newUserRequest = new AdminUserCreationDTO();
        newUserRequest.setUsername("existinguser");

        when(userServiceMock.createUser(any(User.class))).thenThrow(new DuplicateUserInfoException("User already exists"));

        ResponseEntity<UserResponseDTO> response = userController.createUser(newUserRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        verify(userServiceMock, times(1)).createUser(any(User.class));
    }


    @Test
    @DisplayName("PUT /api/user/{id} - Should update user and return HTTP 200 OK")
    void updateUser_ShouldReturnUpdatedUser() {
        UserUpdateDTO updatedUserRequest = new UserUpdateDTO(); // Simula el request body
        updatedUserRequest.setUsername("updateduser");
        // No se necesita setear el ID aquí porque el controlador lo hace con updatedUser.setId(id);

        User userAfterUpdate = new User();
        userAfterUpdate.setId(1L);
        userAfterUpdate.setUsername("updateduser");
        userAfterUpdate.setRole(Role.USER);

        when(userServiceMock.updateUser(any(User.class))).thenReturn(userAfterUpdate);

        ResponseEntity<UserResponseDTO> response = userController.updateUser(1L, updatedUserRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("updateduser", response.getBody().getUsername());
        // Verifica que el ID fue seteado en el objeto pasado al servicio
        verify(userServiceMock, times(1)).updateUser(argThat(user -> user.getId().equals(1L) && user.getUsername().equals("updateduser")));
    }

    @Test
    @DisplayName("PUT /api/user/{id} - Should return HTTP 404 Not Found if service throws RuntimeException (e.g., UserNotFound)")
    void updateUser_UserNotFound_ShouldReturnNotFound() {
        UserUpdateDTO updatedUserRequest = new UserUpdateDTO();
        updatedUserRequest.setUsername("updateduser");

        // Simula que el servicio lanza una RuntimeException (podría ser una UserNotFoundException específica)
        when(userServiceMock.updateUser(any(User.class))).thenThrow(new RuntimeException("User not found"));

        ResponseEntity<UserResponseDTO> response = userController.updateUser(99L, updatedUserRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userServiceMock, times(1)).updateUser(any(User.class));
    }

    @Test
    @DisplayName("DELETE /api/user/{id} - Should delete user and return HTTP 204 No Content")
    void deleteUser_ShouldReturnNoContent() {
        // El método deleteUser del servicio es void, así que usamos doNothing()
        doNothing().when(userServiceMock).deleteUser(1L);

        ResponseEntity<Void> response = userController.deleteUser(1L);

        assertNotNull(response);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        assertNull(response.getBody());
        verify(userServiceMock, times(1)).deleteUser(1L);
    }

    @Test
    @DisplayName("DELETE /api/user/{id} - Should return HTTP 404 Not Found if service throws RuntimeException (e.g., UserNotFound)")
    void deleteUser_UserNotFound_ShouldReturnNotFound() {
        // Simula que el servicio lanza una RuntimeException (podría ser una UserNotFoundException específica)
        doThrow(new RuntimeException("User not found")).when(userServiceMock).deleteUser(99L);

        ResponseEntity<Void> response = userController.deleteUser(99L);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        verify(userServiceMock, times(1)).deleteUser(99L);
    }
}