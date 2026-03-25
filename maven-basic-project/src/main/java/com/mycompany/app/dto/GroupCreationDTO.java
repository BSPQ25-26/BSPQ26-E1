package com.mycompany.app.dto;

public class GroupCreationDTO {
    private String accessToken;
    private String nombre;
    private String descripcion;

    public GroupCreationDTO() {
    }

    public GroupCreationDTO(String accessToken, String nombre, String descripcion) {
        this.accessToken = accessToken;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
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
}
