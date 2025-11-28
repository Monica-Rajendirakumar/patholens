package com.example.patholens.modules;

public class ChangePasswordRequest {
    private String current_password;
    private String password;
    private String password_confirmation;

    public ChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        this.current_password = currentPassword;
        this.password = newPassword;
        this.password_confirmation = confirmPassword;
    }

    // Getters and Setters
    public String getCurrent_password() { return current_password; }
    public void setCurrent_password(String current_password) { this.current_password = current_password; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getPassword_confirmation() { return password_confirmation; }
    public void setPassword_confirmation(String password_confirmation) {
        this.password_confirmation = password_confirmation;
    }
}