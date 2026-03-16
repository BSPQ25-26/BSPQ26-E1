package com.mycompany.app.service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.CredentialsDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;

@Service
public class AuthService {
    private final UsuarioRepository usuarioRepository;

    //Un token hardcodeado para no tener que estar haciendo login para comprobar cosas
    private final Map<String, String> activeTokens = new HashMap<>(Map.of(
        "1", "pablo@gmail.com"
    ));

    public AuthService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public String login(CredentialsDTO credentialsDTO){
        Usuario usuario = usuarioRepository.findByEmail(credentialsDTO.getEmail());

        if (usuario != null && usuario.getContraseña().equals(credentialsDTO.getPassword())) {
            String token = UUID.randomUUID().toString();
            activeTokens.put(token, usuario.getEmail());
            return token;
        }
        return null;
    }

    public void logout(String token) {
        activeTokens.remove(token);
    }

    public boolean isValidToken(String token){
        return activeTokens.containsKey(token);
    }
}
