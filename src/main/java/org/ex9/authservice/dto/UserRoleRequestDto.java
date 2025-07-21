package org.ex9.authservice.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class UserRoleRequestDto {

    @NotBlank(message = "User login must not be null")
    @Schema(description = "Login of the user to assign roles to",
            example = "user1",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String userLogin;

    @NotEmpty(message = "Roles array must not be empty")
    @ArraySchema(
            schema = @Schema(description = "List of role ID's",
                    example = "USER",
                    requiredMode = Schema.RequiredMode.REQUIRED
            )
    )
    private List<String> roles;

}
