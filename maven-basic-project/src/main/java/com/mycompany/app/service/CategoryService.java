package com.mycompany.app.service;

import com.mycompany.app.model.Category;
import com.mycompany.app.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public List<Category> getCategoriesByUser(Integer userId) {
        return categoryRepository.findByUserId(userId);
    }

    public void deleteCategory(Integer id) {
        categoryRepository.deleteById(id);
    }
}