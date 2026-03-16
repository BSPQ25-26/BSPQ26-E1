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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Login",
        description = "Login",
        responses = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody CredentialsDTO credentials) {
        String token = authService.login(credentials);
        
        if (token == null) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    @Operation(
        summary = "Logout from the system",
        description = "Allows an employee to log out by providing a valid authorization token.",
        responses = {
            @ApiResponse(responseCode = "200", description = "No Content: Logout successful"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: Invalid token")
        }
    )
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam("token") String token) {
        if (authService.isValidToken(token)) {
            authService.logout(token);
            return new ResponseEntity<>(HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @Operation(
        summary = "Check",
        description = "Check",
        responses = {
            @ApiResponse(responseCode = "200", description = "Checked out successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid")
        }
    )
    @GetMapping("/check")
    public ResponseEntity<String> check(
        @RequestParam("token") String token
    ) {
        if (authService.isValidToken(token)) {
            return new ResponseEntity<>("Valid token", HttpStatus.OK);
        }
        return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
    }
}
