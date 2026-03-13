package com.mycompany.app.facade;

import com.mycompany.app.dto.AuthResponseDTO;
import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Login",
        description = "Authenticates via Supabase and returns a JWT plus local user info.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
            @ApiResponse(responseCode = "404", description = "User not found in system")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@RequestBody CredentialsDTO credentials) {
        return ResponseEntity.ok(authService.login(credentials));
    }

    @Operation(
        summary = "Create account",
        description = "Registers via Supabase and creates a local usuario row.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Account created, token returned"),
            @ApiResponse(responseCode = "409", description = "Email already registered"),
            @ApiResponse(responseCode = "400", description = "Bad request")
        }
    )
    @PostMapping("/create")
    public ResponseEntity<AuthResponseDTO> createUser(@RequestBody UserCreationDTO userCreationDTO) {
        return ResponseEntity.ok(authService.create(userCreationDTO));
    }

    @Operation(
        summary = "Logout",
        description = "Invalidates the JWT on Supabase. Requires Bearer token in Authorization header.",
        security = @SecurityRequirement(name = "bearerAuth"),
        responses = {
            @ApiResponse(responseCode = "200", description = "Logged out successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or missing token")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.startsWith("Bearer ") ? authHeader.substring(7) : authHeader;
        authService.logout(token);
        return ResponseEntity.ok().build();
    }
}
