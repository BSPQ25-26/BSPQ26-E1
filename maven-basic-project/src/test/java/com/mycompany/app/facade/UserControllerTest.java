package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mycompany.app.dto.UserCreationDTO;
import com.mycompany.app.dto.UserInfoDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.UserService;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserService userService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private UserController userController;

    @Test
    void createUser_WhenServiceReturnsTrue_ReturnsCreated() {
        UserCreationDTO dto = new UserCreationDTO("Ana", "ana@mail.com", "pwd", 0.0);
        when(userService.createUser(dto)).thenReturn(true);

        ResponseEntity<Void> response = userController.createUser(dto);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void createUser_WhenServiceReturnsFalse_ReturnsUnauthorized() {
        UserCreationDTO dto = new UserCreationDTO("Ana", "ana@mail.com", "pwd", 0.0);
        when(userService.createUser(dto)).thenReturn(false);

        ResponseEntity<Void> response = userController.createUser(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getUserInfo_WithInvalidToken_ReturnsUnauthorized() {
        when(authService.isValidToken("bad")).thenReturn(false);

        ResponseEntity<UserInfoDTO> response = userController.getUserInfo("ana@mail.com", "bad");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void getUserInfo_WhenUserNotFound_ReturnsNotFound() {
        when(authService.isValidToken("ok")).thenReturn(true);
        when(userService.getUserInfo("ghost@mail.com")).thenReturn(null);

        ResponseEntity<UserInfoDTO> response = userController.getUserInfo("ghost@mail.com", "ok");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getUserInfo_WhenServiceThrows_ReturnsBadRequest() {
        when(authService.isValidToken("ok")).thenReturn(true);
        when(userService.getUserInfo("ana@mail.com")).thenThrow(new RuntimeException("error"));

        ResponseEntity<UserInfoDTO> response = userController.getUserInfo("ana@mail.com", "ok");

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
