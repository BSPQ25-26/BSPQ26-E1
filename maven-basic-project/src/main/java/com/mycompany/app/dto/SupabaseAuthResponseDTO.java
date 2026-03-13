package com.mycompany.app.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SupabaseAuthResponseDTO {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private Integer expiresIn;

    @JsonProperty("refresh_token")
    private String refreshToken;

    private SupabaseUserDTO user;

    public String getAccessToken()   { return accessToken; }
    public String getTokenType()     { return tokenType; }
    public Integer getExpiresIn()    { return expiresIn; }
    public String getRefreshToken()  { return refreshToken; }
    public SupabaseUserDTO getUser() { return user; }

    public void setAccessToken(String accessToken)   { this.accessToken = accessToken; }
    public void setTokenType(String tokenType)       { this.tokenType = tokenType; }
    public void setExpiresIn(Integer expiresIn)      { this.expiresIn = expiresIn; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setUser(SupabaseUserDTO user)         { this.user = user; }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SupabaseUserDTO {
        private String id;
        private String email;

        public String getId()    { return id; }
        public String getEmail() { return email; }
        public void setId(String id)       { this.id = id; }
        public void setEmail(String email) { this.email = email; }
    }
}
