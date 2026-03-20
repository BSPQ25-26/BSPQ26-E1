package com.mycompany.app.model;

import jakarta.persistence.*;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    private String name;
    
    private String type; 

    @ManyToOne
    @JoinColumn(name = "user_id") 
    private Usuario user; 

    public Category() {}

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    
    public Usuario getUser() { return user; }
    public void setUser(Usuario user) { this.user = user; }
}