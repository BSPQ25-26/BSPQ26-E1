package com.mycompany.app.dto;

public class BudgetCreationDTO {
    
    private Double limitAmount;
    private Integer categoryId;
    private Integer userId;
    private String token; // Necesario para la validación de seguridad

    // Constructor vacío (necesario para Spring/Jackson)
    public BudgetCreationDTO() {}

    // Getters y Setters
    public Double getLimitAmount() { return limitAmount; }
    public void setLimitAmount(Double limitAmount) { this.limitAmount = limitAmount; }

    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}