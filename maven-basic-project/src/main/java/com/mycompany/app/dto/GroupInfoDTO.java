package com.mycompany.app.dto;

import java.time.LocalDateTime;
import java.util.List;

public class GroupInfoDTO {
    private Integer id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;
    private List<String> miembrosEmails;
    private Integer numeroMiembros;

    public GroupInfoDTO() {
    }

    public GroupInfoDTO(Integer id, String nombre, String descripcion, LocalDateTime fechaCreacion,
                        List<String> miembrosEmails, Integer numeroMiembros) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
        this.miembrosEmails = miembrosEmails;
        this.numeroMiembros = numeroMiembros;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public List<String> getMiembrosEmails() {
        return miembrosEmails;
    }

    public void setMiembrosEmails(List<String> miembrosEmails) {
        this.miembrosEmails = miembrosEmails;
    }

    public Integer getNumeroMiembros() {
        return numeroMiembros;
    }

    public void setNumeroMiembros(Integer numeroMiembros) {
        this.numeroMiembros = numeroMiembros;
    }
}
