package com.mycompany.app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.dto.CategoryDeletionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void createCategory_WithValidUser_ReturnsTrue() {
        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setUserId(1);
        dto.setName("Food");

        Usuario user = new Usuario("Ana", "ana@mail.com", "pwd", 0.0);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(user));

        boolean result = categoryService.createCategory(dto);

        assertTrue(result);
    }

    @Test
    void createCategory_WhenUserNotFound_ReturnsFalse() {
        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setUserId(10);
        dto.setName("Food");

        when(usuarioRepository.findById(10)).thenReturn(Optional.empty());

        boolean result = categoryService.createCategory(dto);

        assertFalse(result);
    }

    @Test
    void createCategory_WhenRepositoryFails_ReturnsFalse() {
        CategoryCreationDTO dto = new CategoryCreationDTO();
        dto.setUserId(1);
        dto.setName("Food");

        Usuario user = new Usuario("Ana", "ana@mail.com", "pwd", 0.0);
        when(usuarioRepository.findById(1)).thenReturn(Optional.of(user));
        when(categoryRepository.save(any(Category.class))).thenThrow(new RuntimeException("insert error"));

        boolean result = categoryService.createCategory(dto);

        assertFalse(result);
    }

    @Test
    void getCategoriesByUser_MergesGlobalAndUserCategories() {
        List<Category> global = new ArrayList<>();
        global.add(new Category("Global", null));
        List<Category> userCategories = new ArrayList<>();
        userCategories.add(new Category("Personal", new Usuario()));

        when(categoryRepository.findByUserIdIsNull()).thenReturn(global);
        when(categoryRepository.findByUserId(1)).thenReturn(userCategories);

        List<Category> result = categoryService.getCategoriesByUser(1);

        assertEquals(2, result.size());
    }

    @Test
    void deleteCategory_WhenExists_ReturnsTrue() {
        CategoryDeletionDTO dto = new CategoryDeletionDTO();
        dto.setCategoryId(7);
        when(categoryRepository.existsById(7)).thenReturn(true);

        boolean result = categoryService.deleteCategory(dto);

        assertTrue(result);
    }

    @Test
    void deleteCategory_WhenNotExists_ReturnsFalse() {
        CategoryDeletionDTO dto = new CategoryDeletionDTO();
        dto.setCategoryId(7);
        when(categoryRepository.existsById(7)).thenReturn(false);

        boolean result = categoryService.deleteCategory(dto);

        assertFalse(result);
    }
}
