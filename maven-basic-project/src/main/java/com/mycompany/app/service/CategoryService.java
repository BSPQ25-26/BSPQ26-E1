package com.mycompany.app.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.mycompany.app.dto.CategoryCreationDTO;
import com.mycompany.app.dto.CategoryDeletionDTO;
import com.mycompany.app.model.Category;
import com.mycompany.app.model.Usuario;
import com.mycompany.app.repository.CategoryRepository;
import com.mycompany.app.repository.UsuarioRepository;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final UsuarioRepository usuarioRepository;

    public CategoryService(CategoryRepository categoryRepository, UsuarioRepository usuarioRepository) {
        this.categoryRepository = categoryRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public Boolean createCategory(CategoryCreationDTO request) {
        try {
            Optional<Usuario> userOpt = usuarioRepository.findById(request.getUserId());
            
            if (userOpt.isEmpty()) {
                return false;
            }

            Category newCategory = new Category();
            newCategory.setName(request.getName());
            newCategory.setUser(userOpt.get());

            categoryRepository.save(newCategory);
            return true;

        } catch (Exception e) {
            System.err.println("Error creating category: " + e.getMessage());
            return false;
        }
    }

    public List<Category> getCategoriesByUser(Integer userId) {
        List<Category> allCategories = categoryRepository.findByUserIdIsNull();
        
        List<Category> userCategories = categoryRepository.findByUserId(userId);
        
        allCategories.addAll(userCategories);
        
        return allCategories;
    }

    public Boolean deleteCategory(CategoryDeletionDTO request) {
        try {
            if (!categoryRepository.existsById(request.getCategoryId())) {
                return false;
            }
            
            categoryRepository.deleteById(request.getCategoryId());
            return true;

        } catch (Exception e) {
            System.err.println("Error deleting category: " + e.getMessage());
            return false;
        }
    }
}