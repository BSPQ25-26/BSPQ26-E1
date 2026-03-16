package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    @Operation(
        summary = "Create user",
        description = "Create a new user",
        responses = {
            @ApiResponse(responseCode = "200", description = "Creation OK"),
            @ApiResponse(responseCode = "401", description = "Invalid")
        }
    )
    @PostMapping("/")
    public ResponseEntity<Void> createUser(@RequestBody UserCreationDTO userCreationDTO) {
        if (!authService.isValidToken(userCreationDTO.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        try {
            userService.createUser(userCreationDTO);
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
