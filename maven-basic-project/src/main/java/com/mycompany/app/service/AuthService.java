package com.mycompany.app.service;

import com.mycompany.app.dto.AuthResponseDTO;
import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.dto.SupabaseAuthResponseDTO;
import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.exception.AuthException;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon-key}")
    private String supabaseAnonKey;

    private final RestTemplate restTemplate;
    private final UsuarioRepository usuarioRepository;

    public AuthService(RestTemplate restTemplate, UsuarioRepository usuarioRepository) {
        this.restTemplate      = restTemplate;
        this.usuarioRepository = usuarioRepository;
    }

    public AuthResponseDTO login(CredentialsDTO credentials) {
        String url = supabaseUrl + "/auth/v1/token?grant_type=password";

        HttpHeaders headers = buildApiKeyHeaders();
        Map<String, String> body = new HashMap<>();
        body.put("email",    credentials.getEmail());
        body.put("password", credentials.getPassword());

        SupabaseAuthResponseDTO supabaseResp;
        try {
            ResponseEntity<SupabaseAuthResponseDTO> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), SupabaseAuthResponseDTO.class);
            supabaseResp = response.getBody();
        } catch (HttpClientErrorException ex) {
            throw new AuthException("Invalid email or password", HttpStatus.UNAUTHORIZED);
        }

        if (supabaseResp == null || supabaseResp.getAccessToken() == null) {
            throw new AuthException("Unexpected response from auth provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        Usuario usuario = usuarioRepository.findByEmail(credentials.getEmail())
            .orElseThrow(() -> new AuthException("User account not found in system", HttpStatus.NOT_FOUND));

        return new AuthResponseDTO(
            supabaseResp.getAccessToken(),
            supabaseResp.getTokenType(),
            supabaseResp.getExpiresIn(),
            usuario.getId(),
            usuario.getNombre()
        );
    }

    public AuthResponseDTO create(UserCreationDTO dto) {
        String url = supabaseUrl + "/auth/v1/signup";

        HttpHeaders headers = buildApiKeyHeaders();
        Map<String, String> body = new HashMap<>();
        body.put("email",    dto.getEmail());
        body.put("password", dto.getPassword());

        SupabaseAuthResponseDTO supabaseResp;
        try {
            ResponseEntity<SupabaseAuthResponseDTO> response =
                restTemplate.postForEntity(url, new HttpEntity<>(body, headers), SupabaseAuthResponseDTO.class);
            supabaseResp = response.getBody();
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNPROCESSABLE_ENTITY) {
                throw new AuthException("Email is already registered", HttpStatus.CONFLICT);
            }
            throw new AuthException("Account creation failed: " + ex.getMessage(), HttpStatus.BAD_REQUEST);
        }

        if (supabaseResp == null) {
            throw new AuthException("No response from auth provider", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        // Always persist the usuario row (idempotent: reuse existing if already there)
        Usuario usuario = usuarioRepository.findByEmail(dto.getEmail())
            .orElseGet(() -> {
                Usuario u = new Usuario();
                u.setNombre(dto.getUsername());
                u.setEmail(dto.getEmail());
                u.setBalance(0.0);
                return usuarioRepository.save(u);
            });

        if (supabaseResp.getAccessToken() == null) {
            throw new AuthException(
                "Account created but email confirmation is required before login",
                HttpStatus.ACCEPTED
            );
        }

        return new AuthResponseDTO(
            supabaseResp.getAccessToken(),
            supabaseResp.getTokenType(),
            supabaseResp.getExpiresIn(),
            usuario.getId(),
            usuario.getNombre()
        );
    }

    public void logout(String accessToken) {
        String url = supabaseUrl + "/auth/v1/logout";

        HttpHeaders headers = buildApiKeyHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        try {
            restTemplate.postForEntity(url, new HttpEntity<>("{}", headers), Void.class);
        } catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return;
            }
            throw new AuthException("Logout failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private HttpHeaders buildApiKeyHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseAnonKey);
        return headers;
    }
}
