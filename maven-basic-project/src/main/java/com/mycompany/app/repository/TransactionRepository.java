package com.mycompany.app.repository;

import java.util.List; // Add this import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.mycompany.app.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByCreadorId(Integer creadorId);

    List<Transaction> findByGrupoId(Integer grupoId);
}
