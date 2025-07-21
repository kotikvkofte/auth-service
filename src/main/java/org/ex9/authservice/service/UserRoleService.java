package org.ex9.authservice.service;

import lombok.RequiredArgsConstructor;
import org.ex9.authservice.dto.UserRoleRequestDto;
import org.ex9.authservice.dto.UserRoleResponseDto;
import org.ex9.authservice.entity.Role;
import org.ex9.authservice.entity.User;
import org.ex9.authservice.exception.RoleNotFoundException;
import org.ex9.authservice.exception.UserNotFoundException;
import org.ex9.authservice.repository.RoleRepository;
import org.ex9.authservice.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.Set;
import java.util.stream.Collectors;

/**
 * Сервис для управления ролями пользователей.
 * @author Краковцев Артём
 */
@Service
@RequiredArgsConstructor
public class UserRoleService {

    /**
     * Репозиторий пользователей.
     */
    private final UserRepository userRepository;

    /**
     * Репозиторий ролей.
     */
    private final RoleRepository roleRepository;

    /**
     * Обновляет роли пользователя.
     *
     * @param dto DTO с логином пользователя и списком ролей.
     * @throws UserNotFoundException Если пользователь не найден.
     * @throws RoleNotFoundException Если одна из ролей не найдена.
     */
    public void updateUserRoles(UserRoleRequestDto dto) {
        User user = userRepository.findByLogin(dto.getUserLogin())
                .orElseThrow(() -> new UserNotFoundException("User with id " + dto.getUserLogin() + " not found"));

        Set<Role> roleEntities = dto.getRoles().stream()
                .map(roleId -> roleRepository.findById(roleId)
                        .orElseThrow(() -> new RoleNotFoundException("Role with id " + roleId + " not found")))
                .collect(Collectors.toSet());

        user.setRoles(roleEntities);
        userRepository.save(user);
    }

    /**
     * Получает роли пользователя.
     *
     * @param targetLogin Логин запрашиваемого пользователя.
     * @param currentLogin Логин текущего пользователя.
     * @param isAdmin      Флаг, указывающий, является ли текущий пользователь администратором.
     * @return DTO с логином и списком ролей пользователя.
     * @throws AccessDeniedException Если доступ запрещён.
     * @throws UserNotFoundException Если пользователь не найден.
     */
    public UserRoleResponseDto getRolesByLogin(String targetLogin, String currentLogin, boolean isAdmin) {

        if (!isAdmin && !targetLogin.equals(currentLogin)) {
            throw new AccessDeniedException("You are not allowed to view roles of other users");
        }

        User user = userRepository.findByLogin(targetLogin)
                .orElseThrow(() -> new UserNotFoundException("User with id " + targetLogin + " not found"));

        return UserRoleResponseDto.builder()
                .userLogin(targetLogin)
                .roles(user.getRoles())
                .build();
    }

}
