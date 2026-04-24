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

import com.mycompany.app.dto.PayDebtDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.TransactionService;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock
    private TransactionService transactionService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private TransactionController transactionController;

    @Test
    void createTransaction_WithInvalidToken_ReturnsUnauthorized() {
        TransactionCreationDTO dto = new TransactionCreationDTO();
        dto.setToken("bad");

        when(authService.isValidToken("bad")).thenReturn(false);

        ResponseEntity<String> response = transactionController.createTransaction(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createTransaction_WithValidTokenAndServiceTrue_ReturnsOk() {
        TransactionCreationDTO dto = new TransactionCreationDTO();
        dto.setToken("ok");

        when(authService.isValidToken("ok")).thenReturn(true);
        when(transactionService.createTransaction(dto)).thenReturn(true);

        ResponseEntity<String> response = transactionController.createTransaction(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void deleteTransaction_WhenServiceFalse_ReturnsBadRequest() {
        TransactionDeletionDTO dto = new TransactionDeletionDTO("ok", 1);

        when(authService.isValidToken("ok")).thenReturn(true);
        when(transactionService.deleteTransaction(dto)).thenReturn(false);

        ResponseEntity<String> response = transactionController.deleteTransaction(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    @Test
    void editTransaction_WithInvalidToken_ReturnsUnauthorized() {
        TransactionEditionDTO dto = new TransactionEditionDTO();
        dto.setAccessToken("bad");

        when(authService.isValidToken("bad")).thenReturn(false);

        ResponseEntity<String> response = transactionController.editTransaction(dto, 2);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void pagarDeuda_WithInvalidToken_ReturnsUnauthorized() {
        PayDebtDTO dto = new PayDebtDTO();
        dto.setToken("bad");

        when(authService.isValidToken("bad")).thenReturn(false);

        ResponseEntity<String> response = transactionController.pagarDeuda(dto, 5);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
}
