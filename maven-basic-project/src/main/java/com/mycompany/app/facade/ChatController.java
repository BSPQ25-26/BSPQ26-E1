package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.ChatService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/chat")
public class ChatController {

    private final ChatService chatService;
    private final AuthService authService;

    public ChatController(ChatService chatService, AuthService authService) {
        this.chatService = chatService;
        this.authService = authService;
    }

    @Operation(
        summary = "Use AI to analyze the expenses of a user",
        description = "Uses the gemini API to analyze the expenses of the user, along with the categories",
        responses = {
            @ApiResponse(responseCode = "200", description = "Successful. Returns the string with the response."),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
        }
    )
    @GetMapping("/monthly-advice/{userId}")
    public ResponseEntity<String> getMonthlyAdvice(@PathVariable int userId, String token) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(chatService.analyzeExpenses(userId));
    }

@GetMapping("/ask")
    public ResponseEntity<String> askAi(@RequestParam String message, @CookieValue(value = "token", required = false) String token) {
        if (token == null || !authService.isValidToken(token)) {
            return new ResponseEntity<>("Please, log in to use the chat.", HttpStatus.UNAUTHORIZED);
        }

        try {
            String response = chatService.chatLibre(message);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); 
            return new ResponseEntity<>("The AI servers are currently overloaded. Please try again in a few minutes.", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}