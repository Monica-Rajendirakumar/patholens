package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.LoginRequest;
import com.example.patholens.modules.LoginResponse;
import com.example.patholens.utils.PrefsManager;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private TextView tvSignUp, tvForgotPassword;
    private ProgressBar progressBar;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize SharedPreferences manager
        prefsManager = new PrefsManager(this);

        // Check if user is already logged in
        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        // Initialize views
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);

        // Add a ProgressBar to your XML layout or create it programmatically
        // For now, we'll handle loading state with button text

        // Login button click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (validateInput(email, password)) {
                performLogin(email, password);
            }
        });

        // Sign up link click
        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // Forgot password click
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private boolean validateInput(String email, String password) {
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }

    private void performLogin(String email, String password) {
        // Disable button and show loading state
        btnLogin.setEnabled(false);
        btnLogin.setText("Logging in...");

        // Create login request
        LoginRequest loginRequest = new LoginRequest(email, password);

        // Make API call
        Call<LoginResponse> call = RetrofitClient.getInstance().getApiService().login(loginRequest);
        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                // Re-enable button
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        // Login successful
                        LoginResponse.Data data = loginResponse.getData();
                        LoginResponse.User user = data.getUser();

                        // Save user data and token
                        prefsManager.saveLoginData(
                                data.getToken(),
                                user.getId(),
                                user.getName(),
                                user.getEmail()
                        );

                        Toast.makeText(LoginActivity.this,
                                "Welcome " + user.getName() + "!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to main activity
                        navigateToMain();
                    } else {
                        // Login failed
                        Toast.makeText(LoginActivity.this,
                                loginResponse.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    // Handle error response
                    if (response.code() == 401) {
                        Toast.makeText(LoginActivity.this,
                                "Invalid email or password",
                                Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(LoginActivity.this,
                                "Login failed. Please try again.",
                                Toast.LENGTH_LONG).show();
                    }
                    Log.e(TAG, "Login failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                // Re-enable button
                btnLogin.setEnabled(true);
                btnLogin.setText("Login");

                // Network error or server not reachable
                Toast.makeText(LoginActivity.this,
                        "Network error. Please check your connection.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Login error: " + t.getMessage(), t);
            }
        });
    }

    private void navigateToMain() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close login activity
    }
}