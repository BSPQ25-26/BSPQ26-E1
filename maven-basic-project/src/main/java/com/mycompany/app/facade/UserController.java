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

/**
 * @file UserController.java
 * @brief REST controller for user account management.
 *
 * This controller constitutes the <b>Remote Method Invocation (RMI) interface</b>
 * for the user-management subsystem of the finance application. It is mapped
 * to the base path {@code /user} and exposes the following operations:
 *
 * <ul>
 *   <li><b>User registration</b> &ndash; create a new user account with
 *       a username, email, password, and initial balance.</li>
 *   <li><b>Profile retrieval</b> &ndash; fetch a user's public profile
 *       information (username, email, balance) by email address.</li>
 * </ul>
 *
 * <h2>Authentication workflow</h2>
 * <ol>
 *   <li>A new user registers via {@link #createUser(UserCreationDTO)}.
 *       This endpoint does <em>not</em> require a JWT token.</li>
 *   <li>After registration the user must authenticate through
 *       {@link AuthController#login(com.mycompany.app.dto.CredentialsDTO)}
 *       to obtain a valid JWT token.</li>
 *   <li>All subsequent endpoints (e.g.&nbsp;{@link #getUserInfo(String, String)})
 *       require the JWT token for authorization.</li>
 * </ol>
 *
 * <h2>Security model</h2>
 * <ul>
 *   <li>{@code createUser} &ndash; <b>unauthenticated</b>; any client can
 *       register a new account.</li>
 *   <li>{@code getUserInfo} &ndash; <b>authenticated</b>; requires a valid
 *       JWT verified via {@link AuthService#isValidToken(String)}.</li>
 * </ul>
 *
 * <h2>Error handling</h2>
 * <ul>
 *   <li>HTTP 200 &ndash; operation succeeded (profile retrieval).</li>
 *   <li>HTTP 201 &ndash; resource created (user registration).</li>
 *   <li>HTTP 400 &ndash; invalid request or unexpected error.</li>
 *   <li>HTTP 401 &ndash; authentication failure (invalid/expired JWT or duplicate email on registration).</li>
 *   <li>HTTP 404 &ndash; requested user not found.</li>
 *   <li>HTTP 500 &ndash; unexpected server-side error.</li>
 * </ul>
 *
 * @author  BSPQ26-E1 Team
 * @version 2.0
 * @since   2026-05-01
 *
 * @see UserService          Business logic layer for user operations
 * @see AuthService           JWT token validation service
 * @see AuthController        Login endpoint for obtaining JWT tokens
 * @see UserCreationDTO       DTO for user registration requests
 * @see UserInfoDTO           DTO for user profile responses
 * @see com.mycompany.app.model.Usuario  User entity model
 */
@Tag(name = "Users", description = "Endpoints for managing user accounts, including registration and profile retrieval")
@RestController
@RequestMapping("/user")
public class UserController {

    /**
     * Service layer responsible for user-related business logic,
     * including account creation and profile data retrieval.
     *
     * @see UserService
     */
    private final UserService userService;

    /**
     * Service layer responsible for JWT token validation.
     * Called at the beginning of every authenticated endpoint to
     * enforce authorization.
     *
     * @see AuthService
     */
    private final AuthService authService;

    /**
     * Constructs a new {@code UserController} with the required service
     * dependencies. Spring injects both beans automatically via constructor
     * injection.
     *
     * @param userService  the service handling user creation and profile
     *                     retrieval &ndash; must not be {@code null}
     * @param authService  the service handling JWT token validation
     *                     &ndash; must not be {@code null}
     */
    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * @brief Registers a new user account in the system.
     *
     * <b>Endpoint:</b> {@code POST /user/}
     *
     * Creates a new user with the provided registration details. The email
     * address must be unique; if a user with the same email already exists
     * the request is rejected with HTTP&nbsp;401. This is the only endpoint
     * in the controller that does <em>not</em> require a JWT token.
     *
     * After successful creation the user must authenticate via
     * {@link AuthController#login(com.mycompany.app.dto.CredentialsDTO)}
     * to obtain a JWT token for subsequent API calls.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "username": "John Doe",
     *   "email":    "john.doe@example.com",
     *   "password": "s3cur3P@ss",
     *   "balance":  100.00
     * }
     * }</pre>
     *
     * @param userCreationDTO the {@link UserCreationDTO} containing:
     *        <ul>
     *          <li>{@code username} &ndash; display name for the user (required)</li>
     *          <li>{@code email}    &ndash; unique email address used as the login identifier (required)</li>
     *          <li>{@code password} &ndash; plaintext password for the account (required)</li>
     *          <li>{@code balance}  &ndash; initial account balance in euros (required)</li>
     *        </ul>
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 201 (Created) &ndash; user account created successfully</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; a user with the given email already
     *               exists, or validation failed</li>
     *           <li>HTTP 500 (Internal Server Error) &ndash; unexpected server-side error</li>
     *         </ul>
     *
     * @pre  No user with the same email address may exist in the database.
     * @post A new {@link com.mycompany.app.model.Usuario} record is persisted
     *       with the provided username, email, password, and balance.
     *
     * @see UserCreationDTO
     * @see UserService#createUser(UserCreationDTO)
     * @see AuthController
     */
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

    /**
     * @brief Retrieves the profile information of a user by their email address.
     *
     * <b>Endpoint:</b> {@code GET /user/{email}?token=...}
     *
     * Looks up the user record matching the given email and returns their
     * public profile data (username, email, current balance) wrapped in a
     * {@link UserInfoDTO}. The email is passed as a path variable and the
     * JWT token as a query parameter.
     *
     * <b>Example request URL:</b>
     * <pre>{@code
     * GET /user/john.doe@example.com?token=eyJhbGciOiJIUzI1NiIs...
     * }</pre>
     *
     * <b>JSON response body example (HTTP 200):</b>
     * <pre>{@code
     * {
     *   "username": "John Doe",
     *   "email":    "john.doe@example.com",
     *   "balance":  142.50
     * }
     * }</pre>
     *
     * @param email the email address of the user to look up, supplied as a
     *              path variable (required)
     * @param token a valid JWT authorization token, supplied as a query
     *              parameter named {@code token} (required)
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; body contains the {@link UserInfoDTO}
     *               with the user's username, email, and balance</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *           <li>HTTP 404 (Not Found) &ndash; no user exists with the given
     *               email address</li>
     *           <li>HTTP 400 (Bad Request) &ndash; unexpected error during retrieval</li>
     *         </ul>
     *
     * @pre  The caller must provide a valid JWT obtained from
     *       {@link AuthController#login(com.mycompany.app.dto.CredentialsDTO)}.
     *
     * @note This is the only endpoint in the controller that uses
     *       {@code @GetMapping} with a path variable and query parameter.
     *
     * @see UserInfoDTO
     * @see UserService#getUserInfo(String)
     */
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