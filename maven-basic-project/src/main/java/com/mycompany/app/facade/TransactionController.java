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

/**
 * @file TransactionController.java
 * @brief REST controller for managing financial transactions and debts.
 *
 * This controller constitutes the <b>Remote Method Invocation (RMI) interface</b>
 * of the finance application's transaction subsystem. It is mapped to the
 * base path {@code /transaction} and exposes the following operations:
 *
 * <ul>
 *   <li><b>Transaction CRUD</b> &ndash; create, edit, and delete financial
 *       transactions of types {@code INGRESO} (income), {@code GASTO} (expense),
 *       and {@code LIQUIDACION} (debt settlement).</li>
 *   <li><b>Debt management</b> &ndash; create pending debts between users and
 *       mark them as paid, which internally generates settlement transactions.</li>
 *   <li><b>Composite editing</b> &ndash; atomically edit a transaction together
 *       with all of its associated debts in a single request.</li>
 *   <li><b>Budget control</b> &ndash; set monthly spending limits per category;
 *       the system raises a warning (HTTP 409) when a new expense would exceed
 *       the configured limit.</li>
 *   <li><b>Balance queries</b> &ndash; compute the net balance (income minus
 *       expenses) for a given user within a date range.</li>
 * </ul>
 *
 * <h2>Security model</h2>
 * Every endpoint requires a valid <b>JWT authorization token</b>, which is
 * verified through {@link AuthService#isValidToken(String)}. Requests with
 * an invalid or expired token receive an HTTP&nbsp;401 (Unauthorized) response.
 *
 * <h2>Error handling</h2>
 * <ul>
 *   <li>HTTP 200 &ndash; operation succeeded.</li>
 *   <li>HTTP 400 &ndash; invalid input data or business-rule violation.</li>
 *   <li>HTTP 401 &ndash; authentication failure (invalid/expired JWT).</li>
 *   <li>HTTP 409 &ndash; budget limit exceeded (only for {@code createTransaction}).</li>
 *   <li>HTTP 500 &ndash; unexpected server-side error (only for {@code getNetBalance}).</li>
 * </ul>
 *
 * @author  BSPQ26-E1 Team
 * @version 2.0
 * @since   2026-05-01
 *
 * @see TransactionService   Business logic layer for transactions and debts
 * @see AuthService           JWT token validation service
 * @see TransactionCreationDTO
 * @see TransactionDeletionDTO
 * @see TransactionEditionDTO
 * @see DeudaCreationDTO
 * @see PayDebtDTO
 * @see TranscactionDebtEditionDTO
 * @see com.mycompany.app.dto.BudgetCreationDTO
 */
@Tag(name = "Transactions", description = "Endpoints for managing financial or system transactions, including creation, modification, and deletion operations")
@RestController
@RequestMapping("/transaction")
public class TransactionController {

    /**
     * Service layer responsible for all transaction and debt business logic,
     * including CRUD operations, budget checks, and balance calculations.
     *
     * @see TransactionService
     */
    private final TransactionService transactionService;

    /**
     * Service layer responsible for JWT token validation.
     * Called at the beginning of every endpoint to enforce authentication.
     *
     * @see AuthService
     */
    private final AuthService authService;

    /**
     * Constructs a new {@code TransactionController} with the required service
     * dependencies.  Spring injects both beans automatically via constructor
     * injection.
     *
     * @param transactionService the service handling transaction CRUD, debt
     *                           management, budget enforcement, and balance
     *                           calculation &ndash; must not be {@code null}
     * @param authService        the service handling JWT token validation
     *                           &ndash; must not be {@code null}
     */
    public TransactionController(TransactionService transactionService, AuthService authService) {
        this.transactionService = transactionService;
        this.authService = authService;
    }

