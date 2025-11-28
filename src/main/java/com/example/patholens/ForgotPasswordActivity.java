package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import com.example.patholens.modules.SendOtpRequest;
import com.example.patholens.modules.OtpResponse;
import com.example.patholens.modules.VerifyOtpRequest;
import com.example.patholens.modules.VerifyOtpResponse;
import com.example.patholens.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;
    private TextView tvErrorMessage;
    private TextView tvDescription;

    // OTP Section
    private LinearLayout otpSection;
    private TextInputEditText etOtp1, etOtp2, etOtp3, etOtp4, etOtp5, etOtp6;
    private Button btnVerifyOtp;
    private TextView tvOtpError;
    private TextView tvResendOtp;

    private String userEmail;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvDescription = findViewById(R.id.tvDescription);

        // OTP views
        otpSection = findViewById(R.id.otpSection);
        etOtp1 = findViewById(R.id.etOtp1);
        etOtp2 = findViewById(R.id.etOtp2);
        etOtp3 = findViewById(R.id.etOtp3);
        etOtp4 = findViewById(R.id.etOtp4);
        etOtp5 = findViewById(R.id.etOtp5);
        etOtp6 = findViewById(R.id.etOtp6);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpError = findViewById(R.id.tvOtpError);
        tvResendOtp = findViewById(R.id.tvResendOtp);

        setupOtpInputs();

        // Reset Password button click (Request OTP)
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            tvErrorMessage.setVisibility(View.GONE);

            if (validateEmail(email)) {
                sendOtpRequest(email);
            }
        });

        // Verify OTP button click
        btnVerifyOtp.setOnClickListener(v -> {
            String otp = getEnteredOtp();
            tvOtpError.setVisibility(View.GONE);

            if (validateOtp(otp)) {
                verifyOtpRequest(otp);
            }
        });

        // Resend OTP
        tvResendOtp.setOnClickListener(v -> {
            tvOtpError.setVisibility(View.GONE);
            resendOtpRequest();
        });

        // Back to login click
        tvBackToLogin.setOnClickListener(v -> {
            finish();
        });
    }

    private void setupOtpInputs() {
        // Auto-focus to next input
        etOtp1.addTextChangedListener(new OtpTextWatcher(etOtp1, etOtp2));
        etOtp2.addTextChangedListener(new OtpTextWatcher(etOtp2, etOtp3));
        etOtp3.addTextChangedListener(new OtpTextWatcher(etOtp3, etOtp4));
        etOtp4.addTextChangedListener(new OtpTextWatcher(etOtp4, etOtp5));
        etOtp5.addTextChangedListener(new OtpTextWatcher(etOtp5, etOtp6));
        etOtp6.addTextChangedListener(new OtpTextWatcher(etOtp6, null));
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            showError("Email is required");
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showError("Please enter a valid email address");
            return false;
        }

        return true;
    }

    // ============= API INTEGRATION =============

    private void sendOtpRequest(String email) {
        if (isLoading) return;

        isLoading = true;
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Sending...");

        SendOtpRequest request = new SendOtpRequest(email);

        Call<OtpResponse> call = RetrofitClient.getInstance().getApiService().sendOtp(request);
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                isLoading = false;
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Send OTP");

                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();

                    if (otpResponse.isSuccess()) {
                        userEmail = email;
                        showOtpSection();
                        Toast.makeText(ForgotPasswordActivity.this,
                                otpResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    } else {
                        showError(otpResponse.getMessage());
                    }
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        showError("Failed to send OTP. Please try again.");
                    } catch (Exception e) {
                        showError("Failed to send OTP. Please try again.");
                    }
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                isLoading = false;
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Send OTP");
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void verifyOtpRequest(String otp) {
        if (isLoading) return;

        isLoading = true;
        btnVerifyOtp.setEnabled(false);
        btnVerifyOtp.setText("Verifying...");

        VerifyOtpRequest request = new VerifyOtpRequest(userEmail, otp);

        Call<VerifyOtpResponse> call = RetrofitClient.getInstance().getApiService().verifyOtp(request);
        call.enqueue(new Callback<VerifyOtpResponse>() {
            @Override
            public void onResponse(Call<VerifyOtpResponse> call, Response<VerifyOtpResponse> response) {
                isLoading = false;
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Verify OTP");

                if (response.isSuccessful() && response.body() != null) {
                    VerifyOtpResponse verifyResponse = response.body();

                    if (verifyResponse.isSuccess()) {
                        // OTP verified successfully, navigate to reset password
                        Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                        intent.putExtra("email", userEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        showOtpError(verifyResponse.getMessage());
                        clearOtpInputs();
                    }
                } else {
                    showOtpError("Invalid or expired OTP. Please try again.");
                    clearOtpInputs();
                }
            }

            @Override
            public void onFailure(Call<VerifyOtpResponse> call, Throwable t) {
                isLoading = false;
                btnVerifyOtp.setEnabled(true);
                btnVerifyOtp.setText("Verify OTP");
                showOtpError("Network error. Please check your connection.");
            }
        });
    }

    private void resendOtpRequest() {
        if (isLoading || userEmail == null) return;

        isLoading = true;
        tvResendOtp.setEnabled(false);

        SendOtpRequest request = new SendOtpRequest(userEmail);

        Call<OtpResponse> call = RetrofitClient.getInstance().getApiService().resendOtp(request);
        call.enqueue(new Callback<OtpResponse>() {
            @Override
            public void onResponse(Call<OtpResponse> call, Response<OtpResponse> response) {
                isLoading = false;
                tvResendOtp.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    OtpResponse otpResponse = response.body();

                    if (otpResponse.isSuccess()) {
                        clearOtpInputs();
                        // Show success message
                        tvOtpError.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
                        tvOtpError.setText("OTP resent successfully");
                        tvOtpError.setVisibility(View.VISIBLE);

                        // Hide message after 2 seconds
                        new android.os.Handler().postDelayed(() -> {
                            tvOtpError.setVisibility(View.GONE);
                        }, 2000);
                    } else {
                        showOtpError(otpResponse.getMessage());
                    }
                } else {
                    showOtpError("Failed to resend OTP. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<OtpResponse> call, Throwable t) {
                isLoading = false;
                tvResendOtp.setEnabled(true);
                showOtpError("Network error. Please check your connection.");
            }
        });
    }

    // ============= UI HELPER METHODS =============

    private void showOtpSection() {
        // Update UI
        tvDescription.setText("Enter the 6-digit OTP sent to " + userEmail);
        etEmail.setEnabled(false);
        btnResetPassword.setVisibility(View.GONE);
        otpSection.setVisibility(View.VISIBLE);

        // Focus on first OTP input
        etOtp1.requestFocus();
    }

    private String getEnteredOtp() {
        return etOtp1.getText().toString() +
                etOtp2.getText().toString() +
                etOtp3.getText().toString() +
                etOtp4.getText().toString() +
                etOtp5.getText().toString() +
                etOtp6.getText().toString();
    }

    private boolean validateOtp(String otp) {
        if (otp.length() != 6) {
            showOtpError("Please enter complete 6-digit OTP");
            return false;
        }
        return true;
    }

    private void clearOtpInputs() {
        etOtp1.setText("");
        etOtp2.setText("");
        etOtp3.setText("");
        etOtp4.setText("");
        etOtp5.setText("");
        etOtp6.setText("");
        etOtp1.requestFocus();
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void showOtpError(String message) {
        tvOtpError.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvOtpError.setText(message);
        tvOtpError.setVisibility(View.VISIBLE);
    }

    // TextWatcher for OTP inputs
    private class OtpTextWatcher implements android.text.TextWatcher {
        private final TextInputEditText currentView;
        private final TextInputEditText nextView;

        OtpTextWatcher(TextInputEditText currentView, TextInputEditText nextView) {
            this.currentView = currentView;
            this.nextView = nextView;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (s.length() == 1 && nextView != null) {
                nextView.requestFocus();
            }
        }

        @Override
        public void afterTextChanged(android.text.Editable s) {}
    }
}