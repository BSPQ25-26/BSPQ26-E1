package com.mycompany.app.facade;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/auth")
public class AuthController {
    //private final AuthService authService;
    private final UsuarioRepository usuarioRepository;

    //public AuthController(AuthService authService) {
    public AuthController(UsuarioRepository usuarioRepository) {
        //this.authService = authService;
        this.usuarioRepository = usuarioRepository;
        
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
    public ResponseEntity<String> login(@RequestBody CredentialsDTO credentials) {

        if("user".equals(credentials.getEmail()) && "password".equals(credentials.getPassword())){
            return new ResponseEntity<>("muy bien", HttpStatus.OK);
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
        String aa = usuarioRepository.findAll().get(0).getNombre();
        
        if("adios".equals(token)){
            return new ResponseEntity<>(aa, HttpStatus.OK);
        }else{
            return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
        }
    }

    @Operation(
            summary = "Create account",
            description = "User creation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: account creation, returns a token"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createUser(@RequestBody UserCreationDTO userCreationDTO) {
        return new ResponseEntity<>("token", HttpStatus.OK);
    }
}
