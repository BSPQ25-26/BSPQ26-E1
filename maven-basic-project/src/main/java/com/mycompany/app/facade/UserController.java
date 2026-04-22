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
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users", description = "Endpoints for managing user accounts, including registration and profile retrieval")
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
        summary = "Register a new user",
        description = "Creates a new user account based on the provided registration details. Upon successful creation, the user will need to log in separately to obtain an authentication token.",
        responses = {
            @ApiResponse(responseCode = "201", description = "User successfully created. Please log in to continue."),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The user could not be created (e.g., credentials or roles failed validation)."),
            @ApiResponse(responseCode = "500", description = "Internal Server Error. An unexpected error occurred during user creation.")
        }
    )
    @PostMapping("/")
    public ResponseEntity<Void> createUser(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the user's registration details (e.g., email, password, name)", required = true)
        @RequestBody UserCreationDTO userCreationDTO
    ) {
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
        summary = "Get user profile information",
        description = "Retrieves the profile details of a specific user by their email address. This endpoint requires an active authorization token to ensure the requester has the proper access rights.",
        responses = {
            @ApiResponse(responseCode = "200", description = "User information retrieved successfully. Returns the user details payload."),
            @ApiResponse(responseCode = "400", description = "Bad Request. An error occurred while attempting to fetch the user data."),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The provided authorization token is invalid, missing, or expired."),
            @ApiResponse(responseCode = "404", description = "Not Found. No user exists with the provided email address.")
        }
    )
    @GetMapping("/{email}")
    public ResponseEntity<UserInfoDTO> getUserInfo(
        @Parameter(description = "The email address of the user to retrieve", required = true)
        @PathVariable String email,
        
        @Parameter(description = "A valid authorization token to verify the user's session", required = true)
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