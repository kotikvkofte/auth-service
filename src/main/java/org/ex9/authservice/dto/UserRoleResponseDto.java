package org.ex9.authservice.dto;

import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;
import org.ex9.authservice.entity.Role;

import java.util.Set;

@Data
@Builder
@Schema(description = "Response containing user roles")
public class UserRoleResponseDto {

    @Schema(description = "User login", example = "user1")
    private String userLogin;

    @ArraySchema(
            schema = @Schema(description = "List of role identifiers", example = "USER")
    )
    private Set<Role> roles;

}
