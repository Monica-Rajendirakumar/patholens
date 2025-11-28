package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

import com.example.patholens.modules.ResetPasswordRequest;
import com.example.patholens.modules.ResetPasswordResponse;
import com.example.patholens.api.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnResetPassword;
    private TextView tvErrorMessage;
    private TextView tvBackToLogin;

    private String userEmail;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get email from intent
        userEmail = getIntent().getStringExtra("email");

        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Initialize views
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnResetPassword.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();
            tvErrorMessage.setVisibility(View.GONE);

            if (validatePasswords(newPassword, confirmPassword)) {
                resetPassword(newPassword, confirmPassword);
            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            navigateToLogin();
        });
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password)) {
            showError("Password is required");
            return false;
        }

        if (password.length() < 8) {
            showError("Password must be at least 8 characters");
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError("Please confirm your password");
            return false;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match");
            return false;
        }

        return true;
    }

    private void resetPassword(String password, String confirmPassword) {
        if (isLoading) return;

        isLoading = true;
        btnResetPassword.setEnabled(false);
        btnResetPassword.setText("Resetting...");

        ResetPasswordRequest request = new ResetPasswordRequest(userEmail, password, confirmPassword);

        Call<ResetPasswordResponse> call = RetrofitClient.getInstance().getApiService().resetPassword(request);
        call.enqueue(new Callback<ResetPasswordResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                isLoading = false;
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Reset Password");

                if (response.isSuccessful() && response.body() != null) {
                    ResetPasswordResponse resetResponse = response.body();

                    if (resetResponse.isSuccess()) {
                        Toast.makeText(ResetPasswordActivity.this,
                                resetResponse.getMessage(), Toast.LENGTH_LONG).show();
                        navigateToLogin();
                    } else {
                        showError(resetResponse.getMessage());
                    }
                } else {
                    showError("Failed to reset password. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                isLoading = false;
                btnResetPassword.setEnabled(true);
                btnResetPassword.setText("Reset Password");
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void navigateToLogin() {
        Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onBackPressed() {
        // Prevent going back to OTP verification
        navigateToLogin();
    }
}