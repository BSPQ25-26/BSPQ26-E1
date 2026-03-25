package com.mycompany.app.dto;

public class UpdateGroupDTO {
    private String accessToken;
    private Integer groupId;
    private String nombre;
    private String descripcion;

    public UpdateGroupDTO() {
    }

    public UpdateGroupDTO(String accessToken, Integer groupId, String nombre, String descripcion) {
        this.accessToken = accessToken;
        this.groupId = groupId;
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
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
