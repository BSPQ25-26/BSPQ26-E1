package com.mycompany.app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.mycompany.app.model.Deuda;

@Repository
public interface DeudaRepository extends JpaRepository<Deuda, Integer> {
    List<Deuda> findByDeudorId(Integer usuarioId);
    List<Deuda> findByAcreedorId(Integer usuarioId);
    List<Deuda> findByTransaccionOriginalId(Integer transaccionId);
}