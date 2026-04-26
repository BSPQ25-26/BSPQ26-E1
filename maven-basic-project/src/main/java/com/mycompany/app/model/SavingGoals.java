package com.mycompany.app.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "objetivos_ahorro") // Nombre de la tabla corregido
public class SavingGoals {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToOne
    // Cambiado de id_usuario a usuario_id
    @JoinColumn(name = "usuario_id", referencedColumnName = "id", unique = true) 
    private Usuario user;

    // Aquí le decimos a Spring el nombre exacto de la columna en la BD
    @Column(name = "cantidad_objetivo") 
    private Double amount;

    @Column(name = "fecha_inicio")
    private LocalDate initialDate;

    @Column(name = "fecha_fin")
    private LocalDate endDate;

    public SavingGoals() {}

    public SavingGoals(Usuario user, Double amount, LocalDate initialDate, LocalDate endDate) {
        this.user = user;
        this.amount = amount;
        this.initialDate = initialDate;
        this.endDate = endDate;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public LocalDate getInitialDate() { return initialDate; }
    public void setInitialDate(LocalDate initialDate) { this.initialDate = initialDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
}