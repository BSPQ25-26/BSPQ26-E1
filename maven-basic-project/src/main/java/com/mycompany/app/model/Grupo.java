package com.mycompany.app.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "grupo")
public class Grupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String nombre;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    @ManyToMany
    @JoinTable(
        name = "grupo_miembros",
        joinColumns = @JoinColumn(name = "grupo_id"),
        inverseJoinColumns = @JoinColumn(name = "usuario_id")
    )
    private Set<Usuario> miembros = new HashSet<>();

    public Grupo() {
    }

    public Grupo(String nombre, Usuario creador) {
        this.nombre = nombre;
        this.creador = creador;
        this.miembros.add(creador);
    }

    public Integer getId() { return id; }
    public String getNombre() { return nombre; }
    public Usuario getCreador() { return creador; }
    public Set<Usuario> getMiembros() { return miembros; }

    public void setId(Integer id) { this.id = id; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCreador(Usuario creador) { this.creador = creador; }
    public void setMiembros(Set<Usuario> miembros) { this.miembros = miembros; }
}
