package org.ex9.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ex9.authservice.dto.UserRoleRequestDto;
import org.ex9.authservice.dto.UserRoleResponseDto;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserNotFoundException;
import org.ex9.authservice.service.UserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserRolesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRoleService userRoleService;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public UserRoleService userRoleService() {
            return Mockito.mock(UserRoleService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(userRoleService);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testUpdateRoles_success() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("user1");
        request.setRoles(List.of("USER", "CREDIT_USER"));

        doNothing().when(userRoleService).updateUserRoles(any(UserRoleRequestDto.class));

        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(userRoleService, times(1)).updateUserRoles(request);
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    void testUpdateRoles_whenWatchOtherLogin_accessDenied() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("user1");
        request.setRoles(List.of("USER"));

        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: Access Denied"));

        verify(userRoleService, never()).updateUserRoles(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testUpdateRoles_whenInvalidRequest_validationError() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("123");
        request.setRoles(List.of());

        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation error: roles: Roles array must not be empty"));

        verify(userRoleService, never()).updateUserRoles(any());
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testUpdateRoles_userNotFound() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("nonexistent");
        request.setRoles(List.of("USER"));

        doThrow(new UserNotFoundException("User with login nonexistent not found"))
                .when(userRoleService).updateUserRoles(request);

        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("User with login nonexistent not found"));

        verify(userRoleService, times(1)).updateUserRoles(request);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testUpdateRoles_roleNotFound() throws Exception {
        UserRoleRequestDto request = new UserRoleRequestDto();
        request.setUserLogin("user1");
        request.setRoles(List.of("INVALID_ROLE"));

        doThrow(new RoleNotFoundException("Role with ID INVALID_ROLE not found"))
                .when(userRoleService).updateUserRoles(any(UserRoleRequestDto.class));

        mockMvc.perform(put("/user-roles/save")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Role with ID INVALID_ROLE not found"));

        verify(userRoleService, times(1)).updateUserRoles(request);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testGetUserRoles_adminSuccess() throws Exception {
        Role role = new Role();
        role.setId("USER");
        UserRoleResponseDto response = UserRoleResponseDto.builder()
                .userLogin("user1")
                .roles(Set.of(role))
                .build();

        when(userRoleService.getRolesByLogin("user1", "admin", true))
                .thenReturn(response);

        mockMvc.perform(get("/user-roles/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("user1"))
                .andExpect(jsonPath("$.roles[0].id").value("USER"));

        verify(userRoleService, times(1)).getRolesByLogin("user1", "admin", true);
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    void testGetUserRoles_selfSuccess() throws Exception {
        Role role = new Role();
        role.setId("USER");
        UserRoleResponseDto response = UserRoleResponseDto.builder()
                .userLogin("user1")
                .roles(Set.of(role))
                .build();

        when(userRoleService.getRolesByLogin("user1", "user1", false))
                .thenReturn(response);

        mockMvc.perform(get("/user-roles/user1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userLogin").value("user1"))
                .andExpect(jsonPath("$.roles[0].id").value("USER"));

        verify(userRoleService, times(1)).getRolesByLogin("user1", "user1", false);
    }

    @Test
    @WithMockUser(username = "user1", authorities = {"USER"})
    void testGetUserRoles_AccessDenied() throws Exception {
        when(userRoleService.getRolesByLogin("admin", "user1", false))
                .thenThrow(new AccessDeniedException("viewing roles of other users is not available"));

        mockMvc.perform(get("/user-roles/admin"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value("Access denied: viewing roles of other users is not available"));

        verify(userRoleService, times(1)).getRolesByLogin("admin", "user1", false);
    }

    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    void testGetUserRoles_UserNotFound() throws Exception {
        when(userRoleService.getRolesByLogin(eq("nonexistent"), eq("admin"), eq(true)))
                .thenThrow(new UserNotFoundException("Пользователь с логином nonexistent не найден"));

        mockMvc.perform(get("/user-roles/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Пользователь с логином nonexistent не найден"));

        verify(userRoleService, times(1)).getRolesByLogin("nonexistent", "admin", true);
    }

}