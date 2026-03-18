package com.mycompany.app.dto;

public class TransactionEditionDTO {

    private String accessToken;
    private String concepto;
    private Double importeTotal;
    private String tipoTransaccion;
    private Integer categoriaId;
    private Integer grupoId; 
    private Integer creadorId;

    public TransactionEditionDTO() {}

    public TransactionEditionDTO(String accessToken, String concepto, Double importeTotal, String tipoTransaccion, Integer categoriaId, Integer grupoId, Integer creadorId) {
        this.accessToken = accessToken;
        this.concepto = concepto;
        this.importeTotal = importeTotal;
        this.tipoTransaccion = tipoTransaccion;
        this.categoriaId = categoriaId;
        this.grupoId = grupoId;
        this.creadorId = creadorId;
    }

    public String getAccessToken() { return accessToken; }
    public String getConcepto() { return concepto; }
    public Double getImporteTotal() { return importeTotal; }
    public String getTipoTransaccion() { return tipoTransaccion; } 
    public Integer getCategoriaId() { return categoriaId; }
    public Integer getGrupoId() { return grupoId; }
    public Integer getCreadorId() { return creadorId; }
    
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }
}