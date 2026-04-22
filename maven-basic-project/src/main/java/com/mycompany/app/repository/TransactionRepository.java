package com.mycompany.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.mycompany.app.model.Group;
import com.mycompany.app.model.Transaction;
import com.mycompany.app.model.Usuario;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByCreador(Usuario creador);

    List<Transaction> findByGrupo(Group grupo);

    @Query("SELECT DISTINCT t FROM Transaction t WHERE t.creador = :user OR t.grupo IN :groups ORDER BY t.fecha DESC")
    List<Transaction> findByCreadorOrGrupoIn(@Param("user") Usuario user, @Param("groups") List<Group> groups);
}
