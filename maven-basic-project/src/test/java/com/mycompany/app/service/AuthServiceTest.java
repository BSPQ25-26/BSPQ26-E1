package com.mycompany.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithValidCredentials_ReturnsTokenAndTokenBecomesValid() {
        CredentialsDTO credentials = new CredentialsDTO("user@mail.com", "secret");
        Usuario user = new Usuario("user", "user@mail.com", "secret", 100.0);

        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(user);

        String token = authService.login(credentials);

        assertNotNull(token);
        assertTrue(authService.isValidToken(token));
        assertEquals("user@mail.com", authService.getEmailFromToken(token));
    }

    @Test
    void login_WithWrongPassword_ReturnsNull() {
        CredentialsDTO credentials = new CredentialsDTO("user@mail.com", "wrong");
        Usuario user = new Usuario("user", "user@mail.com", "secret", 100.0);

        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(user);

        String token = authService.login(credentials);

        assertNull(token);
    }

    @Test
    void logout_RemovesExistingToken() {
        CredentialsDTO credentials = new CredentialsDTO("user@mail.com", "secret");
        Usuario user = new Usuario("user", "user@mail.com", "secret", 100.0);
        when(usuarioRepository.findByEmail("user@mail.com")).thenReturn(user);

        String token = authService.login(credentials);
        assertTrue(authService.isValidToken(token));

        authService.logout(token);

        assertFalse(authService.isValidToken(token));
        assertNull(authService.getEmailFromToken(token));
    }

    @Test
    void isValidToken_WithUnknownToken_ReturnsFalse() {
        assertFalse(authService.isValidToken("unknown-token"));
    }
}
