package com.mycompany.app.repository;

import com.mycompany.app.model.SavingGoals;
import com.mycompany.app.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SavingGoalsRepository extends JpaRepository<SavingGoals, Long> {
    SavingGoals findByUser(Usuario user);
    void deleteByUser(Usuario user);
}