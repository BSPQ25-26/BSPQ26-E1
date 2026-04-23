package com.mycompany.app.dto;

import java.util.List;

public class TranscactionDebtEditionDTO {
    private String token;
    private String concepto;
    private Double importeTotal;
    private String tipoTransaccion;
    private Integer grupoId;
    private Integer categoriaId;
    private Integer creadorId;
    private List<DebtEditionDTO> deudas;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getConcepto() { return concepto; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public Double getImporteTotal() { return importeTotal; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public Integer getGrupoId() { return grupoId; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
    public Integer getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public Integer getCreadorId() { return creadorId; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }
    public List<DebtEditionDTO> getDeudas() { return deudas; }
    public void setDeudas(List<DebtEditionDTO> deudas) { this.deudas = deudas; }
}