    /**
     * @brief Creates a new financial transaction.
     *
     * <b>Endpoint:</b> {@code POST /transaction/create}
     *
     * Records a transaction of one of the following types:
     * <ul>
     *   <li>{@code INGRESO} &ndash; income; increases the creator's balance.</li>
     *   <li>{@code GASTO}   &ndash; expense; decreases the creator's balance.
     *       If the expense is associated with a category that has a budget,
     *       the monthly cumulative spending is checked against the limit.
     *       Exceeding the limit causes an HTTP&nbsp;409 response with a
     *       warning message.</li>
     *   <li>{@code LIQUIDACION} &ndash; debt settlement; decreases the
     *       creator's balance.</li>
     * </ul>
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "concepto":         "Groceries",
     *   "importeTotal":     45.50,
     *   "tipoTransaccion":  "GASTO",
     *   "categoriaId":      3,
     *   "grupoId":          1,
     *   "creadorId":        7,
     *   "token":            "eyJhbGciOiJIUzI1NiIs..."
     * }
     * }</pre>
     *
     * @param request the {@link TransactionCreationDTO} containing:
     *        <ul>
     *          <li>{@code concepto}         &ndash; short description of the transaction (required)</li>
     *          <li>{@code importeTotal}     &ndash; monetary amount in euros (required, positive)</li>
     *          <li>{@code tipoTransaccion}  &ndash; one of {@code INGRESO}, {@code GASTO}, {@code LIQUIDACION} (required)</li>
     *          <li>{@code categoriaId}      &ndash; FK to the category (optional, nullable)</li>
     *          <li>{@code grupoId}          &ndash; FK to the group (optional, nullable)</li>
     *          <li>{@code creadorId}        &ndash; FK to the user who creates the transaction (required)</li>
     *          <li>{@code token}            &ndash; JWT authorization token (required)</li>
     *        </ul>
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; transaction created successfully</li>
     *           <li>HTTP 400 (Bad Request) &ndash; invalid data or creator not found</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *           <li>HTTP 409 (Conflict) &ndash; monthly budget limit exceeded for the category</li>
     *         </ul>
     *
     * @pre  The {@code creadorId} must reference an existing user.
     * @post On success, the transaction is persisted and the creator's balance
     *       is updated according to the transaction type.
     *
     * @throws RuntimeException propagated as HTTP 409 when the budget limit is exceeded
     *
     * @see TransactionCreationDTO
     * @see TransactionService#createTransaction(TransactionCreationDTO)
     */
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

    /**
     * @brief Permanently deletes an existing transaction.
     *
     * <b>Endpoint:</b> {@code POST /transaction/delete}
     *
     * Removes the specified transaction from the database. This operation
     * is irreversible. Note that this endpoint does <em>not</em> revert
     * any balance changes that were applied when the transaction was created.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "transactionId": 42,
     *   "accessToken":   "eyJhbGciOiJIUzI1NiIs..."
     * }
     * }</pre>
     *
     * @param request the {@link TransactionDeletionDTO} containing:
     *        <ul>
     *          <li>{@code transactionId} &ndash; the unique ID of the transaction to delete (required)</li>
     *          <li>{@code accessToken}   &ndash; JWT authorization token (required)</li>
     *        </ul>
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; transaction deleted successfully</li>
     *           <li>HTTP 400 (Bad Request) &ndash; transaction not found or deletion failed</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  The transaction with the given ID must exist in the database.
     * @post The transaction is permanently removed from the persistence layer.
     *
     * @see TransactionDeletionDTO
     * @see TransactionService#deleteTransaction(TransactionDeletionDTO)
     */
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

    /**
     * @brief Edits an existing transaction identified by its ID.
     *
     * <b>Endpoint:</b> {@code POST /transaction/edit/{transactionId}}
     *
     * Updates the transaction's concepto, importeTotal, tipoTransaccion,
     * category, group, and creator based on the provided DTO fields.
     * All fields in the DTO overwrite the existing values; pass the
     * current values for fields that should remain unchanged.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "accessToken":     "eyJhbGciOiJIUzI1NiIs...",
     *   "concepto":        "Updated groceries",
     *   "importeTotal":    55.00,
     *   "tipoTransaccion": "GASTO",
     *   "categoriaId":     3,
     *   "grupoId":         1,
     *   "creadorId":       7
     * }
     * }</pre>
     *
     * @param request       the {@link TransactionEditionDTO} containing:
     *        <ul>
     *          <li>{@code accessToken}     &ndash; JWT authorization token (required)</li>
     *          <li>{@code concepto}        &ndash; updated description (required)</li>
     *          <li>{@code importeTotal}    &ndash; updated amount in euros (required)</li>
     *          <li>{@code tipoTransaccion} &ndash; updated type: {@code INGRESO}, {@code GASTO}, or {@code LIQUIDACION} (required)</li>
     *          <li>{@code categoriaId}     &ndash; updated category FK (nullable)</li>
     *          <li>{@code grupoId}         &ndash; updated group FK (nullable)</li>
     *          <li>{@code creadorId}       &ndash; updated creator FK (nullable)</li>
     *        </ul>
     * @param transactionId the unique database identifier of the transaction to modify
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; transaction updated successfully</li>
     *           <li>HTTP 400 (Bad Request) &ndash; transaction not found or invalid data</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  A transaction with the given {@code transactionId} must exist.
     * @post The transaction record is updated in-place; no new record is created.
     *
     * @see TransactionEditionDTO
     * @see TransactionService#editTransaction(TransactionEditionDTO, Integer)
     */
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

