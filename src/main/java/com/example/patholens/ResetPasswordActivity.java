package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ResetPasswordActivity extends AppCompatActivity {

    private TextInputEditText etNewPassword, etConfirmPassword;
    private Button btnConfirm;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Get email from intent
        email = getIntent().getStringExtra("email");

        // Initialize views
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnConfirm = findViewById(R.id.btnConfirm);

        // Confirm button click
        btnConfirm.setOnClickListener(v -> {
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (validatePasswords(newPassword, confirmPassword)) {
                // TODO: Implement your password reset logic here
                // Update password in database

                Toast.makeText(ResetPasswordActivity.this,
                        "Password reset successful", Toast.LENGTH_SHORT).show();

                // Navigate to login screen
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });
    }

    private boolean validatePasswords(String newPassword, String confirmPassword) {
        if (TextUtils.isEmpty(newPassword)) {
            etNewPassword.setError("Password is required");
            etNewPassword.requestFocus();
            return false;
        }

        if (newPassword.length() < 6) {
            etNewPassword.setError("Password must be at least 6 characters");
            etNewPassword.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(confirmPassword)) {
            etConfirmPassword.setError("Confirm password is required");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!newPassword.equals(confirmPassword)) {
            etConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        return true;
    }
}