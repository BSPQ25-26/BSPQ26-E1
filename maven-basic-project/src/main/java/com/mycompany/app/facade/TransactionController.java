package com.mycompany.app.facade;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.PayDebtDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @Operation(summary = "Create transaction", description = "Create a new transation", responses = {
            @ApiResponse(responseCode = "200", description = "OK: transaction creation completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
    })
    @PostMapping("/create")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionCreationDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.createTransaction(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

    }

    @Operation(summary = "Delete transaction", description = "Deleting a new transation", responses = {
            @ApiResponse(responseCode = "200", description = "OK: transaction deletion completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
    })
    @PostMapping("/delete")
    public ResponseEntity<String> deleteTransaction(@RequestBody TransactionDeletionDTO request) {
        if (!authService.isValidToken(request.getAccessToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.deleteTransaction(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Edit transaction", description = "Editing a new transation", responses = {
            @ApiResponse(responseCode = "200", description = "OK: transaction edition completed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
    })
    @PostMapping("/edit/{transactionId}")
    public ResponseEntity<String> editTransaction(@RequestBody TransactionEditionDTO request,
            @PathVariable("transactionId") Integer transactionId) {
        if (!authService.isValidToken(request.getAccessToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.editTransaction(request, transactionId);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(
        summary = "Create debt",
        description = "Create a new pending debt between two users",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK: debt created successfully"),
                @ApiResponse(responseCode = "400", description = "Bad Request: invalid data or users not found"),
                @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
        }
    )
    @PostMapping("/crear")
    public ResponseEntity<String> createDeuda(@RequestBody DeudaCreationDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.createDeuda(request);
        if (result) { return new ResponseEntity<>(HttpStatus.OK); }
        else { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }
    }

   @Operation(
        summary = "Pay debt",
        description = "Mark a pending debt as paid",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK: debt paid successfully"),
                @ApiResponse(responseCode = "400", description = "Bad Request: debt not found or already paid"),
                @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
        }
    )
    @PostMapping("/pay/{deudaId}")
    public ResponseEntity<String> pagarDeuda(@RequestBody PayDebtDTO request,
            @PathVariable("deudaId") Integer deudaId) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.pagarDeuda(deudaId);
        if (result) { return new ResponseEntity<>(HttpStatus.OK); }
        else { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }
    }
}
