package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    //private final AuthService authService;

    //public AuthController(AuthService authService) {
    public AuthController() {
        //this.authService = authService;
    }

    @Operation(
            summary = "Login",
            description = "Login for the users",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: login successful, returns a token"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody String credentials) {
        String token = "bien";

        if("hola".equals(credentials)){
            return new ResponseEntity<>(token, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(
            summary = "Logout",
            description = "Logout for the users",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: logout successful"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid token")
            }
    )
    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody String token) {
        if("adios".equals(token)){
            return new ResponseEntity<>(token, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }
}
