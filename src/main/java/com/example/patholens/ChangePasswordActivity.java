package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patholens.modules.ChangePasswordRequest;
import com.example.patholens.modules.ChangePasswordResponse;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.utils.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChangePasswordActivity extends AppCompatActivity {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private ImageView btnBack;
    private EditText etCurrentPassword;
    private EditText etNewPassword;
    private EditText etConfirmPassword;
    private ImageView btnToggleCurrentPassword;
    private ImageView btnToggleNewPassword;
    private ImageView btnToggleConfirmPassword;
    private Button btnSavePassword;
    private TextView tvErrorMessage;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;
    private boolean isLoading = false;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        prefsManager = new PrefsManager(this);
        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnToggleCurrentPassword = findViewById(R.id.btnToggleCurrentPassword);
        btnToggleNewPassword = findViewById(R.id.btnToggleNewPassword);
        btnToggleConfirmPassword = findViewById(R.id.btnToggleConfirmPassword);
        btnSavePassword = findViewById(R.id.btnSavePassword);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSavePassword.setOnClickListener(v -> {
            hideError();
            changePassword();
        });

        btnToggleCurrentPassword.setOnClickListener(v -> {
            isCurrentPasswordVisible = !isCurrentPasswordVisible;
            togglePasswordVisibility(etCurrentPassword, btnToggleCurrentPassword, isCurrentPasswordVisible);
        });

        btnToggleNewPassword.setOnClickListener(v -> {
            isNewPasswordVisible = !isNewPasswordVisible;
            togglePasswordVisibility(etNewPassword, btnToggleNewPassword, isNewPasswordVisible);
        });

        btnToggleConfirmPassword.setOnClickListener(v -> {
            isConfirmPasswordVisible = !isConfirmPasswordVisible;
            togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword, isConfirmPasswordVisible);
        });
    }

    private void togglePasswordVisibility(EditText editText, ImageView imageView, boolean isVisible) {
        if (isVisible) {
            editText.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility);
        } else {
            editText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            imageView.setImageResource(R.drawable.ic_visibility_off);
        }
        editText.setSelection(editText.getText().length());
    }

    private void changePassword() {
        String currentPassword = etCurrentPassword.getText().toString().trim();
        String newPassword = etNewPassword.getText().toString().trim();
        String confirmPassword = etConfirmPassword.getText().toString().trim();

        if (!validatePasswords(currentPassword, newPassword, confirmPassword)) {
            return;
        }

        makeChangePasswordRequest(currentPassword, newPassword, confirmPassword);
    }

    private boolean validatePasswords(String currentPassword, String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(currentPassword)) {
            showError("Current password is required");
            etCurrentPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(newPassword)) {
            showError("New password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < MIN_PASSWORD_LENGTH) {
            showError("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
            etNewPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            showError("Please confirm your new password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            showError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (currentPassword.equals(newPassword)) {
            showError("New password must be different from current password");
            etNewPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void makeChangePasswordRequest(String currentPassword, String newPassword, String confirmPassword) {
        if (isLoading) {
            return;
        }

        setLoadingState(true);

        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,
                newPassword,
                confirmPassword
        );

        String token = prefsManager.getBearerToken();

        Call<ChangePasswordResponse> call = RetrofitClient.getInstance()
                .getApiService()
                .changePassword(token, request);

        call.enqueue(new Callback<ChangePasswordResponse>() {
            @Override
            public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleSuccessfulResponse(response.body());
                } else {
                    showError("Failed to change password. Please try again.");
                }
            }

            @Override
            public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                setLoadingState(false);
                showError("Network error. Please check your connection.");
            }
        });
    }

    private void handleSuccessfulResponse(ChangePasswordResponse changeResponse) {
        if (changeResponse.isSuccess()) {
            Toast.makeText(this, changeResponse.getMessage(), Toast.LENGTH_LONG).show();
            logoutAndRedirect();
        } else {
            showError(changeResponse.getMessage());
        }
    }

    private void setLoadingState(boolean loading) {
        isLoading = loading;
        btnSavePassword.setEnabled(!loading);
        btnSavePassword.setText(loading ? "Changing..." : "Save Password");
    }

    private void logoutAndRedirect() {
        prefsManager.clearSession();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra("password_changed", true);
        startActivity(intent);
        finish();
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        tvErrorMessage.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}