package org.ex9.authservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@Schema(description = "Request for user authentication")
public class SignInRequestDto {

    @NotBlank(message = "Password must be not blank")
    @Schema(description = "User login",
            example = "user1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String login;

    @NotBlank(message = "Password must be not blank")
    @Schema(description = "User password",
            example = "password123",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String password;

}
