package com.mycompany.app.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String nombre;
    private String email;
    private String contraseña;
    private Double balance;

    @ManyToMany(mappedBy = "miembros")
    private Set<Group> groups = new HashSet<>();

    public Usuario() {
    }

    public Usuario(String nombre, String email, String contraseña, Double balance) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
        this.balance = balance;
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getContraseña() { return contraseña; }
    public Double getBalance() { return balance; }
    public Set<Group> getGroups() { return groups; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setEmail(String email) { this.email = email; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }
    public void setBalance(Double balance) { this.balance = balance; }
    public void setGroups(Set<Group> groups) { this.groups = groups; }

    public void addBalance(Double sum) { this.balance = this.balance + sum; }
    public void substractBalance(Double sum) { this.balance = this.balance - sum; }
}