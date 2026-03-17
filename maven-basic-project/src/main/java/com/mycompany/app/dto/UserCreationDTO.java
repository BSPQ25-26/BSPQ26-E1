package com.mycompany.app.dto;

public class UserCreationDTO {
    private String username;
    private String email;
    private String password;
    private Double balance;


    public UserCreationDTO() {}

    public UserCreationDTO(String username, String email, String password, Double balance){
        this.username = username;
        this.email = email;
        this.password = password;
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Double getBalance(){
        return balance;
    }

    public void setBalance(Double balance){
        this.balance = balance;
    }
}
