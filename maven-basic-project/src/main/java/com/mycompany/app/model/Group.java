package com.mycompany.app.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupo")
public class Group {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @Column(length = 500)
    private String descripcion;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @ManyToMany
    @JoinTable(
        name = "usuario_grupo",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> miembros = new HashSet<>();

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    private Set<Transaction> transacciones = new HashSet<>();

    public Group() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Group(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters
    public Integer getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public Set<Usuario> getMiembros() {
        return miembros;
    }

    public Set<Transaction> getTransacciones() {
        return transacciones;
    }

    // Setters
    public void setId(Integer id) {
        this.id = id;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public void setMiembros(Set<Usuario> miembros) {
        this.miembros = miembros;
    }

    public void setTransacciones(Set<Transaction> transacciones) {
        this.transacciones = transacciones;
    }

    // Helper methods for managing relationships
    public void addMiembro(Usuario usuario) {
        this.miembros.add(usuario);
        usuario.getGroups().add(this);
    }

    public void removeMiembro(Usuario usuario) {
        this.miembros.remove(usuario);
        usuario.getGroups().remove(this);
    }

    public void addTransaccion(Transaction transaction) {
        this.transacciones.add(transaction);
        transaction.setGroup(this);
    }

    public void removeTransaccion(Transaction transaction) {
        this.transacciones.remove(transaction);
        transaction.setGroup(null);
    }
}
