package com.mycompany.app.facade;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.dto.CategoryDeletionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * @file CategoryController.java
 * @brief REST controller for managing transaction categories (RMI facade layer).
 *
 * This controller exposes the remote HTTP API endpoints for all category-related
 * operations, acting as the <b>RMI interface</b> between clients and the
 * server-side business logic. It delegates to {@link CategoryService} for
 * persistence operations and uses {@link AuthService} to validate session tokens
 * before processing any request.
 *
 * <h2>Base URL</h2>
 * All endpoints are mapped under <code>/categories</code>.
 *
 * <h2>Security</h2>
 * Every endpoint validates the caller's token via
 * {@link AuthService#isValidToken(String)} before executing any business logic.
 * Requests with a missing or invalid token are rejected immediately with
 * {@code 401 Unauthorized}.
 *
 * <h2>Category Model</h2>
 * A {@link Category} has three fields:
 * <ul>
 *   <li>{@code name} – human-readable label (e.g. "Food", "Transport").</li>
 *   <li>{@code icon} – optional icon identifier for UI display.</li>
 *   <li>{@code user} – owning {@code Usuario}; {@code null} for system-wide
 *       (global) categories that are visible to all users.</li>
 * </ul>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li><b>Create</b> – add a new user-scoped category.</li>
 *   <li><b>Retrieve</b> – list all categories visible to a given user
 *       (personal + global).</li>
 *   <li><b>Delete</b> – permanently remove a category by ID.</li>
 * </ul>
 *
 * @author BSPQ26-E1 Team
 * @version 1.1
 * @since 2026-05-01
 * @see CategoryService
 * @see AuthService
 * @see CategoryCreationDTO
 * @see CategoryDeletionDTO
 * @see Category
 */
@Tag(
    name = "Categories",
    description = "Endpoints for managing user categories, including creation, "
                + "retrieval, and deletion operations"
)
@RestController
@RequestMapping("/categories")
public class CategoryController {

    // -----------------------------------------------------------------------
    // Dependencies
    // -----------------------------------------------------------------------

    /**
     * Service layer that encapsulates all category business logic: creation,
     * retrieval by user, and deletion.
     */
    private final CategoryService categoryService;

    /**
     * Service layer used exclusively for token validation. Each endpoint calls
     * {@link AuthService#isValidToken(String)} before delegating to
     * {@link CategoryService}.
     */
    private final AuthService authService;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    /**
     * Constructs a {@code CategoryController} with its required service dependencies.
     *
     * <p>Spring Boot will inject both services automatically via constructor
     * injection when the application context is initialised.</p>
     *
     * @param categoryService the service handling category CRUD operations;
     *                        must not be {@code null}
     * @param authService     the service used to validate session tokens;
     *                        must not be {@code null}
     */
    public CategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    // -----------------------------------------------------------------------
    // Endpoints
    // -----------------------------------------------------------------------

    /**
     * Creates a new custom category for a specific user.
     *
     * <p>The category is owned by the user identified by {@code userId} in the
     * request body. Token validation is performed first; if the token is invalid
     * the request is rejected before any persistence is attempted.</p>
     *
     * <p>Category names are not enforced as unique at the controller level, but
     * should be kept unique per user to avoid confusion in the UI.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /categories/create</pre>
     *
     * <h3>Request Body – {@link CategoryCreationDTO}</h3>
     * <ul>
     *   <li>{@code name}   – display label for the category (required)</li>
     *   <li>{@code userId} – ID of the owning user (required)</li>
     *   <li>{@code icon}   – optional icon identifier for UI rendering</li>
     *   <li>{@code token}  – active session token for authentication (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * This endpoint returns no body. The HTTP status code is the sole indicator
     * of the operation result.
     *
     * @param request the DTO carrying the category {@code name}, {@code userId},
     *                optional {@code icon}, and the caller's session {@code token}
     * @return <ul>
     *   <li>{@code 200 OK} (empty body) if the category was successfully created</li>
     *   <li>{@code 400 Bad Request} (empty body) if creation failed — e.g. the
     *       specified {@code userId} does not exist in the system</li>
     *   <li>{@code 401 Unauthorized} (empty body) if the token is invalid or
     *       expired</li>
     * </ul>
     * @pre  The user identified by {@code userId} must exist in the database.
     * @post A new {@link Category} record is persisted and associated with the
     *       specified user; it becomes visible in subsequent
     *       {@link #getUserCategories} calls for that user.
     * @see CategoryCreationDTO
     * @see CategoryService#createCategory(CategoryCreationDTO)
     * @see AuthService#isValidToken(String)
     */
    @Operation(
            summary = "Create a new category",
            description = "Creates a new custom category associated with a specific user. "
                        + "The request must include valid category details and an active "
                        + "authorization token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category successfully created."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The category could not be created due to invalid data or constraints."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data transfer object containing the category details and authorization token",
                required = true
            )
            @RequestBody CategoryCreationDTO request
    ) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = categoryService.createCategory(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Retrieves all categories visible to a specific user.
     *
     * <p>The returned list is the <em>union</em> of two sets:</p>
     * <ol>
     *   <li><b>Global categories</b> – categories with no owner ({@code user_id IS NULL}
     *       in the database), shared across all users of the system.</li>
     *   <li><b>User-specific categories</b> – categories created by the user
     *       identified by {@code userId}.</li>
     * </ol>
     *
     * <p>This merged list is what should be presented to the user when they
     * classify a transaction or define a budget.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>GET /categories/user/{userId}?token=&lt;uuid&gt;</pre>
     *
     * <h3>Response Body</h3>
     * A JSON array of {@link Category} objects, each containing:
     * {@code id}, {@code name}, {@code icon}, and {@code user}
     * (may be {@code null} for global categories). Returns an empty array
     * if the user has no personal categories and no global ones exist.
     *
     * @param userId the unique integer identifier of the user whose categories
     *               are to be retrieved
     * @param token  an active session token to authenticate the request;
     *               must not be {@code null} or blank
     * @return <ul>
     *   <li>{@code 200 OK} with a (possibly empty) {@code List<Category>}
     *       combining global and user-owned categories</li>
     *   <li>{@code 401 Unauthorized} (empty body) if the token is invalid
     *       or has been invalidated</li>
     * </ul>
     * @see Category
     * @see CategoryService#getCategoriesByUser(Integer)
     * @see AuthService#isValidToken(String)
     */
    @Operation(
            summary = "Get all categories for a user",
            description = "Retrieves a complete list of categories accessible to a specific user. "
                        + "This includes their personal custom categories as well as any globally "
                        + "available (system-wide) categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully. Returns a list of category objects."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or missing.")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(
            @Parameter(description = "The unique identifier (ID) of the user whose categories are to be retrieved", required = true)
            @PathVariable("userId") Integer userId,

            @Parameter(description = "A valid session token to authenticate the request", required = true)
            @RequestParam("token") String token
    ) {
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Category> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    /**
     * Permanently deletes an existing category from the system.
     *
     * <p>The category is identified by its integer {@code categoryId} supplied
     * in the request body. Token validation is performed first; only requests
     * with a valid session token proceed to deletion.</p>
     *
     * <p>If the category does not exist in the database the service returns
     * {@code false} and the endpoint responds with {@code 400 Bad Request}.</p>
     *
     * <h3>HTTP Method &amp; Path</h3>
     * <pre>POST /categories/delete</pre>
     *
     * <h3>Request Body – {@link CategoryDeletionDTO}</h3>
     * <ul>
     *   <li>{@code categoryId} – ID of the category to delete (required)</li>
     *   <li>{@code token}      – active session token for authentication (required)</li>
     * </ul>
     *
     * <h3>Response Body</h3>
     * This endpoint returns no body. The HTTP status code is the sole indicator
     * of the operation result.
     *
     * @param request the DTO carrying the target {@code categoryId} and the
     *                caller's session {@code token}
     * @return <ul>
     *   <li>{@code 200 OK} (empty body) if the category was found and deleted</li>
     *   <li>{@code 400 Bad Request} (empty body) if no category with the given
     *       ID exists, or if an unexpected error prevented deletion</li>
     *   <li>{@code 401 Unauthorized} (empty body) if the token is invalid or
     *       expired</li>
     * </ul>
     * @pre  The category identified by {@code categoryId} must exist in the database.
     * @post The {@link Category} record is permanently removed. Any transactions
     *       that previously referenced this category may lose their category
     *       association depending on the database cascade configuration.
     * @warning Deleting a category is <strong>irreversible</strong>. Transactions
     *          that reference it may be affected; verify cascade rules before
     *          calling this endpoint.
     * @see CategoryDeletionDTO
     * @see CategoryService#deleteCategory(CategoryDeletionDTO)
     * @see AuthService#isValidToken(String)
     */
    @Operation(
            summary = "Delete an existing category",
            description = "Permanently deletes a category from the system based on the provided "
                        + "details. The request requires an active authorization token to verify "
                        + "permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category successfully deleted."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The category could not be deleted (e.g., it does not exist or has dependent items)."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/delete")
    public ResponseEntity<String> deleteCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "Data transfer object containing the target category ID and authorization token",
                required = true
            )
            @RequestBody CategoryDeletionDTO request
    ) {
        if (!authService.isValidToken(request.getToken())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Boolean result = categoryService.deleteCategory(request);
        if (result) {
            return new ResponseEntity<>(HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}