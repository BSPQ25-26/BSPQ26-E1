package com.mycompany.app.dto;

public class DeudaCreationDTO {
    private Integer transaccionId;
    private Integer deudorId;
    private Integer acreedorId;
    private Double importe;
    private String token;

    public Integer getTransaccionId() { return transaccionId; }
    public void setTransaccionId(Integer transaccionId) { this.transaccionId = transaccionId; }
    public Integer getDeudorId() { return deudorId; }
    public void setDeudorId(Integer deudorId) { this.deudorId = deudorId; }
    public Integer getAcreedorId() { return acreedorId; }
    public void setAcreedorId(Integer acreedorId) { this.acreedorId = acreedorId; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
    public String getToken() { return this.token; }
}