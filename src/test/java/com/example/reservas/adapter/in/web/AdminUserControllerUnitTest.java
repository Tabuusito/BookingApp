package com.example.reservas.adapter.in.web;
/*
import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.AdminUserController;
import infrastructure.adapter.in.web.dto.AdminUserCreationDTO;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.mapper.UserDTOMapper;
import infrastructure.adapter.in.web.security.RequesterContext;
import infrastructure.adapter.in.web.security.SpringSecurityUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerUnitTest {

    @Mock
    private UserService userServiceMock;

    @Mock
    private UserDTOMapper userDTOMapperMock;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private AdminUserController userController;

    private User user1, user2;
    private UserResponseDTO userResponse1, userResponse2;
    private RequesterContext adminRequesterContext;

    @BeforeEach
    void setUp() {
        user1 = new User(1L, "user", "user@example.com", "hash1", true, Role.USER);
        user2 = new User(2L, "admin", "admin@example.com", "hash2", true, Role.ADMIN);
        userResponse1 = new UserResponseDTO(1L, "user", "user@example.com", "USER", true);
        userResponse2 = new UserResponseDTO(2L, "admin", "admin@example.com", "ADMIN", true);
        adminRequesterContext = new RequesterContext(Optional.of(2L), Set.of("ROLE_ADMIN"));

        when(authenticationMock.isAuthenticated()).thenReturn(true);
        when(authenticationMock.getPrincipal()).thenReturn(new SpringSecurityUser(user2)); // user2 es el admin
        Collection<GrantedAuthority> adminAuthorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN"));
        doReturn(adminAuthorities).when(authenticationMock).getAuthorities();
    }

    @Test
    @DisplayName("getAllUsers - Should delegate to service and return mapped DTOs")
    void getAllUsers_ShouldDelegateToServiceAndMapResponse() {
        // Arrange
        when(userServiceMock.getAllUsers(any(RequesterContext.class))).thenReturn(Arrays.asList(user1, user2));
        when(userDTOMapperMock.toDTO(user1)).thenReturn(userResponse1);
        when(userDTOMapperMock.toDTO(user2)).thenReturn(userResponse2);

        // Act
        ResponseEntity<List<UserResponseDTO>> response = userController.getAllUsers(authenticationMock);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(2, response.getBody().size());
        assertEquals("user", response.getBody().get(0).getUsername());

        verify(userServiceMock).getAllUsers(any(RequesterContext.class));
        verify(userDTOMapperMock).toDTO(user1);
        verify(userDTOMapperMock).toDTO(user2);
    }

    @Test
    @DisplayName("getUserById - Should throw UserNotFoundException when service throws it")
    void getUserById_WhenServiceThrowsNotFound_ShouldPropagateException() {
        // Arrange
        when(userServiceMock.findUserById(eq(99L), any(RequesterContext.class))).thenThrow(new UserNotFoundException("Not Found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            userController.getUserById(99L, authenticationMock);
        });

        verify(userServiceMock).findUserById(eq(99L), argThat(requester ->
                requester.userId().equals(adminRequesterContext.userId()) &&
                        requester.roles().equals(adminRequesterContext.roles())
        ));
        verifyNoInteractions(userDTOMapperMock); // El mapper no debe ser llamado si hay excepción
    }

    @Test
    @DisplayName("createUser - Should throw DuplicateUserInfoException when service throws it")
    void createUser_WhenServiceThrowsDuplicate_ShouldPropagateException() {
        // Arrange
        AdminUserCreationDTO creationDTO = new AdminUserCreationDTO();
        creationDTO.setUsername("new");
        creationDTO.setEmail("new@example.com");
        creationDTO.setPassword("pass");
        creationDTO.setRole("USER");
        creationDTO.setActive(true);

        User userDomain = new User();
        userDomain.setUsername("new");
        userDomain.setEmail("new@example.com");
        userDomain.setPasswordHash("passHash");
        userDomain.setRole(Role.USER);
        userDomain.setActive(true);

        when(userDTOMapperMock.toDomain(eq(creationDTO))).thenReturn(userDomain);

        when(userServiceMock.createUser(eq(userDomain), any(RequesterContext.class)))
                .thenThrow(new DuplicateUserInfoException("User exists"));

        // Act & Assert
        assertThrows(DuplicateUserInfoException.class, () -> {
            userController.createUser(creationDTO, authenticationMock);
        });

        verify(userServiceMock).createUser(eq(userDomain), any(RequesterContext.class));
        verify(userDTOMapperMock).toDomain(eq(creationDTO));
        verifyNoMoreInteractions(userDTOMapperMock);
    }

    @Test
    @DisplayName("deleteUser - Should return NoContent when service executes successfully")
    void deleteUser_Success_ReturnsNoContent() {
        // Arrange
        long userIdToDelete = 1L;
        doNothing().when(userServiceMock).deleteUser(eq(userIdToDelete), any(RequesterContext.class));

        // Act
        ResponseEntity<Void> response = userController.deleteUser(userIdToDelete, authenticationMock);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userServiceMock).deleteUser(eq(userIdToDelete), argThat(requester ->
                requester.userId().equals(adminRequesterContext.userId()) &&
                        requester.roles().equals(adminRequesterContext.roles())
        ));
    }
}*/