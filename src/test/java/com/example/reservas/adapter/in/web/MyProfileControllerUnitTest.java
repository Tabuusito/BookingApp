package com.example.reservas.adapter.in.web;

import domain.exception.DuplicateUserInfoException;
import domain.exception.UserNotFoundException;
import domain.model.Role;
import domain.model.User;
import domain.port.in.UserService;
import infrastructure.adapter.in.web.controller.MyProfileController;
import infrastructure.adapter.in.web.dto.UserResponseDTO;
import infrastructure.adapter.in.web.dto.UserUpdateDTO;
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

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MyProfileControllerUnitTest {

    @Mock
    private UserService userServiceMock;

    @Mock
    private UserDTOMapper userDTOMapperMock;

    @Mock
    private Authentication authenticationMock;

    @InjectMocks
    private MyProfileController myProfileController;

    private User authenticatedUser;
    private UserResponseDTO userResponseDTO;
    private RequesterContext userRequesterContext;

    @BeforeEach
    void setUp() {
        authenticatedUser = new User(1L, "myuser", "myuser@example.com", "hash1", true, Role.USER);
        userResponseDTO = new UserResponseDTO(1L, "myuser", "myuser@example.com", "USER", true);
        userRequesterContext = new RequesterContext(Optional.of(1L), Set.of("ROLE_USER"));

        // Simular un usuario autenticado
        when(authenticationMock.isAuthenticated()).thenReturn(true);
        when(authenticationMock.getPrincipal()).thenReturn(new SpringSecurityUser(authenticatedUser));
        Collection<GrantedAuthority> userAuthorities = Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"));
        doReturn(userAuthorities).when(authenticationMock).getAuthorities();    }

    @Test
    @DisplayName("getMyProfile - Should return the authenticated user's profile")
    void getMyProfile_ShouldReturnProfile() {
        // Arrange
        // Usamos argThat para verificar el contenido del RequesterContext si es necesario
        when(userServiceMock.getMyProfile(argThat(context -> context.userId().get().equals(1L)))).thenReturn(authenticatedUser);
        when(userDTOMapperMock.toDTO(authenticatedUser)).thenReturn(userResponseDTO);

        // Act
        ResponseEntity<UserResponseDTO> response = myProfileController.getMyProfile(authenticationMock);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("myuser", response.getBody().getUsername());
        verify(userServiceMock).getMyProfile(any(RequesterContext.class));
        verify(userDTOMapperMock).toDTO(authenticatedUser);
    }

    @Test
    @DisplayName("getMyProfile - Should propagate UserNotFoundException from service")
    void getMyProfile_WhenUserNotInDB_ShouldPropagateException() {
        // Arrange
        when(userServiceMock.getMyProfile(any(RequesterContext.class)))
                .thenThrow(new UserNotFoundException("User not found"));

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> {
            myProfileController.getMyProfile(authenticationMock);
        });
        verify(userServiceMock).getMyProfile(any(RequesterContext.class));
        verifyNoInteractions(userDTOMapperMock);
    }

    @Test
    @DisplayName("updateMyProfile - Should successfully update the profile")
    void updateMyProfile_ShouldUpdateProfile() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        updateDTO.setUsername("updatedUsername");

        User userToUpdate = new User();
        userToUpdate.setUsername("updatedUsername");

        User updatedUserDomain = new User(1L, "updatedUsername", "myuser@example.com", "hash1", true, Role.USER);
        UserResponseDTO updatedResponseDTO = new UserResponseDTO(1L, "updatedUsername", "myuser@example.com", "USER", true);

        when(userDTOMapperMock.toDomain(any(UserUpdateDTO.class))).thenReturn(userToUpdate);
        when(userServiceMock.updateMyProfile(eq(userToUpdate), any(RequesterContext.class))).thenReturn(updatedUserDomain);
        when(userDTOMapperMock.toDTO(updatedUserDomain)).thenReturn(updatedResponseDTO);

        // Act
        ResponseEntity<UserResponseDTO> response = myProfileController.updateMyProfile(updateDTO, authenticationMock);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("updatedUsername", response.getBody().getUsername());
        verify(userServiceMock).updateMyProfile(eq(userToUpdate), any(RequesterContext.class));
    }

    @Test
    @DisplayName("updateMyProfile - Should propagate DuplicateUserInfoException from service")
    void updateMyProfile_WhenDuplicate_ShouldPropagateException() {
        // Arrange
        UserUpdateDTO updateDTO = new UserUpdateDTO();
        User userToUpdate = new User();
        when(userDTOMapperMock.toDomain(updateDTO)).thenReturn(userToUpdate);
        when(userServiceMock.updateMyProfile(eq(userToUpdate), any(RequesterContext.class)))
                .thenThrow(new DuplicateUserInfoException("Username exists"));

        // Act & Assert
        assertThrows(DuplicateUserInfoException.class, () -> {
            myProfileController.updateMyProfile(updateDTO, authenticationMock);
        });
    }

    @Test
    @DisplayName("deleteMyProfile - Should return 204 No Content on successful deletion")
    void deleteMyProfile_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(userServiceMock).deleteMyProfile(any(RequesterContext.class));

        // Act
        ResponseEntity<Void> response = myProfileController.deleteMyProfile(authenticationMock);

        // Assert
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userServiceMock).deleteMyProfile(any(RequesterContext.class));
    }
}
