package com.example.patholens.modules;

public class VerifyOtpResponse {
    private boolean success;
    private String message;
    private String email;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getEmail() {
        return email;
    }
}