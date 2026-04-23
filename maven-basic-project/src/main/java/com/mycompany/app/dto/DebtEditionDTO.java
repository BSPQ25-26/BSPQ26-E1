package com.mycompany.app.dto;

public class DebtEditionDTO {
    private Integer id;
    private String token;
    private Double importe;
    private Integer deudorId;
    private Integer acreedorId;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
    public Integer getDeudorId() { return deudorId; }
    public void setDeudorId(Integer deudorId) { this.deudorId = deudorId; }
    public Integer getAcreedorId() { return acreedorId; }
    public void setAcreedorId(Integer acreedorId) { this.acreedorId = acreedorId; }
}