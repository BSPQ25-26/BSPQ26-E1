package com.mycompany.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "limit_amount", nullable = false)
    private Double limitAmount;

    @ManyToOne
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private Usuario user;

    public Budget() {}

    public Budget(Double limitAmount, Category category, Usuario user) {
        this.limitAmount = limitAmount;
        this.category = category;
        this.user = user;
    }

    // Getters y Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public Double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(Double limitAmount) { this.limitAmount = limitAmount; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
}