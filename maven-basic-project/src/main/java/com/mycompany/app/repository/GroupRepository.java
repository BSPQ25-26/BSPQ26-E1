package com.mycompany.app.repository;

import com.mycompany.app.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GroupRepository extends JpaRepository<Group, Integer> {

    Optional<Group> findById(Integer id);

    @Query("SELECT g FROM Group g JOIN g.miembros m WHERE m.email = :email")
    List<Group> findByMiembrosEmail(@Param("email") String email);

    @Query("SELECT g FROM Group g JOIN FETCH g.miembros WHERE g.id = :id")
    Optional<Group> findByIdWithMiembros(@Param("id") Integer id);

    @Query("SELECT g FROM Group g JOIN FETCH g.transacciones WHERE g.id = :id")
    Optional<Group> findByIdWithTransacciones(@Param("id") Integer id);
}
