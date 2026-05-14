package com.mycompany.app.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.mycompany.app.model.Transaction;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    
    List<Transaction> findByCreadorId(Integer creadorId);
    
    List<Transaction> findByGrupoId(Integer grupoId);

    @Query("SELECT COALESCE(SUM(t.importeTotal), 0.0) FROM Transaction t WHERE t.creador.id = :creadorId AND t.fecha BETWEEN :startDate AND :endDate AND t.tipoTransaccion = :tipo")
    Double sumImporteByDateRangeAndTipo(
        @Param("creadorId") Integer creadorId, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate, 
        @Param("tipo") String tipo
    );
    
}