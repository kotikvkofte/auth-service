package org.ex9.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ex9.authservice.dto.UserRoleRequestDto;
import org.ex9.authservice.dto.UserRoleResponseDto;
import org.ex9.authservice.service.UserRoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("user-roles")
@Tag(name = "User Roles", description = "API for managing user roles")
public class UserRolesController {

    private final UserRoleService userRoleService;

    @Operation(summary = "Assign roles to a user",
            description = "Updates the roles for a specified user. Only accessible to users with ADMIN role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Roles assigned successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid user or roles"),
            @ApiResponse(responseCode = "403", description = "Access denied: ADMIN role required")
    })
    @PutMapping("/save")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> updateRoles(@Valid @RequestBody UserRoleRequestDto request) {

        userRoleService.updateUserRoles(request);

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get user roles",
            description = "Retrieves the roles for a specified user. Accessible to ADMIN or the user themselves.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User roles retrieved successfully"),
            @ApiResponse(responseCode = "403", description = "Access denied: Only ADMIN or the user themselves can view roles"),
            @ApiResponse(responseCode = "404", description = "User not found")
    })
    @GetMapping("/{login}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserRoleResponseDto> getUserRoles(@PathVariable String login) {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String currentLogin = auth.getName();
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        var res = userRoleService.getRolesByLogin(login, currentLogin, isAdmin);
        return ResponseEntity.ok(res);
    }

}
