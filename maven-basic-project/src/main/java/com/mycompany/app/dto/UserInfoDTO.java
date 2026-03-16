package com.mycompany.app.dto;

public class UserInfoDTO {
    private String username;
    private String email;
    private Double balance;


    public UserInfoDTO() {}

    public UserInfoDTO(String username, String email, Double balance){
        this.username = username;
        this.email = email;
        this.balance = balance;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Double getBalance(){
        return balance;
    }

    public void setBalance(Double balance){
        this.balance = balance;
    }
}
