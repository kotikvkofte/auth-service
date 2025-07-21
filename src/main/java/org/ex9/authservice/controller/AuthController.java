package org.ex9.authservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.ex9.authservice.dto.SignInRequestDto;
import org.ex9.authservice.dto.SignUpRequestDto;
import org.ex9.authservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "API for user registration and authentication")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Register a new user", description = "Creates a new user with the provided login, password, and email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "User already exists or invalid input provided")
    })
    @PutMapping("/signup")
    public ResponseEntity<Void> signUp(@Valid @RequestBody SignUpRequestDto request) {
        authService.signUp(request);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Authenticate user", description = "Authenticates a user with login and password and returns a JWT token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "JWT token returned"),
            @ApiResponse(responseCode = "401", description = "Invalid login or password")
    })
    @PostMapping("/signin")
    public ResponseEntity<String> signIn(@Valid @RequestBody SignInRequestDto request) {
        String token = authService.signIn(request);
        return ResponseEntity.ok(token);
    }

}
