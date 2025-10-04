package com.example.patholens;

import android.os.Bundle;
import android.text.InputType;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private ImageView btnToggleCurrentPassword, btnToggleNewPassword, btnToggleConfirmPassword;
    private Button btnSavePassword;

    private boolean isCurrentPasswordVisible = false;
    private boolean isNewPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

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
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSavePassword.setOnClickListener(v -> changePassword());

        btnToggleCurrentPassword.setOnClickListener(v ->
                togglePasswordVisibility(etCurrentPassword, btnToggleCurrentPassword,
                        isCurrentPasswordVisible = !isCurrentPasswordVisible));

        btnToggleNewPassword.setOnClickListener(v ->
                togglePasswordVisibility(etNewPassword, btnToggleNewPassword,
                        isNewPasswordVisible = !isNewPasswordVisible));

        btnToggleConfirmPassword.setOnClickListener(v ->
                togglePasswordVisibility(etConfirmPassword, btnToggleConfirmPassword,
                        isConfirmPasswordVisible = !isConfirmPasswordVisible));
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

        if (currentPassword.isEmpty()) {
            etCurrentPassword.setError("Current password is required");
            etCurrentPassword.requestFocus();
            return;
        }

        if (newPassword.isEmpty()) {
            etNewPassword.setError("New password is required");
            etNewPassword.requestFocus();
            return;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return;
        }

        // Verify current password and update
        // This would typically involve checking with your backend

        Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
        finish();
    }
}