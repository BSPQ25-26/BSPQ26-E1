package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.dto.UserInfoDTO;
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
            @ApiResponse(responseCode = "200", description = "Creation OK, now you are required to login"),
            @ApiResponse(responseCode = "401", description = "Invalid")
        }
    )
    @PostMapping("/")
    public ResponseEntity<Void> createUser(@RequestBody UserCreationDTO userCreationDTO) {
        try {
            if (!userService.createUser(userCreationDTO)){
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @Operation(
        summary = "Get user info",
        description = "Retrieve user information by email",
        responses = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "401", description = "Invalid token"),
            @ApiResponse(responseCode = "404", description = "User not found")
        }
    )
    @GetMapping("/{email}")
    public ResponseEntity<UserInfoDTO> getUserInfo(
        @PathVariable String email,
        @RequestParam("token") String token
    ) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        

        try {
            UserInfoDTO usuario = userService.getUserInfo(email); 
            if (usuario == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(usuario, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
