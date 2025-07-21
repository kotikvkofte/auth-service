package org.ex9.authservice.service;

import org.ex9.authservice.dto.UserRoleRequestDto;
import org.ex9.authservice.dto.UserRoleResponseDto;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserNotFoundException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRoleServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserRoleService userRoleService;

    @Test
    void testUpdateUserRoles_success() {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("user1");
        request.setRoles(List.of("USER", "CREDIT_USER"));

        User user = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .build();

        Role userRole = new Role();
        userRole.setId("USER");
        Role creditUserRole = new Role();
        creditUserRole.setId("CREDIT_USER");

        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findById("CREDIT_USER")).thenReturn(Optional.of(creditUserRole));
        when(userRepository.save(any(User.class))).thenReturn(user);

        userRoleService.updateUserRoles(request);

        verify(userRepository, times(1)).findByLogin("user1");
        verify(roleRepository, times(1)).findById("USER");
        verify(roleRepository, times(1)).findById("CREDIT_USER");
        verify(userRepository, times(1)).save(any());
    }

    @Test
    void testUpdateUserRoles_userNotFound() {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("nonexistent");
        request.setRoles(List.of("USER"));

        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userRoleService.updateUserRoles(request),
                "Ожидалось исключение UserNotFoundException"
        );

        assertEquals("User with id nonexistent not found", exception.getMessage());
        verify(userRepository, times(1)).findByLogin("nonexistent");
        verify(roleRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testUpdateUserRoles_roleNotFound() {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("user1");
        request.setRoles(List.of("USER", "INVALID_ROLE"));

        User user = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .build();

        Role userRole = new Role();
        userRole.setId("USER");

        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        when(roleRepository.findById("INVALID_ROLE")).thenReturn(Optional.empty());

        RoleNotFoundException exception = assertThrows(
                RoleNotFoundException.class,
                () -> userRoleService.updateUserRoles(request),
                "Ожидалось исключение RoleNotFoundException"
        );

        assertEquals("Role with id INVALID_ROLE not found", exception.getMessage());
        verify(userRepository, times(1)).findByLogin("user1");
        verify(roleRepository, times(1)).findById("USER");
        verify(roleRepository, times(1)).findById("INVALID_ROLE");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testGetRolesByLogin_adminSuccess() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .roles(Set.of(Role.builder().id("USER").build()))
                .build();

        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));

        UserRoleResponseDto response = userRoleService.getRolesByLogin("user1", "admin", true);

        assertEquals("user1", response.getUserLogin());
        assertEquals("USER", response.getRoles().iterator().next().getId());
        verify(userRepository, times(1)).findByLogin("user1");
    }

    @Test
    void testGetRolesByLogin_selfSuccess() {
        User user = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .roles(Set.of(Role.builder().id("USER").build()))
                .build();

        when(userRepository.findByLogin("user1")).thenReturn(Optional.of(user));

        UserRoleResponseDto response = userRoleService.getRolesByLogin("user1", "user1", false);

        assertEquals("user1", response.getUserLogin());
        assertEquals("USER", response.getRoles().iterator().next().getId());
        verify(userRepository, times(1)).findByLogin("user1");
    }

    @Test
    void testGetRolesByLogin_accessDenied() {
        assertThrows(AccessDeniedException.class, () -> userRoleService.getRolesByLogin("admin", "user1", false));

        verify(userRepository, never()).findByLogin("admin");
    }

    @Test
    void testGetRolesByLogin_userNotFound() {
        when(userRepository.findByLogin("nonexistent")).thenReturn(Optional.empty());

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> userRoleService.getRolesByLogin("nonexistent", "admin", true),
                "Ожидалось исключение UserNotFoundException"
        );

        assertEquals("User with id nonexistent not found", exception.getMessage());
        verify(userRepository, times(1)).findByLogin("nonexistent");
    }

}