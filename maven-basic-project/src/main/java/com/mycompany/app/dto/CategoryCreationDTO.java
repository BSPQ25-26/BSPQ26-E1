package com.mycompany.app.dto;

public class CategoryCreationDTO {
    private String name;
    private Integer userId;
    private String token;

    // Constructores
    public CategoryCreationDTO() {}

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}