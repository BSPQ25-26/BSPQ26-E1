package com.mycompany.app.facade;

import com.mycompany.app.model.Category;
import com.mycompany.app.service.CategoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping("/create")
    public ResponseEntity<Category> createCategory(@RequestBody Category category) {
        // Nota: Idealmente deberías asignar el usuario autenticado a la categoría aquí
        return ResponseEntity.ok(categoryService.saveCategory(category));
    }

    @GetMapping("/user/{usuarioId}")
    public ResponseEntity<List<Category>> getUserCategories(@PathVariable Integer usuarioId) {
        return ResponseEntity.ok(categoryService.getCategoriesByUsuario(usuarioId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Integer id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }
}