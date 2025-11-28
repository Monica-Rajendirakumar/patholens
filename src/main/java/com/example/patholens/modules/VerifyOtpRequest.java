package com.example.patholens.modules;

public class VerifyOtpRequest {
    private String email;
    private int otp; // Changed from String to int

    public VerifyOtpRequest(String email, String otpString) {
        this.email = email;
        this.otp = Integer.parseInt(otpString); // Convert string to int
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getOtp() {
        return otp;
    }

    public void setOtp(int otp) {
        this.otp = otp;
    }
}