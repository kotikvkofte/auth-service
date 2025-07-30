package org.ex9.authservice.security.services;

import lombok.RequiredArgsConstructor;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя по логину для Spring Security.
 * @author Краковцев Артём
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Загружает данные пользователя по логину.
     *
     * @param username Логин пользователя.
     * @return Объект UserDetails с данными пользователя.
     * @throws UsernameNotFoundException Если пользователь не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException(username));
        user.getRoles();
        return new UserDetailsImpl(user);
    }

}
