package com.mycompany.app.dto;

public class TransactionDeletionDTO {

    private String accessToken;
    private Integer transactionId;

    public TransactionDeletionDTO() {}

    public TransactionDeletionDTO(String accessToken, Integer transactionId) {
        this.accessToken = accessToken;
        this.transactionId  = transactionId;
    }

    public String  getAccessToken() { return accessToken; }
    public Integer  getTransactionId()   { return transactionId; }

    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setTransactionId(Integer transactionId) { this.transactionId = transactionId; }
    
}
