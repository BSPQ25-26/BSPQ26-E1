package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.service.AuthService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Authentication", description = "Endpoints for managing user access, token validation, and logout operations")
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Log in to the system",
        description = "Evaluates the provided credentials. If the username and password are correct, it generates and returns an access token (JWT) for making authenticated requests.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful login. Returns the access token."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials. Username or password do not match.")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Object containing the user's access credentials (e.g., email and password)", required = true)
        @RequestBody CredentialsDTO credentials
    ) {
        String token = authService.login(credentials);
        
        if (token == null) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @Operation(
        summary = "Log out from the system",
        description = "Allows an employee to close their active session. This endpoint invalidates the provided token, preventing it from being used in future requests.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful logout. The token has been invalidated."),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid, missing, or has already expired.")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
        @Parameter(description = "The active authorization token to be invalidated", required = true)
        @RequestParam("token") String token
    ) {
        if (authService.isValidToken(token)) {
            authService.logout(token);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Operation(
        summary = "Check token validity",
        description = "Verifies the status of a specific token. Useful for frontend clients to check if the user's session is still active before performing critical operations.",
        responses = {
            @ApiResponse(responseCode = "200", description = "The token is fully valid and the session is active."),
            @ApiResponse(responseCode = "401", description = "The token is invalid, malformed, or has expired.")
        }
    )
    @GetMapping("/check")
    public ResponseEntity<String> check(
        @Parameter(description = "The authorization token to verify", required = true)
        @RequestParam("token") String token
    ) {
        if (authService.isValidToken(token)) {
            return new ResponseEntity<>("Valid token", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
    }
}