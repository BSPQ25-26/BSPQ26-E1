package com.mycompany.app.dto;

public class AuthResponseDTO {

    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private Integer userId;
    private String nombre;

    public AuthResponseDTO() {}

    public AuthResponseDTO(String accessToken, String tokenType,
                           Integer expiresIn, Integer userId, String nombre) {
        this.accessToken = accessToken;
        this.tokenType   = tokenType;
        this.expiresIn   = expiresIn;
        this.userId      = userId;
        this.nombre      = nombre;
    }

    public String  getAccessToken() { return accessToken; }
    public String  getTokenType()   { return tokenType; }
    public Integer getExpiresIn()   { return expiresIn; }
    public Integer getUserId()      { return userId; }
    public String  getNombre()      { return nombre; }
}
