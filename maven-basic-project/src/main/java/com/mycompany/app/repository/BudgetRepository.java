package com.mycompany.app.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.app.model.Budget;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Integer> {
    
    Optional<Budget> findByCategoryIdAndUserId(Integer categoryId, Integer userId);

}