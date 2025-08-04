package org.ex9.authservice.repository;

import jakarta.validation.constraints.NotNull;
import org.ex9.authservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByLogin(String login);

    boolean existsByEmail(@NotNull String email);

    boolean existsByLogin(@NotNull String login);

    Optional<User> findByEmail(String email);

}
