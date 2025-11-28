package com.example.patholens.modules;

public class OtpResponse {
    private boolean success;
    private String message;
    private String email;
    private Integer expires_in_minutes;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }

    public Integer getExpiresInMinutes() {
        return expires_in_minutes;
    }
}