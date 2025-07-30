package org.ex9.authservice.service;

import lombok.RequiredArgsConstructor;
import org.ex9.authservice.dto.SignInRequestDto;
import org.ex9.authservice.dto.SignUpRequestDto;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserAlreadyExistsException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.ex9.authservice.security.jwt.JwtService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Set;

/**
 * Сервис для обработки аутентификации и регистрации пользователей.
 * @author Краковцев Артём
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {

    /**
     * Менеджер аутентификации.
     */
    private final AuthenticationManager authenticationManager;

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий ролей.
     */
    private final RoleRepository roleRepository;

    /**
     * Шифровщик паролей.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Сервис для работы с JWT.
     */
    private final JwtService jwtService;

    /**
     * Регистрирует нового пользователя в системе.
     *
     * @param request DTO с данными для регистрации.
     * @throws UserAlreadyExistsException Если логин или email уже заняты.
     * @throws RoleNotFoundException      Если роль не найдена.
     */
    public void signUp(SignUpRequestDto request) {
        if (userRepository.existsByLogin(request.getLogin())) {
            throw new UserAlreadyExistsException("User with login " + request.getLogin() + " already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("User with email " + request.getEmail() + " already exists");
        }

        Role role = roleRepository.findById("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        User user = User.builder()
                .login(request.getLogin())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .createDate(LocalDate.now())
                .roles(Set.of(role))
                .build();

        userRepository.save(user);
    }

    /**
     * Аутентифицирует пользователя и возвращает JWT-токен.
     *
     * @param request DTO с логином и паролем.
     * @return WT-токен.
     */
    public String signIn(SignInRequestDto request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getLogin(), request.getPassword()));
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return jwtService.generateToken(userDetails);
    }

}