    /**
     * @brief Creates a new debt (deuda) between two users linked to a transaction.
     *
     * <b>Endpoint:</b> {@code POST /transaction/crear}
     *
     * The debt is created in {@code PENDIENTE} (pending) state. The debtor
     * owes the creditor the specified amount for a given original transaction.
     * The debt can later be settled via {@link #pagarDeuda(PayDebtDTO, Integer)}.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "transaccionId": 42,
     *   "deudorId":      5,
     *   "acreedorId":    7,
     *   "importe":       25.00,
     *   "token":         "eyJhbGciOiJIUzI1NiIs..."
     * }
     * }</pre>
     *
     * @param request the {@link DeudaCreationDTO} containing:
     *        <ul>
     *          <li>{@code transaccionId} &ndash; FK to the original transaction that originates the debt (required)</li>
     *          <li>{@code deudorId}      &ndash; FK to the user who owes the money (required)</li>
     *          <li>{@code acreedorId}    &ndash; FK to the user who is owed the money (required)</li>
     *          <li>{@code importe}       &ndash; the debt amount in euros (required, positive)</li>
     *          <li>{@code token}         &ndash; JWT authorization token (required)</li>
     *        </ul>
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; debt created successfully</li>
     *           <li>HTTP 400 (Bad Request) &ndash; invalid data, transaction/users not found</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  The original transaction, debtor, and creditor must all exist.
     * @post A new {@code Deuda} entity with state {@code PENDIENTE} is persisted.
     *
     * @see DeudaCreationDTO
     * @see TransactionService#createDeuda(DeudaCreationDTO)
     * @see #pagarDeuda(PayDebtDTO, Integer)
     */
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

    /**
     * @brief Marks a pending debt as paid.
     *
     * <b>Endpoint:</b> {@code POST /transaction/pay/{deudaId}}
     *
     * This operation performs three atomic steps:
     * <ol>
     *   <li>Creates a {@code LIQUIDACION} (settlement) transaction for the
     *       debtor, decreasing their balance by the debt amount.</li>
     *   <li>Creates an {@code INGRESO} (income) transaction for the creditor,
     *       increasing their balance by the same amount.</li>
     *   <li>Sets the debt state from {@code PENDIENTE} to {@code PAGADO}.</li>
     * </ol>
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIs..."
     * }
     * }</pre>
     *
     * @param request the {@link PayDebtDTO} containing:
     *        <ul>
     *          <li>{@code token} &ndash; JWT authorization token (required)</li>
     *        </ul>
     * @param deudaId the unique database identifier of the debt to settle
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; debt paid successfully</li>
     *           <li>HTTP 400 (Bad Request) &ndash; debt not found or already in {@code PAGADO} state</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  The debt must exist and be in {@code PENDIENTE} state.
     * @post Two new transactions are persisted, both users' balances are
     *       updated, and the debt state is set to {@code PAGADO}.
     *
     * @see PayDebtDTO
     * @see TransactionService#pagarDeuda(Integer)
     * @see #createDeuda(DeudaCreationDTO)
     */
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

    /**
     * @brief Edits a transaction and all its associated debts atomically.
     *
     * <b>Endpoint:</b> {@code POST /transaction/edit-with-deudas/{transactionId}}
     *
     * Performs a composite, <em>transactional</em> update: either all changes
     * (to the transaction and every listed debt) succeed, or none are applied.
     * The operation is rejected if any associated debt is already in
     * {@code PAGADO} (paid) state.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "token":            "eyJhbGciOiJIUzI1NiIs...",
     *   "concepto":         "Updated team dinner",
     *   "importeTotal":     120.00,
     *   "tipoTransaccion":  "GASTO",
     *   "grupoId":          1,
     *   "categoriaId":      4,
     *   "creadorId":        7,
     *   "deudas": [
     *     { "id": 10, "importe": 40.00, "deudorId": 5, "acreedorId": 7 },
     *     { "id": 11, "importe": 40.00, "deudorId": 6, "acreedorId": 7 }
     *   ]
     * }
     * }</pre>
     *
     * @param request       the {@link TranscactionDebtEditionDTO} containing:
     *        <ul>
     *          <li>{@code token}            &ndash; JWT authorization token (required)</li>
     *          <li>{@code concepto}         &ndash; updated description (required)</li>
     *          <li>{@code importeTotal}     &ndash; updated amount in euros (required)</li>
     *          <li>{@code tipoTransaccion}  &ndash; updated type (required)</li>
     *          <li>{@code grupoId}          &ndash; updated group FK (nullable)</li>
     *          <li>{@code categoriaId}      &ndash; updated category FK (nullable)</li>
     *          <li>{@code creadorId}        &ndash; updated creator FK (nullable)</li>
     *          <li>{@code deudas}           &ndash; list of {@link com.mycompany.app.dto.DebtEditionDTO} objects (nullable)</li>
     *        </ul>
     * @param transactionId the unique database identifier of the transaction to modify
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; transaction and all debts updated</li>
     *           <li>HTTP 400 (Bad Request) &ndash; transaction/debt not found or a debt is already paid</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  No associated debt may be in {@code PAGADO} state.
     * @post The transaction and all listed debts are updated atomically.
     *
     * @see TranscactionDebtEditionDTO
     * @see com.mycompany.app.dto.DebtEditionDTO
     * @see TransactionService#editTransactionWithDeudas(TranscactionDebtEditionDTO, Integer)
     */
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

