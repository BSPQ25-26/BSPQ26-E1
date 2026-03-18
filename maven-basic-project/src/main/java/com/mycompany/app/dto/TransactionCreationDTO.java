package com.mycompany.app.dto;

public class TransactionCreationDTO {

    private String token; 
    private String concepto;
    private Double importeTotal;
    private String tipoTransaccion;
    private Integer categoriaId;
    private Integer grupoId; 
    private Integer creadorId;
    
    public TransactionCreationDTO() {
    }

    public TransactionCreationDTO(String concepto, Double importeTotal, String tipoTransaccion, 
                                  Integer categoriaId, Integer grupoId, Integer creadorId, String token) {
        this.concepto = concepto;
        this.importeTotal = importeTotal;
        this.tipoTransaccion = tipoTransaccion;
        this.categoriaId = categoriaId;
        this.grupoId = grupoId;
        this.creadorId = creadorId;
        this.token = token;
    }

    public String getConcepto() { return concepto; }
    public Double getImporteTotal() { return importeTotal; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public Integer getCategoriaId() { return categoriaId; }
    public Integer getGrupoId() { return grupoId; }
    public Integer getCreadorId() { return creadorId; }
    public String getToken() { return token; }

    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }
    public void setToken(String token) { this.token = token; }
}