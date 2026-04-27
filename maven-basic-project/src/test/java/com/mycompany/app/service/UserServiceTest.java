package com.mycompany.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.dto.UserInfoDTO;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_WhenEmailDoesNotExist_ReturnsTrue() {
        UserCreationDTO dto = new UserCreationDTO("name", "mail@test.com", "pwd", 0.0);
        when(usuarioRepository.findByEmail("mail@test.com")).thenReturn(null);

        boolean result = userService.createUser(dto);

        assertTrue(result);
    }

    @Test
    void createUser_WhenEmailAlreadyExists_ReturnsFalse() {
        UserCreationDTO dto = new UserCreationDTO("name", "mail@test.com", "pwd", 0.0);
        when(usuarioRepository.findByEmail("mail@test.com")).thenReturn(new Usuario());

        boolean result = userService.createUser(dto);

        assertFalse(result);
    }

    @Test
    void createUser_WhenRepositoryThrowsException_ReturnsFalse() {
        UserCreationDTO dto = new UserCreationDTO("name", "mail@test.com", "pwd", 0.0);
        when(usuarioRepository.findByEmail("mail@test.com")).thenReturn(null);
        when(usuarioRepository.save(any(Usuario.class))).thenThrow(new RuntimeException("db error"));

        boolean result = userService.createUser(dto);

        assertFalse(result);
    }

    @Test
    void getUserInfo_WhenUserExists_ReturnsDto() {
        Usuario user = new Usuario("Pepe", "pepe@mail.com", "pwd", 75.5);
        when(usuarioRepository.findByEmail("pepe@mail.com")).thenReturn(user);

        UserInfoDTO info = userService.getUserInfo("pepe@mail.com");

        assertEquals("Pepe", info.getUsername());
        assertEquals("pepe@mail.com", info.getEmail());
        assertEquals(75.5, info.getBalance());
    }

    @Test
    void getUserInfo_WhenUserDoesNotExist_ThrowsException() {
        when(usuarioRepository.findByEmail("ghost@mail.com")).thenReturn(null);

        assertThrows(NullPointerException.class, () -> userService.getUserInfo("ghost@mail.com"));
    }
}
