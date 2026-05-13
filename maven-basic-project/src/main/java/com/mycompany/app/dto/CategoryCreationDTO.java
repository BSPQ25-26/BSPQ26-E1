package com.mycompany.app.dto;

public class CategoryCreationDTO {
    private String name;
    private Integer userId;
    private String token;
    private String icon;

    // Constructores
    public CategoryCreationDTO() {}

    // Getters y Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
}