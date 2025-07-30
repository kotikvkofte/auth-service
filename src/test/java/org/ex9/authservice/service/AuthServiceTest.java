package org.ex9.authservice.service;

import org.ex9.authservice.dto.SignInRequestDto;
import org.ex9.authservice.dto.SignUpRequestDto;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserAlreadyExistsException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.ex9.authservice.security.jwt.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void testSignUp_success() {
        SignUpRequestDto request = new SignUpRequestDto("user1", "password123","user1@example.com");

        Role role = new Role();
        role.setId("USER");

        User savedUser = User.builder()
                .id(UUID.randomUUID())
                .login("user1")
                .email("user1@example.com")
                .password("$2a$10$hashedPassword")
                .createDate(LocalDate.now())
                .roles(Set.of(role))
                .build();

        when(userRepository.existsByLogin("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(roleRepository.findById("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode("password123")).thenReturn("$2a$10$hashedPassword");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        authService.signUp(request);

        verify(userRepository, times(1)).existsByLogin("user1");
        verify(userRepository, times(1)).existsByEmail("user1@example.com");
        verify(roleRepository, times(1)).findById("USER");
        verify(passwordEncoder, times(1)).encode("password123");
        verify(userRepository, times(1)).save(argThat(user ->
                user.getLogin().equals("user1") &&
                        user.getEmail().equals("user1@example.com") &&
                        user.getPassword().equals("$2a$10$hashedPassword") &&
                        user.getCreateDate().equals(LocalDate.now()) &&
                        user.getRoles().equals(Set.of(role))
        ));
    }

    @Test
    void testSignUp_loginAlreadyExists() {
        SignUpRequestDto request = new SignUpRequestDto("user1", "user1@example.com", "password123");

        when(userRepository.existsByLogin("user1")).thenReturn(true);

        UserAlreadyExistsException exception = assertThrows(
                UserAlreadyExistsException.class,
                () -> authService.signUp(request),
                "Ожидалось исключение UserAlreadyExistsException"
        );

        Role role = new Role();
        role.setId("USER");

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createDate(LocalDate.now())
                .roles(Set.of(role))
                .build();

        assertEquals("User with login user1 already exists", exception.getMessage());
        verify(userRepository, times(1)).existsByLogin("user1");
        verify(userRepository, never()).existsByEmail(request.getEmail());
        verify(roleRepository, never()).findById("USER");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSignUp_emailAlreadyExists() {
        SignUpRequestDto request = new SignUpRequestDto("user1", "password123", "user1@example.com");

        when(userRepository.existsByLogin("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.signUp(request));

        verify(userRepository, times(1)).existsByLogin("user1");
        verify(userRepository, times(1)).existsByEmail("user1@example.com");
        verify(roleRepository, never()).findById("USER");
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSignUp_roleNotFound() {
        SignUpRequestDto request = new SignUpRequestDto("user1", "password123", "user1@example.com");

        when(userRepository.existsByLogin("user1")).thenReturn(false);
        when(userRepository.existsByEmail("user1@example.com")).thenReturn(false);
        when(roleRepository.findById("USER")).thenReturn(Optional.empty());

        assertThrows(RoleNotFoundException.class, () -> authService.signUp(request));

        verify(userRepository, times(1)).existsByLogin("user1");
        verify(userRepository, times(1)).existsByEmail("user1@example.com");
        verify(roleRepository, times(1)).findById("USER");
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void testSignIn_success() {
        SignInRequestDto request = new SignInRequestDto("user1", "password123");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(jwtService.generateToken(userDetails)).thenReturn("jwt-token");

        String token = authService.signIn(request);

        assertEquals("jwt-token", token);
        verify(authenticationManager, times(1)).authenticate(
                argThat(auth -> auth.getPrincipal().equals("user1") && auth.getCredentials().equals("password123"))
        );
        verify(jwtService, times(1)).generateToken(userDetails);
    }

    @Test
    void testSignIn_authenticationFail() {
        SignInRequestDto request = new SignInRequestDto("user1", "wrong-password");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new org.springframework.security.core.AuthenticationException("Bad credentials") {});

        assertThrows(
                org.springframework.security.core.AuthenticationException.class,
                () -> authService.signIn(request),
                "Ожидалось исключение AuthenticationException"
        );

        verify(authenticationManager, times(1)).authenticate(
                argThat(auth -> auth.getPrincipal().equals("user1") && auth.getCredentials().equals("wrong-password"))
        );
        verify(jwtService, never()).generateToken(any());
    }

}