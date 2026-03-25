package com.mycompany.app.service;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.dto.UserInfoDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;


@Service
public class UserService {
    private final UsuarioRepository usuarioRepository;

    public UserService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public boolean createUser(UserCreationDTO userCreationDTO){
        try {
            if (this.usuarioRepository.findByEmail(userCreationDTO.getEmail()) != null){
                return false;
            }
            Usuario usuario =  new Usuario(
                    userCreationDTO.getUsername(),
                    userCreationDTO.getEmail(),
                    userCreationDTO.getPassword(),
                    userCreationDTO.getBalance());

            usuarioRepository.save(usuario);

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public UserInfoDTO getUserInfo(String email){
        Usuario user = usuarioRepository.findByEmail(email);
        UserInfoDTO userInfoDTO = new UserInfoDTO(user.getNombre(), user.getEmail(), user.getBalance());

        return userInfoDTO;
    }
}
