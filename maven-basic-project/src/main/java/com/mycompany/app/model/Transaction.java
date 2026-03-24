package com.mycompany.app.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "transacciones")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String concepto;

    @Column(name = "importe_total", nullable = false)
    private Double importeTotal;

    @Column(name = "tipo_transaccion", nullable = false)
    private String tipoTransaccion;

    @Column(name = "fecha")
    private LocalDateTime fecha;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Category categoria;

    @Column(name = "grupo_id")
    private Integer grupoId;

    @ManyToOne
    @JoinColumn(name = "creador_id", nullable = false)
    private Usuario creador;

    public Transaction() {
    }

    public Transaction(String concepto, Double importeTotal, String tipoTransaccion, 
                       Category categoria, Integer grupoId, Usuario creador) {
        this.concepto = concepto;
        this.importeTotal = importeTotal;
        this.tipoTransaccion = tipoTransaccion;
        this.categoria = categoria;
        this.grupoId = grupoId;
        this.creador = creador;
        this.fecha = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getConcepto() { return concepto; }
    public Double getImporteTotal() { return importeTotal; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public LocalDateTime getFecha() { return fecha; }
    public Category getCategoria() { return categoria; }
    public Integer getGrupoId() { return grupoId; }
    public Usuario getCreador() { return creador; }

    public void setId(Integer id) { this.id = id; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCategoria(Category categoria) { this.categoria = categoria; }
    public void setGrupoId(Integer grupoId) { this.grupoId = grupoId; }
    public void setCreador(Usuario creador) { this.creador = creador; }
}