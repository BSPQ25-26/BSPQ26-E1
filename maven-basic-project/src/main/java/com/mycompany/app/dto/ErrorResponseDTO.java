package com.mycompany.app.dto;

public class ErrorResponseDTO {

    private int status;
    private String message;
    private long timestamp;

    public ErrorResponseDTO(int status, String message) {
        this.status    = status;
        this.message   = message;
        this.timestamp = System.currentTimeMillis();
    }

    public int    getStatus()    { return status; }
    public String getMessage()   { return message; }
    public long   getTimestamp() { return timestamp; }
}
