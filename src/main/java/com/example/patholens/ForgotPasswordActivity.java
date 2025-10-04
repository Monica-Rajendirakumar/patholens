package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etEmail;
    private Button btnResetPassword;
    private TextView tvBackToLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        tvBackToLogin = findViewById(R.id.tvBackToLogin);

        // Reset Password button click
        btnResetPassword.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();

            if (validateEmail(email)) {
                // TODO: Implement your password reset logic here
                // Send reset email or verification code

//                Toast.makeText(ForgotPasswordActivity.this,
//                        "Reset link sent to your email", Toast.LENGTH_SHORT).show();

                // Navigate to reset password screen
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("email", email);
                startActivity(intent);
            }
        });

        // Back to login click
        tvBackToLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private boolean validateEmail(String email) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        return true;
    }
}