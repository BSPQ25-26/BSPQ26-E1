package com.mycompany.app.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.dto.CategoryDeletionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.service.AuthService;
import com.mycompany.app.service.CategoryService;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @Mock
    private AuthService authService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void createCategory_WithInvalidToken_ReturnsUnauthorized() {
        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setToken("bad");

        when(authService.isValidToken("bad")).thenReturn(false);

        ResponseEntity<String> response = categoryController.createCategory(dto);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void createCategory_WithValidTokenAndServiceTrue_ReturnsOk() {
        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setToken("ok");

        when(authService.isValidToken("ok")).thenReturn(true);
        when(categoryService.createCategory(dto)).thenReturn(true);

        ResponseEntity<String> response = categoryController.createCategory(dto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void getUserCategories_WithValidToken_ReturnsList() {
        Category category = new Category();
        category.setName("Food");

        when(authService.isValidToken("ok")).thenReturn(true);
        when(categoryService.getCategoriesByUser(1)).thenReturn(List.of(category));

        ResponseEntity<List<Category>> response = categoryController.getUserCategories(1, "ok");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void deleteCategory_WhenServiceReturnsFalse_ReturnsBadRequest() {
        CategoryDeletionDTO dto = new CategoryDeletionDTO();
        dto.setToken("ok");

        when(authService.isValidToken("ok")).thenReturn(true);
        when(categoryService.deleteCategory(dto)).thenReturn(false);

        ResponseEntity<String> response = categoryController.deleteCategory(dto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }
}
