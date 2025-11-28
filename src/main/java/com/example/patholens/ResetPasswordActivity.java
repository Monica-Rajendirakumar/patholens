package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.ResetPasswordRequest;
import com.example.patholens.modules.ResetPasswordResponse;
import com.google.android.material.textfield.TextInputEditText;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

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

        userEmail = getIntent().getStringExtra("email");

        if (!isValidEmail()) {
            Toast.makeText(this, "Invalid session. Please try again.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initializeViews();
        setupListeners();
    }

    private boolean isValidEmail() {
        return userEmail != null && !userEmail.isEmpty();
    }

    private void initializeViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);
    }

    private void setupListeners() {
        btnResetPassword.setOnClickListener(v -> {
            tvErrorMessage.setVisibility(View.GONE);

            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validatePasswords(newPassword, confirmPassword)) {
                resetPassword(newPassword, confirmPassword);
            }
        });

        tvBackToLogin.setOnClickListener(v -> navigateToLogin());
    }

    private boolean validatePasswords(String password, String confirmPassword) {
        if (TextUtils.isEmpty(password)) {
            showError("Password is required");
            return false;
        }

        if (password.length() < MIN_PASSWORD_LENGTH) {
            showError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
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
        if (isLoading) {
            return;
        }

        setLoadingState(true);

        ResetPasswordRequest request = new ResetPasswordRequest(userEmail, password, confirmPassword);

        Call<ResetPasswordResponse> call = RetrofitClient.getInstance()
                .getApiService()
                .resetPassword(request);

        call.enqueue(new Callback<ResetPasswordResponse>() {
            @Override
            public void onResponse(Call<ResetPasswordResponse> call, Response<ResetPasswordResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleResetResponse(response.body());
                } else {
                    showError("Failed to reset password. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ResetPasswordResponse> call, Throwable t) {
                setLoadingState(false);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void handleResetResponse(ResetPasswordResponse resetResponse) {
        if (resetResponse.isSuccess()) {
            Toast.makeText(this, resetResponse.getMessage(), Toast.LENGTH_LONG).show();
            navigateToLogin();
        } else {
            showError(resetResponse.getMessage());
        }
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnResetPassword.setEnabled(!loading);
        btnResetPassword.setText(loading ? "Resetting..." : "Reset Password");
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
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
        navigateToLogin();
    }
}