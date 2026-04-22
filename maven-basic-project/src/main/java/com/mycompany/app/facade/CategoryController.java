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

@Tag(name = "Categories", description = "Endpoints for managing user categories, including creation, retrieval, and deletion operations")
@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService; 

    public CategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @Operation(
            summary = "Create a new category",
            description = "Creates a new custom category associated with a specific user. The request must include valid category details and an active authorization token.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category successfully created."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The category could not be created due to invalid data or constraints."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the category details and authorization token", required = true)
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

    @Operation(
            summary = "Get all categories for a user",
            description = "Retrieves a complete list of categories accessible to a specific user. This includes their personal custom categories as well as any globally available categories.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Categories retrieved successfully. Returns a list of category objects."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or missing.")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(
            @Parameter(description = "The unique identifier (ID) of the user", required = true)
            @PathVariable("userId") Integer userId,
            
            @Parameter(description = "A valid authorization token to verify the user's session", required = true)
            @RequestParam("token") String token
    ) { 
            
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Category> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Delete an existing category",
            description = "Permanently deletes a category from the system based on the provided details. The request requires an active authorization token to verify permissions.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category successfully deleted."),
                    @ApiResponse(responseCode = "400", description = "Bad Request. The category could not be deleted (e.g., it does not exist or has dependent items)."),
                    @ApiResponse(responseCode = "401", description = "Unauthorized. The provided token is invalid or expired.")
            }
    )
    @PostMapping("/delete")
    public ResponseEntity<String> deleteCategory(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Data transfer object containing the target category ID and authorization token", required = true)
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