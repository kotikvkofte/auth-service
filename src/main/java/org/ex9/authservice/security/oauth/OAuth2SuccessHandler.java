package org.ex9.authservice.security.oauth;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserAlreadyExistsException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.ex9.authservice.security.jwt.JwtService;
import org.ex9.authservice.security.services.UserDetailsImpl;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Set;

/**
 * Обработчик успешной авторизации OAuth2.
 * Создаёт пользователя, если он ещё не существует, или возвращает JWT токен.
 * Перенаправляет на /login/success с параметром token.
 * @author Краковцев Артём
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final JwtService jwtService;
    private static final String REDIRECT_URI = "/login/success?token=";

    /**
     * Вызывается при успешной аутентификации через OAuth2.
     *
     * @param request        HTTP-запрос
     * @param response       HTTP-ответ
     * @param authentication объект аутентификации OAuth2
     * @throws IOException      в случае ошибки при редиректе
     * @throws ServletException в случае ошибки обработки сервлета
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        String email = token.getPrincipal().getAttribute("email");

        User user = userRepository.findByEmail(email).orElseGet(() -> registerUser(email));

        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            throw new UserAlreadyExistsException("User with email " + email + " already exists");
        }

        String jwt = jwtService.generateToken(new UserDetailsImpl(user));
        response.sendRedirect(REDIRECT_URI + jwt);
    }

    /**
     * Регистрирует нового пользователя с ролью USER.
     *
     * @param email email, полученный от OAuth-провайдера
     * @return новый пользователь
     */
    private User registerUser(String email) {
        Role role = roleRepository.findById("USER")
                .orElseThrow(() -> new RoleNotFoundException("Role not found"));

        User newUser = User.builder()
                .login(email)
                .email(email)
                .createDate(LocalDate.now())
                .roles(Set.of(role))
                .build();

        return userRepository.save(newUser);
    }

}
