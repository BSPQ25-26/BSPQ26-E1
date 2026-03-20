package com.mycompany.app.facade;

import com.mycompany.app.model.Category;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final AuthService authService; 

    public CategoryController(CategoryService categoryService, AuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(
            @RequestBody Category category,
            @RequestParam("token") String token) {
            
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Category>> getUserCategories(
            @PathVariable Integer userId,
            @RequestParam("token") String token) { 
            
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        return ResponseEntity.ok(categoryService.getCategoriesByUser(userId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable Integer id,
            @RequestParam("token") String token) {
            
        if (!authService.isValidToken(token)) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}