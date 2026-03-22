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


    //Una vez estén los models de categoría y más cosas, hay que sustituir los Ids de FK
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

    @Column(name = "categoria_id", nullable = false)
    private Integer categoriaId;

    @ManyToOne
    @JoinColumn(name = "grupo_id")
    private Group group;

    @Column(name = "creador_id", nullable = false)
    private Integer creadorId;

    public Transaction() {
    }

    public Transaction(String concepto, Double importeTotal, String tipoTransaccion,
                       Integer categoriaId, Group group, Integer creadorId) {
        this.concepto = concepto;
        this.importeTotal = importeTotal;
        this.tipoTransaccion = tipoTransaccion;
        this.categoriaId = categoriaId;
        this.group = group;
        this.creadorId = creadorId;
        this.fecha = LocalDateTime.now();
    }

    public Integer getId() { return id; }
    public String getConcepto() { return concepto; }
    public Double getImporteTotal() { return importeTotal; }
    public String getTipoTransaccion() { return tipoTransaccion; }
    public LocalDateTime getFecha() { return fecha; }
    public Integer getCategoriaId() { return categoriaId; }
    public Group getGroup() { return group; }
    public Integer getCreadorId() { return creadorId; }

    public void setId(Integer id) { this.id = id; }
    public void setConcepto(String concepto) { this.concepto = concepto; }
    public void setImporteTotal(Double importeTotal) { this.importeTotal = importeTotal; }
    public void setTipoTransaccion(String tipoTransaccion) { this.tipoTransaccion = tipoTransaccion; }
    public void setFecha(LocalDateTime fecha) { this.fecha = fecha; }
    public void setCategoriaId(Integer categoriaId) { this.categoriaId = categoriaId; }
    public void setGroup(Group group) { this.group = group; }
    public void setCreadorId(Integer creadorId) { this.creadorId = creadorId; }
}