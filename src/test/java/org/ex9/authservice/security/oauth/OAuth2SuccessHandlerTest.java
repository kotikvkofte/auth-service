package org.ex9.authservice.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserAlreadyExistsException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.ex9.authservice.security.jwt.JwtService;
import org.ex9.authservice.security.services.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OAuth2SuccessHandlerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private OAuth2SuccessHandler oAuth2SuccessHandler;

    private OAuth2AuthenticationToken authentication;
    private final String email = "test@example.com";
    private final String jwtToken = "jwt-token";

    @BeforeEach
    void setUp() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", email);
        OAuth2User oAuth2User = new DefaultOAuth2User(Collections.emptyList(), attributes, "email");
        authentication = new OAuth2AuthenticationToken(oAuth2User, Collections.emptyList(), "google");
    }

    @Test
    void onAuthenticationSuccessTest_whenNewUserCreated_shouldRedirectWithJwt() throws IOException, ServletException {
        Role userRole = Role.builder()
                .id("USER")
                .build();

        User newUser = User.builder()
                .login(email)
                .email(email)
                .createDate(LocalDate.now())
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.of(userRole));
        when(userRepository.save(any(User.class))).thenReturn(newUser);
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn(jwtToken);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository).findByEmail(email);
        verify(roleRepository).findById("USER");
        verify(userRepository).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetailsImpl.class));
        verify(response).sendRedirect(eq("/login/success?token=" + jwtToken));
    }

    @Test
    void onAuthenticationSuccessTest_whenUserExist_shouldRedirectWithJwt() throws Exception {
        Role userRole = Role.builder()
                .id("USER")
                .build();

        User existingUser = User.builder()
                .login(email)
                .email(email)
                .createDate(LocalDate.now())
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));
        when(jwtService.generateToken(any(UserDetailsImpl.class))).thenReturn(jwtToken);

        oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication);

        verify(userRepository).findByEmail(email);
        verify(roleRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService).generateToken(any(UserDetailsImpl.class));
        verify(response).sendRedirect(eq("/login/success?token=" + jwtToken));
    }

    @Test
    void testOnAuthenticationSuccess_whenUserRegistered_throwUserAlreadyExist() throws IOException {
        Role userRole = Role.builder()
                .id("USER")
                .build();

        User existingUser = User.builder()
                .login(email)
                .email(email)
                .createDate(LocalDate.now())
                .password("password123")
                .roles(Set.of(userRole))
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () ->
                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication));
        verify(userRepository).findByEmail(email);
        verify(roleRepository, never()).findById(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetailsImpl.class));
        verify(response, never()).sendRedirect(anyString());
    }

    @Test
    void testOnAuthenticationSuccess_roleNotFound_throwRoleNotFoundException() throws IOException {
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () ->
                oAuth2SuccessHandler.onAuthenticationSuccess(request, response, authentication));
        verify(userRepository).findByEmail(email);
        verify(roleRepository).findById("USER");
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never()).generateToken(any(UserDetailsImpl.class));
        verify(response, never()).sendRedirect(anyString());
    }

}