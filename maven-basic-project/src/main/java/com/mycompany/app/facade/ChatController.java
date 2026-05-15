package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @file ChatController.java
 * @brief REST controller for AI-powered expense analysis (RMI facade layer).
 *
 * This controller exposes the remote HTTP API endpoint that integrates with
 * the <b>Google Gemini AI API</b> (via Spring AI) to analyse a user's
 * recorded expenses and provide personalised monthly financial advice.
 * It acts as the <b>RMI interface</b> for AI chat features, delegating
 * the heavy lifting to {@link ChatService} and using {@link AuthService}
 * for token validation.
 *
 * <h2>Base URL</h2>
 * All endpoints are mapped under <code>/chat</code>.
 *
 * <h2>Security</h2>
 * Every endpoint validates the caller's token via
 * {@link AuthService#isValidToken(String)} before invoking any AI logic.
 * Requests with a missing or invalid token are rejected immediately with
 * {@code 401 Unauthorized}.
 *
 * <h2>AI Integration</h2>
 * The underlying {@link ChatService} performs the following steps:
 * <ol>
 *   <li>Loads all {@code GASTO}-type transactions created by the user.</li>
 *   <li>Groups them by category name, summing the total amounts.</li>
 *   <li>Sends the grouped summary to the Gemini model with a fixed system
 *       prompt requesting a JSON response containing a {@code general_analysis}
 *       string and a {@code tips} array of three savings tips.</li>
 *   <li>Returns the raw JSON string to this controller, which passes it
 *       directly to the caller.</li>
 * </ol>
 *
 * <h2>Response Format</h2>
 * On success the response body is a plain JSON string (not wrapped in an
 * additional JSON envelope). Example:
 * <pre>
 * {
 *   "general_analysis": "Your spending is heavily concentrated on dining out.",
 *   "tips": [
 *     "Cook at home at least 4 days a week.",
 *     "Set a weekly dining budget of €30.",
 *     "Use cashback apps when grocery shopping."
 *   ]
 * }
 * </pre>
 *
 * @author BSPQ26-E1 Team
 * @version 1.1
 * @since 2026-05-01
 * @see ChatService
 * @see AuthService
 */
@Tag(
    name = "Chat / AI Advisor",
    description = "Endpoint for AI-powered financial analysis of a user's expenses "
                + "using the Google Gemini API"
)
@RestController
@RequestMapping("/chat")
public class ChatController {

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    /**
     * Service layer that compiles the user's expense data, builds the AI
     * prompt, calls the Gemini model via Spring AI, and returns the
     * generated advice as a JSON string.
     */
    private final ChatService chatService;

    /**
     * Service layer used exclusively for token validation. Each endpoint calls
     * {@link AuthService#isValidToken(String)} before delegating to
     * {@link ChatService}.
     */
    private final AuthService authService;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a {@code ChatController} with its required service dependencies.
     *
     * <p>Spring Boot will inject both services automatically via constructor
     * injection when the application context is initialised.</p>
     *
     * @param chatService the service that performs AI expense analysis via the
     *                    Gemini API; must not be {@code null}
     * @param authService the service used to validate session tokens;
     *                    must not be {@code null}
     */
    public ChatController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * Generates AI-powered monthly financial advice for a specific user.
     *
     * <p>The endpoint retrieves all {@code GASTO}-type transactions created
     * by the given user, groups them by category and sums their totals, then
     * submits this summary to the Google Gemini model. The model returns a
     * JSON object with a short {@code general_analysis} and three actionable
     * {@code tips} — which is forwarded verbatim to the caller.</p>
     *
     * <p>If the user has no recorded expenses, the AI receives a notice that
     * no expenses exist and will respond accordingly.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>GET /chat/monthly-advice/{userId}?token=&lt;uuid&gt;</pre>
     *
     * <h3>Path Variable</h3>
     * <ul>
     *   <li>{@code userId} – the integer ID of the user to analyse (required)</li>
     * </ul>
     *
     * <h3>Query Parameter</h3>
     * <ul>
     *   <li>{@code token} – active session token for authentication (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * On success, a plain JSON string produced by the Gemini model containing:
     * <ul>
     *   <li>{@code general_analysis} – one sentence summarising the user's
     *       spending pattern.</li>
     *   <li>{@code tips} – array of three specific savings recommendations.</li>
     * </ul>
     * No additional envelope is applied; the model's output is returned as-is.
     *
     * @param userId the unique integer identifier of the user whose expenses
     *               are to be analysed by the AI; must correspond to an existing
     *               user in the system
     * @param token  an active session token to authenticate the request;
     *               must not be {@code null} or blank
     * @return <ul>
     *   <li>{@code 200 OK} with the Gemini-generated JSON advice string if the
     *       token is valid and the AI call succeeds</li>
     *   <li>{@code 401 Unauthorized} (empty body) if the token is invalid,
     *       expired, or was not provided</li>
     * </ul>
     * @see ChatService#analyzeExpenses(int)
     * @see AuthService#isValidToken(String)
     */
    @Operation(
        summary = "Generate AI-powered monthly financial advice for a user",
        description = "Uses the Google Gemini API to analyse the user's recorded expenses, "
                    + "grouped by category, and returns personalised savings tips and a "
                    + "spending analysis as a JSON string.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful. Returns a JSON string with 'general_analysis' and 'tips' fields."),
            @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid, missing, or expired.")
        }
    )
    @GetMapping("/monthly-advice/{userId}")
    public ResponseEntity<String> getMonthlyAdvice(
            @Parameter(description = "The unique identifier of the user whose expenses will be analysed", required = true)
            @PathVariable int userId,

            @Parameter(description = "A valid session token to authenticate the request", required = true)
            @RequestParam String token
    ) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(chatService.analyzeExpenses(userId));
    }
}