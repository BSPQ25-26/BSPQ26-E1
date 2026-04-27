package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.service.AuthService;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void login_WithValidCredentials_ReturnsOkAndToken() {
        CredentialsDTO dto = new CredentialsDTO("user@mail.com", "pwd");
        when(authService.login(dto)).thenReturn("token-123");

        ResponseEntity<String> response = authController.login(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("token-123", response.getBody());
    }

    @Test
    void login_WithInvalidCredentials_ReturnsUnauthorized() {
        CredentialsDTO dto = new CredentialsDTO("user@mail.com", "wrong");
        when(authService.login(dto)).thenReturn(null);

        ResponseEntity<String> response = authController.login(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid credentials", response.getBody());
    }

    @Test
    void logout_WithValidToken_ReturnsOk() {
        when(authService.isValidToken("valid")).thenReturn(true);

        ResponseEntity<Void> response = authController.logout("valid");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(authService).logout("valid");
    }

    @Test
    void logout_WithInvalidToken_ReturnsUnauthorized() {
        when(authService.isValidToken("invalid")).thenReturn(false);

        ResponseEntity<Void> response = authController.logout("invalid");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void check_WithInvalidToken_ReturnsUnauthorizedMessage() {
        when(authService.isValidToken("invalid")).thenReturn(false);

        ResponseEntity<String> response = authController.check("invalid");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid token", response.getBody());
    }
}
