package com.mycompany.app.facade;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.TransactionCreationDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

@RestController
@RequestMapping("/transaction")
public class TransactionController {
    //private final TransactionService transactionService;

    //public TransactionController(TransactionService transactionService) {
    public TransactionController() {
        //this.transactionService = transactionService;
    }

    @Operation(
            summary = "Create transaction",
            description = "Create a new transation",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: transaction creation completed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createTransaction(@RequestBody TransactionCreationDTO transactionCreationDTO) {
        return new ResponseEntity<>("token", HttpStatus.OK);
  
    }

}
