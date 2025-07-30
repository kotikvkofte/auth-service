package org.ex9.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * DTO для регистрации нового пользователя.
 * @author Краковцев Артём
 */
@Data
@AllArgsConstructor
@Schema(description = "Request for user registration")
public class SignUpRequestDto {

    /**
     * Логин нового пользователя.
     */
    @NotBlank(message = "login must not be blank")
    @Schema(description = "User login",
            example = "user1",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String login;

    /**
     * Пароль нового пользователя.
     */
    @NotBlank(message = "login must not be blank")
    @Schema(description = "User password",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

    /**
     * Электронная почта нового пользователя.
     */
    @NotBlank(message = "login must not be blank")
    @Email
    @Schema(description = "User email address",
            example = "user1@example.com",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String email;

}
