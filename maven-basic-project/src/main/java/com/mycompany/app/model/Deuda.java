package com.mycompany.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "Deudas")
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "transaccion_id", nullable = false)
    private Transaction transaccionOriginal;

    @ManyToOne
    @JoinColumn(name = "deudor_id", nullable = false)
    private Usuario deudor;

    @ManyToOne
    @JoinColumn(name = "acreedor_id", nullable = false)
    private Usuario acreedor;

    @Column(nullable = false)
    private Double importe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoDeuda estado = EstadoDeuda.PENDIENTE;

    @OneToOne 
    @JoinColumn(name = "liquidacion_id")
    private Transaction liquidacion;

    public Deuda() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Transaction getTransaccionOriginal() { return transaccionOriginal; }
    public void setTransaccionOriginal(Transaction transaccionOriginal) { this.transaccionOriginal = transaccionOriginal; }
    public Usuario getDeudor() { return deudor; }
    public void setDeudor(Usuario deudor) { this.deudor = deudor; }
    public Usuario getAcreedor() { return acreedor; }
    public void setAcreedor(Usuario acreedor) { this.acreedor = acreedor; }
    public Double getImporte() { return importe; }
    public void setImporte(Double importe) { this.importe = importe; }
    public EstadoDeuda getEstado() { return estado; }
    public void setEstado(EstadoDeuda estado) { this.estado = estado; }
    public Transaction getLiquidacion() { return liquidacion; }
    public void setLiquidacion(Transaction liquidacion) { this.liquidacion = liquidacion; }
}