    /**
     * @brief Sets or updates a monthly budget limit for a specific category.
     *
     * <b>Endpoint:</b> {@code POST /transaction/budget/create}
     *
     * If a budget already exists for the given user&ndash;category pair, it is
     * updated with the new limit. Otherwise a new budget record is created.
     * Future {@code GASTO} transactions that would push the monthly cumulative
     * spending above this limit will trigger an HTTP&nbsp;409 warning in
     * {@link #createTransaction(TransactionCreationDTO)}.
     *
     * <b>JSON request body example:</b>
     * <pre>{@code
     * {
     *   "limitAmount":  200.00,
     *   "categoryId":   3,
     *   "userId":       7,
     *   "token":        "eyJhbGciOiJIUzI1NiIs..."
     * }
     * }</pre>
     *
     * @param request the {@link com.mycompany.app.dto.BudgetCreationDTO} containing:
     *        <ul>
     *          <li>{@code limitAmount} &ndash; the maximum monthly spending limit in euros (required, positive)</li>
     *          <li>{@code categoryId}  &ndash; FK to the category to cap (required)</li>
     *          <li>{@code userId}      &ndash; FK to the user who owns the budget (required)</li>
     *          <li>{@code token}       &ndash; JWT authorization token (required)</li>
     *        </ul>
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) with body {@code "Budget created successfully"}</li>
     *           <li>HTTP 400 (Bad Request) with body {@code "Error creating budget"}</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *         </ul>
     *
     * @pre  The user and category must exist in the database.
     * @post A budget record is created or updated for the user&ndash;category pair.
     *
     * @see com.mycompany.app.dto.BudgetCreationDTO
     * @see TransactionService#createBudget(com.mycompany.app.dto.BudgetCreationDTO)
     * @see #createTransaction(TransactionCreationDTO)
     */
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

    /**
     * @brief Calculates the net balance (income minus expenses) for a user
     *        within a date range.
     *
     * <b>Endpoint:</b> {@code GET /transaction/net-balance}
     *
     * Computes: <code>balance = &sum;(INGRESO) &minus; &sum;(GASTO)</code>
     * for all transactions belonging to the specified user whose date falls
     * within [{@code startDate}, {@code endDate}] (inclusive).
     * {@code LIQUIDACION} transactions are <em>not</em> included in the
     * calculation.
     *
     * <b>Example request URL:</b>
     * <pre>{@code
     * GET /transaction/net-balance?userId=7&startDate=2026-01-01T00:00:00&endDate=2026-01-31T23:59:59&token=eyJhbG...
     * }</pre>
     *
     * @param userId    the unique database identifier of the user whose balance
     *                  is to be computed (required)
     * @param startDate the inclusive start of the date range in ISO&nbsp;8601
     *                  date-time format, e.g. {@code 2026-01-01T00:00:00}
     *                  (required)
     * @param endDate   the inclusive end of the date range in ISO&nbsp;8601
     *                  date-time format, e.g. {@code 2026-01-31T23:59:59}
     *                  (required)
     * @param token     a valid JWT authorization token (required)
     *
     * @return a {@link ResponseEntity} with:
     *         <ul>
     *           <li>HTTP 200 (OK) &ndash; body contains the net balance as a
     *               {@link Double} value</li>
     *           <li>HTTP 401 (Unauthorized) &ndash; invalid or expired JWT token</li>
     *           <li>HTTP 500 (Internal Server Error) &ndash; unexpected error
     *               during calculation</li>
     *         </ul>
     *
     * @note This is the only endpoint in the controller that uses
     *       {@code @GetMapping} and query parameters instead of a JSON body.
     *
     * @see TransactionService#getNetBalance(Integer, java.time.LocalDateTime, java.time.LocalDateTime)
     */
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