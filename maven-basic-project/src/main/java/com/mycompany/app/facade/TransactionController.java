package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.DeudaCreationDTO;
import com.mycompany.app.dto.PayDebtDTO;
import com.mycompany.app.dto.TransactionCreationDTO;
import com.mycompany.app.dto.TransactionDeletionDTO;
import com.mycompany.app.dto.TransactionEditionDTO;
import com.mycompany.app.dto.TranscactionDebtEditionDTO;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.TransactionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Transactions", description = "Endpoints for managing financial or system transactions, including creation, modification, and deletion operations")
@RestController
@RequestMapping("/transaction")
public class TransactionController {
    
    private final TransactionService transactionService;
    private final AuthService authService;

    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    @Operation(
            summary = "Create a new transaction",
            description = "Records a new transaction in the system based on the provided payload. The request must include a valid authorization token to verify user permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction successfully created."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The transaction could not be created due to invalid data or missing fields."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )

    @PostMapping("/create")
        public ResponseEntity<String> createTransaction(@RequestBody TransactionCreationDTO request) {
            if (!authService.isValidToken(request.getToken())) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            try {
                Boolean result = transactionService.createTransaction(request);
                if (result) {
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } catch (RuntimeException e) {
                return new ResponseEntity<>(e.getMessage(), HttpStatus.CONFLICT);
            }
        }

    @Operation(
            summary = "Delete a transaction",
            description = "Permanently removes an existing transaction from the system. Requires an active authorization token to verify that the user has the necessary permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction successfully deleted."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The transaction could not be deleted (e.g., it does not exist or the payload is invalid)."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/delete")
    public ResponseEntity<String> deleteTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the target transaction ID and access token", required = true)
            @RequestBody TransactionDeletionDTO request
    ){
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

    @Operation(
            summary = "Edit an existing transaction",
            description = "Updates the details of an existing transaction identified by its unique path ID. The request body must contain the updated fields and a valid access token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction successfully updated."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The transaction could not be updated due to invalid data or business rule violations."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/edit/{transactionId}")
    public ResponseEntity<String> editTransaction(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the updated transaction data and access token", required = true)
            @RequestBody TransactionEditionDTO request,
            
            @Parameter(description = "The unique identifier of the transaction to be modified", required = true)
            @PathVariable("transactionId") Integer transactionId
        ){
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

    @Operation(
        summary = "Edit transaction and its debts",
        description = "Edit a transaction and all its associated debts. Fails if any debt is already paid.",
        responses = {
                @ApiResponse(responseCode = "200", description = "OK: transaction and debts edited successfully"),
                @ApiResponse(responseCode = "400", description = "Bad Request: transaction not found or a debt is already paid"),
                @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
        }
    )
    @PostMapping("/edit-with-deudas/{transactionId}")
    public ResponseEntity<String> editTransactionWithDeudas(@RequestBody TranscactionDebtEditionDTO request,
            @PathVariable("transactionId") Integer transactionId) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = transactionService.editTransactionWithDeudas(request, transactionId);
        if (result) { return new ResponseEntity<>(HttpStatus.OK); }
        else { return new ResponseEntity<>(HttpStatus.BAD_REQUEST); }
    }

    @Operation(summary = "Set category budget limit", description = "Sets a maximum monthly budget limit for a specific category.")
    @PostMapping("/budget/create")
    public ResponseEntity<String> createBudget(@RequestBody com.mycompany.app.dto.BudgetCreationDTO request) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        boolean result = transactionService.createBudget(request);
        if (result) {
            return new ResponseEntity<>("Budget created successfully", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Error creating budget", HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Get Net Balance", description = "Calculates the net balance (income - expenses) for a user within a specific date range.")
    @GetMapping("/net-balance")
    public ResponseEntity<?> getNetBalance(
            @RequestParam("userId") Integer userId,
            @RequestParam("startDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime startDate,
            @RequestParam("endDate") @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime endDate,
            @RequestParam("token") String token) {
        
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        try {
            Double balance = transactionService.getNetBalance(userId, startDate, endDate);
            return new ResponseEntity<>(balance, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Error calculating net balance", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}