package com.mycompany.app.dto;

public class GrupoMemberDTO {

    private String token;
    private Integer grupoId;
    private Integer usuarioId;

    public GrupoMemberDTO() {
    }

    public GrupoMemberDTO(String token, Integer grupoId, Integer usuarioId) {
        this.token = token;
        this.grupoId = grupoId;
        this.usuarioId = usuarioId;
    }

    public String getToken() { return token; }
    public Integer getGrupoId() { return grupoId; }
    public Integer getUsuarioId() { return usuarioId; }

    public void setToken(String token) { this.token = token; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
    public void setUsuarioId(Integer usuarioId) { this.usuarioId = usuarioId; }
}
