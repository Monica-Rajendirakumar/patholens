package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
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
    private static final int HTTP_UNAUTHORIZED = 401;
    private static final String LOGIN_BUTTON_TEXT = "Login";
    private static final String LOGGING_IN_TEXT = "Logging in...";

    private TextInputEditText etEmail;
    private TextInputEditText etPassword;
    private Button btnLogin;
    private TextView tvSignUp;
    private TextView tvForgotPassword;
    private ProgressBar progressBar;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        prefsManager = new PrefsManager(this);

        if (prefsManager.isLoggedIn()) {
            navigateToMain();
            return;
        }

        initializeViews();
        setupClickListeners();
    }

    private void initializeViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvSignUp = findViewById(R.id.tvSignUp);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLoginClick());
        tvSignUp.setOnClickListener(v -> navigateToRegistration());
        tvForgotPassword.setOnClickListener(v -> navigateToForgotPassword());
    }

    private void handleLoginClick() {
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (validateInput(email, password)) {
            performLogin(email, password);
        }
    }

    private void navigateToRegistration() {
        Intent intent = new Intent(this, RegistrationActivity.class);
        startActivity(intent);
    }

    private void navigateToForgotPassword() {
        Intent intent = new Intent(this, ForgotPasswordActivity.class);
        startActivity(intent);
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
        setLoadingState(true);

        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<LoginResponse> call = RetrofitClient.getInstance()
                .getApiService()
                .login(loginRequest);

        call.enqueue(createLoginCallback());
    }

    private Callback<LoginResponse> createLoginCallback() {
        return new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                setLoadingState(false);
                handleLoginResponse(response);
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                setLoadingState(false);
                handleLoginFailure(t);
            }
        };
    }

    private void handleLoginResponse(Response<LoginResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            LoginResponse loginResponse = response.body();

            if (loginResponse.isSuccess()) {
                handleSuccessfulLogin(loginResponse);
            } else {
                showToast(loginResponse.getMessage());
            }
        } else {
            handleLoginError(response.code());
        }
    }

    private void handleSuccessfulLogin(LoginResponse loginResponse) {
        LoginResponse.Data data = loginResponse.getData();
        LoginResponse.User user = data.getUser();

        saveLoginData(data, user);
        showWelcomeMessage(user.getName());
        navigateToMain();
    }

    private void saveLoginData(LoginResponse.Data data, LoginResponse.User user) {
        prefsManager.saveLoginData(
                data.getToken(),
                user.getId(),
                user.getName(),
                user.getEmail()
        );
    }

    private void showWelcomeMessage(String userName) {
        showToast("Welcome " + userName + "!");
    }

    private void handleLoginError(int statusCode) {
        if (statusCode == HTTP_UNAUTHORIZED) {
            showToast("Invalid email or password");
        } else {
            showToast("Login failed. Please try again.");
        }
        Log.e(TAG, "Login failed: " + statusCode);
    }

    private void handleLoginFailure(Throwable t) {
        showToast("Network error. Please check your connection.");
        Log.e(TAG, "Login error: " + t.getMessage(), t);
    }

    private void setLoadingState(boolean loading) {
        btnLogin.setEnabled(!loading);
        btnLogin.setText(loading ? LOGGING_IN_TEXT : LOGIN_BUTTON_TEXT);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}