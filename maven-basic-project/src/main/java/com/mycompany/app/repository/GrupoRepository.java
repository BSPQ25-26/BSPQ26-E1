package com.mycompany.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.app.model.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {
    List<Grupo> findByCreadorId(Integer creadorId);
    List<Grupo> findByMiembrosId(Integer usuarioId);
}
