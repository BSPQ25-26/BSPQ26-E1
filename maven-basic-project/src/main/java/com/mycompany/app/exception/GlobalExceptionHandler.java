package com.mycompany.app.exception;

import com.mycompany.app.dto.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponseDTO> handleAuthException(AuthException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(ex.getStatus().value(), ex.getMessage());
        return ResponseEntity.status(ex.getStatus()).body(body);
    }

    @ExceptionHandler(HttpClientErrorException.class)
    public ResponseEntity<ErrorResponseDTO> handleHttpClientError(HttpClientErrorException ex) {
        HttpStatus status = HttpStatus.resolve(ex.getStatusCode().value());
        if (status == null) status = HttpStatus.INTERNAL_SERVER_ERROR;
        ErrorResponseDTO body = new ErrorResponseDTO(status.value(), "External service error: " + ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ErrorResponseDTO> handleMissingHeader(MissingRequestHeaderException ex) {
        ErrorResponseDTO body = new ErrorResponseDTO(400, ex.getMessage());
        return ResponseEntity.badRequest().body(body);
    }
}
