package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.AuthResponse;
import com.example.patholens.modules.RegisterRequest;
import com.example.patholens.utils.PrefsManager;
import com.google.android.material.textfield.TextInputEditText;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegistrationActivity extends AppCompatActivity {

    private static final String TAG = "RegistrationActivity";

    private TextInputEditText etName, etAge, etContact, etEmail, etPassword, etPasswordConfirm;
    private Spinner spinnerGender;
    private Button btnRegister;
    private TextView tvLogin;
    private String selectedGender = "";
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize PrefsManager
        prefsManager = new PrefsManager(this);

        // Initialize views
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etContact = findViewById(R.id.etContact);
        spinnerGender = findViewById(R.id.spinnerGender);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etPasswordConfirm = findViewById(R.id.etPasswordConfirm);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Setup gender spinner
        setupGenderSpinner();

        // Register button click
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                performRegistration();
            }
        });

        // Login link click
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGender = "";
            }
        });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();

        // Validate Name
        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (name.length() < 2) {
            etName.setError("Name must be at least 2 characters");
            etName.requestFocus();
            return false;
        }

        // Validate Email
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        // Validate Password
        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            etPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return false;
        }

        // Validate Password Confirmation
        if (TextUtils.isEmpty(passwordConfirm)) {
            etPasswordConfirm.setError("Please confirm your password");
            etPasswordConfirm.requestFocus();
            return false;
        }

        if (!password.equals(passwordConfirm)) {
            etPasswordConfirm.setError("Passwords do not match");
            etPasswordConfirm.requestFocus();
            return false;
        }

        // Validate Age
        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        try {
            int ageValue = Integer.parseInt(age);
            if (ageValue < 1 || ageValue > 120) {
                etAge.setError("Please enter a valid age (1-120)");
                etAge.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid age");
            etAge.requestFocus();
            return false;
        }

        // Validate Gender
        if (selectedGender.equals("Select Gender") || TextUtils.isEmpty(selectedGender)) {
            Toast.makeText(this, "Please select your gender", Toast.LENGTH_SHORT).show();
            spinnerGender.requestFocus();
            return false;
        }

        // Validate Phone Number
        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Phone number is required");
            etContact.requestFocus();
            return false;
        }

        if (contact.length() < 10) {
            etContact.setError("Please enter a valid phone number (min 10 digits)");
            etContact.requestFocus();
            return false;
        }

        // Check if phone number contains only digits
        if (!contact.matches("[0-9]+")) {
            etContact.setError("Phone number should contain only digits");
            etContact.requestFocus();
            return false;
        }

        return true;
    }

    private void performRegistration() {
        // Disable button and show loading state
        btnRegister.setEnabled(false);
        btnRegister.setText("Registering...");

        // Get form data
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();
        String passwordConfirm = etPasswordConfirm.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String contact = etContact.getText().toString().trim();

        // Parse age to Integer
        Integer age = null;
        try {
            age = Integer.parseInt(ageStr);
        } catch (NumberFormatException e) {
            Log.e(TAG, "Age parsing error: " + e.getMessage());
        }

        // Convert gender to match backend format
        String gender = convertGenderForBackend(selectedGender);

        // Create registration request
        RegisterRequest registerRequest = new RegisterRequest(
                name,
                email,
                password,
                passwordConfirm,
                age,
                gender,
                contact
        );

        // Make API call
        Call<AuthResponse> call = RetrofitClient.getInstance().getApiService().register(registerRequest);
        call.enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                // Re-enable button
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();

                    if (authResponse.isSuccess()) {
                        // Registration successful
                        AuthResponse.Data data = authResponse.getData();
                        AuthResponse.User user = data.getUser();

                        // Save user data and token
                        prefsManager.saveUserSession(
                                data.getToken(),
                                user.getId(),
                                user.getName(),
                                user.getEmail()
                        );

                        Toast.makeText(RegistrationActivity.this,
                                "Welcome " + user.getName() + "! Registration successful!",
                                Toast.LENGTH_SHORT).show();

                        // Navigate to main activity
                        navigateToMain();
                    } else {
                        // Registration failed
                        handleErrorResponse(authResponse);
                    }
                } else {
                    // Handle HTTP error response
                    handleHttpError(response.code(), response.message());
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                // Re-enable button
                btnRegister.setEnabled(true);
                btnRegister.setText("Register");

                // Network error or server not reachable
                Toast.makeText(RegistrationActivity.this,
                        "Network error. Please check your connection.",
                        Toast.LENGTH_LONG).show();
                Log.e(TAG, "Registration error: " + t.getMessage(), t);
            }
        });
    }

    private String convertGenderForBackend(String displayGender) {
        // Convert display values to backend format
        switch (displayGender) {
            case "Male":
                return "male";
            case "Female":
                return "female";
            case "Other":
                return "other";
            case "Prefer not to say":
                return "prefer_not_to_say";
            default:
                return null;
        }
    }

    private void handleErrorResponse(AuthResponse authResponse) {
        String message = authResponse.getMessage();
        Object errors = authResponse.getErrors();

        if (errors != null) {
            // Display validation errors
            Toast.makeText(this, message + "\nPlease check your input.", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Validation errors: " + errors.toString());
        } else {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        }
    }

    private void handleHttpError(int statusCode, String message) {
        String errorMessage;

        switch (statusCode) {
            case 422:
                errorMessage = "Please check your input. Some fields may be invalid or already registered.";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            default:
                errorMessage = "Registration failed: " + message;
                break;
        }

        Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        Log.e(TAG, "HTTP Error " + statusCode + ": " + message);
    }

    private void navigateToMain() {
        Intent intent = new Intent(RegistrationActivity.this, MainActivity.class);
        startActivity(intent);
        finish(); // Close registration activity
    }
}