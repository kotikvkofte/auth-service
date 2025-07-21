package org.ex9.authservice.security.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Сервис для работы с JWT-токенами: генерация, валидация и извлечение данных.
 * @author Краковцев Артём
 */
@Service
public class JwtService {

    /**
     * Секретный ключ для подписи токена.
     */
    @Value("${jwt.secret}")
    private String secret;

    /**
     * Время действия токена в миллисекундах.
     */
    @Value("${jwt.expiration}")
    private long expiration;

    /**
     * Генерирует JWT-токен для пользователя.
     *
     * @param userDetails Данные пользователя.
     * @return JWT-токен в виде строки.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList()
        );
        return createToken(claims, userDetails.getUsername());
    }

    /**
     * Создаёт JWT-токен с указанными claims и субъектом.
     *
     * @param claims  Дополнительные данные токена (например, роли).
     * @param subject Логин пользователя.
     * @return JWT-токен в виде строки.
     */
    private String createToken(Map<String, Object> claims, String subject) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Извлекает claims из JWT-токена.
     *
     * @param token JWT-токен.
     * @return Объект Claims с данными токена.
     */
    public Claims getClaims(String token) {
        SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .setSigningKey(key)
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Извлекает логин пользователя из токена.
     *
     * @param token JWT-токен.
     * @return Логин пользователя.
     */
    public String getLogin(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * Проверяет валидность токена.
     *
     * @param token       JWT-токен.
     * @param userDetails Данные пользователя.
     * @return true, если токен валиден, иначе false.
     */
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = getLogin(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    /**
     * Проверяет, истёк ли срок действия токена.
     *
     * @param token JWT-токен.
     * @return true, если срок действия истёк, иначе false.
     */
    private boolean isTokenExpired(String token) {
        return getClaims(token).getExpiration().before(new Date());
    }

}
