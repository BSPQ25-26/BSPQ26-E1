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

/**
 * @file AuthController.java
 * @brief REST controller for authentication and session management (RMI facade layer).
 *
 * This controller exposes the remote HTTP API endpoints that govern the entire
 * authentication lifecycle of the application, acting as the <b>RMI interface</b>
 * for security-related operations. It delegates every request to
 * {@link AuthService} and returns standardised {@link ResponseEntity} responses.
 *
 * <h2>Base URL</h2>
 * All endpoints are mapped under <code>/auth</code>.
 *
 * <h2>Token Model</h2>
 * The application uses UUID-based tokens (not signed JWTs) stored in an
 * in-memory map inside {@link AuthService}. A token is considered <em>valid</em>
 * as long as it exists in that map. Logging out removes the token from the map,
 * immediately invalidating it for all subsequent requests.
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li><b>Login</b>  – validate credentials and issue a session token.</li>
 *   <li><b>Logout</b> – invalidate an active session token.</li>
 *   <li><b>Check</b>  – verify whether a token is still active.</li>
 * </ul>
 *
 * <h2>Security Note</h2>
 * Passwords are currently compared in plain text inside {@link AuthService}.
 * All other controllers rely on tokens issued here for authorisation; an invalid
 * or missing token will cause those endpoints to return {@code 401 Unauthorized}.
 *
 * @author BSPQ26-E1 Team
 * @version 1.1
 * @since 2026-05-01
 * @see AuthService
 * @see CredentialsDTO
 */
@Tag(
    name = "Authentication",
    description = "Endpoints for managing user access, token validation, and logout operations"
)
@RestController
@RequestMapping("/auth")
public class AuthController {

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    /**
     * Service layer responsible for all authentication business logic:
     * credential validation, token generation, token storage and invalidation.
     */
    private final AuthService authService;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs an {@code AuthController} with its required service dependency.
     *
     * <p>Spring Boot will inject {@link AuthService} automatically via constructor
     * injection when the application context is initialised.</p>
     *
     * @param authService the service handling login, logout, and token validation;
     *                    must not be {@code null}
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * Authenticates a user and issues a session token.
     *
     * <p>Validates the supplied {@code email} and {@code password} against the
     * persisted user record. On success, a UUID token is generated, stored in the
     * in-memory active-token map ({@link AuthService}), and returned to the caller.
     * This token must be included in every subsequent authenticated API request.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /auth/login</pre>
     *
     * <h3>Request Body – {@link CredentialsDTO}</h3>
     * <ul>
     *   <li>{@code email}    – the registered e-mail address of the user (required)</li>
     *   <li>{@code password} – the user's plain-text password (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * <ul>
     *   <li>On success: a plain-text UUID token string (e.g.
     *       {@code "a3f1c2d4-…"}) to be used as the {@code accessToken} /
     *       {@code token} parameter in all other endpoints.</li>
     *   <li>On failure: the string {@code "Invalid credentials"}.</li>
     * </ul>
     *
     * @param credentials the DTO carrying the user's {@code email} and
     *                    {@code password}
     * @return <ul>
     *   <li>{@code 200 OK} with the UUID token string on successful authentication</li>
     *   <li>{@code 401 Unauthorized} with {@code "Invalid credentials"} if the
     *       e-mail does not exist or the password does not match</li>
     * </ul>
     * @pre  A user with the given {@code email} must be registered in the system.
     * @post A new UUID token is stored in the active-token map and associated
     *       with the authenticated user's e-mail address.
     * @see CredentialsDTO
     * @see AuthService#login(CredentialsDTO)
     */
    @Operation(
        summary = "Log in to the system",
        description = "Evaluates the provided credentials. If the username and password are "
                    + "correct, it generates and returns an access token (JWT) for making "
                    + "authenticated requests.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful login. Returns the access token."),
            @ApiResponse(responseCode = "401", description = "Invalid credentials. Username or password do not match.")
        }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(
        @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Object containing the user's access credentials (e-mail and password)",
            required = true
        )
        @RequestBody CredentialsDTO credentials
    ) {
        String token = authService.login(credentials);

        if (token == null) {
            return new ResponseEntity<>("Invalid credentials", HttpStatus.UNAUTHORIZED);
        }
        return new ResponseEntity<>(token, HttpStatus.OK);
    }

    /**
     * Logs out the authenticated user by invalidating their active session token.
     *
     * <p>The token is removed from the in-memory active-token map in
     * {@link AuthService}. Once invalidated, any subsequent request using the
     * same token will be rejected with {@code 401 Unauthorized} by all protected
     * endpoints.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /auth/logout?token=&lt;uuid&gt;</pre>
     *
     * <h3>Query Parameter</h3>
     * <ul>
     *   <li>{@code token} – the active UUID session token to invalidate (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * This endpoint returns no body. The HTTP status code is the sole indicator
     * of the operation's result.
     *
     * @param token the active UUID session token to be invalidated; must not be
     *              {@code null} or blank
     * @return <ul>
     *   <li>{@code 200 OK} (empty body) if the token was valid and has been
     *       successfully invalidated</li>
     *   <li>{@code 401 Unauthorized} (empty body) if the token was not found in
     *       the active-token map (already invalid, expired, or never issued)</li>
     * </ul>
     * @pre  The supplied token must currently exist in the active-token map.
     * @post The token is removed from the active-token map and can no longer be
     *       used to authenticate any API request.
     * @see AuthService#isValidToken(String)
     * @see AuthService#logout(String)
     */
    @Operation(
        summary = "Log out from the system",
        description = "Allows a user to close their active session. This endpoint invalidates "
                    + "the provided token, preventing it from being used in future requests.",
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

    /**
     * Checks whether a given session token is still valid.
     *
     * <p>This is a lightweight probe endpoint. It does not alter any server
     * state: it simply checks whether the supplied token exists in the
     * in-memory active-token map and returns the result as an HTTP status code
     * together with a human-readable message.</p>
     *
     * <p>Frontend clients should call this endpoint before performing critical
     * or sensitive operations to confirm that the user's session is still active,
     * avoiding unnecessary {@code 401} failures on business endpoints.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>GET /auth/check?token=&lt;uuid&gt;</pre>
     *
     * <h3>Query Parameter</h3>
     * <ul>
     *   <li>{@code token} – the UUID session token to verify (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * <ul>
     *   <li>On valid token:   the plain-text string {@code "Valid token"}.</li>
     *   <li>On invalid token: the plain-text string {@code "Invalid token"}.</li>
     * </ul>
     *
     * @param token the UUID session token to verify; must not be {@code null}
     * @return <ul>
     *   <li>{@code 200 OK} with body {@code "Valid token"} if the token exists
     *       in the active-token map and the session is active</li>
     *   <li>{@code 401 Unauthorized} with body {@code "Invalid token"} if the
     *       token is not found, has been logged out, or was never issued</li>
     * </ul>
     * @see AuthService#isValidToken(String)
     */
    @Operation(
        summary = "Check token validity",
        description = "Verifies the status of a specific token. Useful for frontend clients "
                    + "to check if the user's session is still active before performing "
                    + "critical operations.",
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