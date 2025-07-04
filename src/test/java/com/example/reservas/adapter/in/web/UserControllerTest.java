package com.example.reservas.adapter.in.web;

import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.UserController;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
import infrastructure.adapter.in.web.exception.GlobalExceptionHandler;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
    private PasswordEncoder passwordEncoderMock;

    @Mock
    private UserDTOMapper userDTOMapperMock;

    @InjectMocks
    private UserController userController;

    private User user1;
    private User user2;

    private UserResponseDTO userResponse1;
    private UserResponseDTO userResponse2;


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

        userResponse1 = new UserResponseDTO();
        userResponse1.setId(1L);
        userResponse1.setUsername("testuser1");
        userResponse1.setEmail("test1@example.com");
        userResponse1.setRole("USER");

        userResponse2 = new UserResponseDTO();
        userResponse2.setId(2L);
        userResponse2.setUsername("testuser2");
        userResponse2.setEmail("test2@example.com");
        userResponse2.setRole("ADMIN");
    }

    @Test
    @DisplayName("GET /api/user - Should return all users and HTTP 200 OK")
    void getAllUsers_ShouldReturnAllUsers() {
        List<User> users = Arrays.asList(user1, user2);
        when(userServiceMock.getAllUsers()).thenReturn(users);
        when(userDTOMapperMock.toDTO(user1)).thenReturn(userResponse1);
        when(userDTOMapperMock.toDTO(user2)).thenReturn(userResponse2);


        ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers();

        assertNotNull(response);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());
        assertEquals("testuser1", response.getBody().getFirst().getUsername());
        verify(userServiceMock, times(1)).getAllUsers();
    }

    @Test
    @DisplayName("GET /api/user - Should throw RuntimeException if service throws it")
    void getAllUsers_ServiceThrowsException_ShouldPropagateException() {
        when(userServiceMock.getAllUsers()).thenThrow(new RuntimeException("Service error"));

        assertThrows(
                RuntimeException.class,
                () -> userController.getAllUsers(),
                "Expected RuntimeException to be thrown"
        );
        verify(userServiceMock, times(1)).getAllUsers();
    }


    @Test
    @DisplayName("GET /api/user/{id} - Should return user and HTTP 200 OK when user found")
    void getUserById_WhenUserFound_ShouldReturnUser() {

        when(userServiceMock.findUserById(1L)).thenReturn(user1);
        when(userDTOMapperMock.toDTO(any(User.class))).thenReturn(userResponse1);

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
    @DisplayName("GET /api/user/{id} - Should throw RuntimeException if service throws it")
    void getUserById_ServiceThrowsException_ShouldPropagateException() {
        when(userServiceMock.findUserById(1L)).thenThrow(new RuntimeException("Service error"));

        assertThrows(
                RuntimeException.class,
                () -> userController.getUserById(1L),
                "Expected RuntimeException to be thrown"
        );
        verify(userServiceMock, times(1)).findUserById(1L);
    }

    @Test
    @DisplayName("POST /api/user - Should create user and return HTTP 201 Created")
    void createUser_ShouldReturnCreatedUser() {
        AdminUserCreationDTO newUserRequest = new AdminUserCreationDTO();
        newUserRequest.setUsername("newuser");
        newUserRequest.setEmail("new@example.com");
        newUserRequest.setPassword("password");
        newUserRequest.setRole("ADMIN");
        newUserRequest.setActive(true);

        User createdUser = new User();
        createdUser.setId(3L);
        createdUser.setUsername("newuser");
        createdUser.setEmail("new@example.com");
        createdUser.setPasswordHash("passwordHash");
        createdUser.setRole(Role.ADMIN);
        createdUser.setActive(true);

        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(3L);
        userResponse.setUsername("newuser");
        userResponse.setEmail("new@example.com");
        userResponse.setRole("ADMIN");
        userResponse.setActive(true);

        when(userServiceMock.createUser(any(User.class))).thenReturn(createdUser);
        when(userDTOMapperMock.toDomain(any(AdminUserCreationDTO.class))).thenReturn(createdUser);
        when(userDTOMapperMock.toDTO(any(User.class))).thenReturn(userResponse);

        ResponseEntity<UserResponseDTO> response = userController.createUser(newUserRequest);

        assertNotNull(response);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(3L, response.getBody().getId());
        assertEquals("newuser", response.getBody().getUsername());
        verify(userServiceMock, times(1)).createUser(any(User.class));
    }

    @Test
    @DisplayName("POST /api/user - Should throw DuplicateUserInfoException if service throws it")
    void createUser_ServiceThrowsDuplicateUserInfo_ShouldPropagateException() {
        AdminUserCreationDTO newUserRequest = new AdminUserCreationDTO();
        newUserRequest.setUsername("existinguser");

        when(userDTOMapperMock.toDomain(any(AdminUserCreationDTO.class))).thenReturn(new User());
        when(userServiceMock.createUser(any(User.class))).thenThrow(new DuplicateUserInfoException("User already exists"));

        // Act & Assert
        // Verificamos que el controlador lanza la excepción esperada
        DuplicateUserInfoException thrown = assertThrows(
                DuplicateUserInfoException.class,
                () -> userController.createUser(newUserRequest) // <-- La llamada al método que lanza
        );

        assertEquals("User already exists", thrown.getMessage()); // Opcional: verificar el mensaje de la excepción
        verify(userDTOMapperMock, times(1)).toDomain(any(AdminUserCreationDTO.class));
        verify(userServiceMock, times(1)).createUser(any(User.class));
    }


    @Test
    @DisplayName("PUT /api/user/{id} - Should update user and return HTTP 200 OK")
    void updateUser_ShouldReturnUpdatedUser() {
        UserUpdateDTO updatedUserRequest = new UserUpdateDTO();
        updatedUserRequest.setUsername("updateduser");
        // No se necesita setear el ID aquí porque el controlador lo hace con updatedUser.setId(id);

        User userAfterUpdate = new User();
        userAfterUpdate.setId(1L);
        userAfterUpdate.setUsername("updateduser");
        userAfterUpdate.setRole(Role.USER);

        UserResponseDTO userResponse = new UserResponseDTO();
        userResponse.setId(1L);
        userResponse.setUsername("updateduser");
        userResponse.setRole("USER");


        when(userServiceMock.updateUser(any(User.class))).thenReturn(userAfterUpdate);
        when(userDTOMapperMock.toDTO(any(User.class))).thenReturn((userResponse));
        when(userDTOMapperMock.toDomain(any(UserUpdateDTO.class))).thenReturn(userAfterUpdate);

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
    @DisplayName("PUT /api/user/{id} - Should throw UserNotFoundException if service throws it")
    void updateUser_UserNotFound_ShouldPropagateNotFoundException() {
        UserUpdateDTO updatedUserRequest = new UserUpdateDTO();
        updatedUserRequest.setUsername("updateduser");

        when(userDTOMapperMock.toDomain(any(UserUpdateDTO.class))).thenReturn(new User()); // Mapeo del DTO antes de llamar al servicio
        when(userServiceMock.updateUser(any(User.class))).thenThrow(new UserNotFoundException("User not found"));

        assertThrows(
                UserNotFoundException.class,
                () -> userController.updateUser(99L, updatedUserRequest),
                "Expected UserNotFoundException to be thrown"
        );
        verify(userDTOMapperMock, times(1)).toDomain(any(UserUpdateDTO.class));
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
    @DisplayName("DELETE /api/user/{id} - Should throw UserNotFoundException if service throws it")
    void deleteUser_UserNotFound_ShouldPropagateNotFoundException() {
        // Arrange
        // Asegúrate de que tu userServiceMock lance la excepción correcta para 'User not found'
        doThrow(new UserNotFoundException("User not found")).when(userServiceMock).deleteUser(99L);

        // Act & Assert
        assertThrows(
                UserNotFoundException.class,
                () -> userController.deleteUser(99L),
                "Expected UserNotFoundException to be thrown"
        );
        verify(userServiceMock, times(1)).deleteUser(99L);
    }
}