package com.mycompany.app.dto;

public class GrupoCreationDTO {

    private String token;
    private String nombre;
    private Integer creadorId;

    public GrupoCreationDTO() {
    }

    public GrupoCreationDTO(String token, String nombre, Integer creadorId) {
        this.token = token;
        this.nombre = nombre;
        this.creadorId = creadorId;
    }

    public String getToken() { return token; }
    public String getNombre() { return nombre; }
    public Integer getCreadorId() { return creadorId; }

    public void setToken(String token) { this.token = token; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }
}
