package com.mycompany.app.dto;

public class CategoryDeletionDTO {
    private Integer categoryId;
    private String token;

    // Constructores
    public CategoryDeletionDTO() {}

    // Getters y Setters
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}