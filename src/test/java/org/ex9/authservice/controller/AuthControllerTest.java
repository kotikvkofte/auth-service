package org.ex9.authservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.ex9.authservice.dto.SignInRequestDto;
import org.ex9.authservice.dto.SignUpRequestDto;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserAlreadyExistsException;
import org.ex9.authservice.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AuthService service;

    @TestConfiguration
    static class MockConfig {
        @Bean
        public AuthService authService() {
            return Mockito.mock(AuthService.class);
        }
    }

    @BeforeEach
    void setUp() {
        reset(service);
    }

    @Test
    void testSignUpSuccess() throws Exception {
        SignUpRequestDto dto = new SignUpRequestDto("testuser", "pass123", "email@test.com");

        doNothing().when(service).signUp(dto);

        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignUp_whenUserExist_returnBadRequest() throws Exception {
        SignUpRequestDto dto = new SignUpRequestDto("testuser", "pass123", "email@test.com");

        doThrow(UserAlreadyExistsException.class).when(service).signUp(dto);

        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testSignUp_whenRoleNotFound_returnNotFound() throws Exception {
        SignUpRequestDto dto = new SignUpRequestDto("testuser", "pass123", "email@test.com");

        doThrow(RoleNotFoundException.class).when(service).signUp(dto);

        mockMvc.perform(put("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").isEmpty())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.timestamp").isString());
    }

    @Test
    void testSignIn_returnsJwt() throws Exception {
        SignInRequestDto dto = new SignInRequestDto("testuser", "pass123");
        Mockito.when(service.signIn(any())).thenReturn("mocked-jwt-token");

        mockMvc.perform(post("/auth/signin")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(content().string("mocked-jwt-token"));
    }

}