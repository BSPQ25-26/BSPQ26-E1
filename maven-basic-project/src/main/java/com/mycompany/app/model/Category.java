package com.mycompany.app.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;
    
    @ManyToOne
    @JoinColumn(name = "user_id") 
    private Usuario user; 

    @Column(name = "Icon")
    private String icon;

    public Category() {}

    public Category(String name, Usuario user, String icon) {
        this.name = name;
        this.user = user;
        this.icon = icon;
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}