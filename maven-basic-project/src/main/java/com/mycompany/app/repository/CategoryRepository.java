package com.mycompany.app.repository;

import com.mycompany.app.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Integer> {
    // Método para buscar las categorías de un usuario específico
    List<Category> findByUsuarioId(Integer usuarioId);
}