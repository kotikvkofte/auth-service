package org.ex9.authservice.security;

import lombok.RequiredArgsConstructor;
import org.ex9.authservice.security.jwt.JwtAuthenticationFilter;
import org.ex9.authservice.security.jwt.JwtService;
import org.ex9.authservice.security.oauth.OAuth2SuccessHandler;
import org.ex9.authservice.security.services.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Конфигурация безопасности для приложения.
 * @author Краковцев Артём
 */
@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Сервис для работы с JWT.
     */
    private final JwtService jwtService;

    private final OAuth2SuccessHandler oAuth2SuccessHandler;

    /**
     * Сервис для загрузки данных пользователя.
     */
    private final UserDetailsServiceImpl userDetailsService;

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * @param http Конфигурация HTTP-безопасности.
     * @return Цепочка фильтров безопасности.
     * @throws Exception Если возникает ошибка конфигурации.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                                .requestMatchers("/auth/signup",
                                        "/auth/signin",
                                        "/swagger-ui/**",
                                        "v3/api-docs/**",
                                        "/login",
                                        "/login/success",
                                        "/js/**")
                                .permitAll()
                                .anyRequest().authenticated()
                        )
                .oauth2Login(login -> login
                        .loginPage("/login")
                        .successHandler(oAuth2SuccessHandler)
                )
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * Создаёт шифровщик паролей.
     *
     * @return BCryptPasswordEncoder для шифрования паролей.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Создаёт менеджер аутентификации.
     *
     * @param config Конфигурация аутентификации.
     * @return AuthenticationManager для обработки аутентификации.
     * @throws Exception Если возникает ошибка конфигурации.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
