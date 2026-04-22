package com.mycompany.app.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

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

    @OneToMany(mappedBy = "grupo", cascade = CascadeType.ALL)
    private Set<Transaction> transacciones = new HashSet<>();

    public Group() {
        this.fechaCreacion = LocalDateTime.now();
    }

    public Group(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public Set<Usuario> getMiembros() { return miembros; }
    public Set<Transaction> getTransacciones() { return transacciones; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }
    public void setMiembros(Set<Usuario> miembros) { this.miembros = miembros; }
    public void setTransacciones(Set<Transaction> transacciones) { this.transacciones = transacciones; }

    public void addMiembro(Usuario usuario) {
        this.miembros.add(usuario);
    }

    public void removeMiembro(Usuario usuario) {
        this.miembros.remove(usuario);
    }

    public void addTransaccion(Transaction transaction) {
        this.transacciones.add(transaction);
        transaction.setGrupo(this);
    }

    public void removeTransaccion(Transaction transaction) {
        this.transacciones.remove(transaction);
        transaction.setGrupo(null);
    }
}