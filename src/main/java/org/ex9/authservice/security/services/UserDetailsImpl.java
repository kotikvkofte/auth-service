package org.ex9.authservice.security.services;

import lombok.RequiredArgsConstructor;
import org.ex9.authservice.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Реализация UserDetails для представления данных пользователя в Spring Security.
 * @author Краковцев Артём
 */
@RequiredArgsConstructor
public class UserDetailsImpl implements UserDetails {

    /**
     * Объект пользователя.
     */
    private final User user;

    /**
     * Возвращает список ролей пользователя в виде объектов GrantedAuthority.
     *
     * @return Коллекция ролей.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getId()))
                .collect(Collectors.toSet());
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return Пароль.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Возвращает логин пользователя.
     *
     * @return Логин.
     */
    @Override
    public String getUsername() {
        return user.getLogin();
    }

}
