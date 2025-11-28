package com.example.patholens.modules;

public class ResetPasswordRequest {
    private String email;
    private String password;
    private String password_confirmation;

    public ResetPasswordRequest(String email, String password, String passwordConfirmation) {
        this.email = email;
        this.password = password;
        this.password_confirmation = passwordConfirmation;
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

    public String getPassword_confirmation() {
        return password_confirmation;
    }

    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }
}