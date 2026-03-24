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
import io.swagger.v3.oas.annotations.responses.ApiResponse;

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
            summary = "Create category",
            description = "Creates a new category for a specific user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: category creation completed"),
                    @ApiResponse(responseCode = "400", description = "Bad Request: category could not be created"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
            }
    )
    @PostMapping("/create")
    public ResponseEntity<String> createCategory(@RequestBody CategoryCreationDTO request) {
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
            summary = "Get user categories",
            description = "Retrieves all categories for a specific user (including global categories)",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: categories retrieved successfully"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
            }
    )
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(
            @PathVariable("userId") Integer userId,
            @RequestParam("token") String token) { 
            
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<Category> categories = categoryService.getCategoriesByUser(userId);
        return ResponseEntity.ok(categories);
    }

    @Operation(
            summary = "Delete category",
            description = "Deletes an existing category",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OK: category deletion completed"),
                    @ApiResponse(responseCode = "400", description = "Bad Request: category could not be deleted"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized: invalid credentials")
            }
    )
    @PostMapping("/delete")
    public ResponseEntity<String> deleteCategory(@RequestBody CategoryDeletionDTO request) {
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