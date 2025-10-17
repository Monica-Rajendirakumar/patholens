package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.patholens.api.ApiService;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.UpdateUserRequest;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private static final String TAG = "EditProfileActivity";

    private ImageView btnBack;
    private EditText etFirstName, etLastName, etAge, etEmail, etPhone;
    private Spinner spinnerGender;
    private Button btnConfirm, btnChangePassword;

    private PrefsManager prefsManager;
    private ApiService apiService;

    private String[] genderOptions = {"Gender", "Male", "Female", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        prefsManager = new PrefsManager(this);
        apiService = RetrofitClient.getInstance().getApiService();

        initializeViews();
        setupSpinner();
        loadExistingData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadExistingData() {
        Intent intent = getIntent();
        if (intent != null) {
            String fullName = intent.getStringExtra("fullName");
            if (fullName != null && fullName.contains(" ")) {
                String[] names = fullName.split(" ", 2);
                etFirstName.setText(names[0]);
                etLastName.setText(names.length > 1 ? names[1] : "");
            } else {
                etFirstName.setText(fullName);
            }

            String age = intent.getStringExtra("age");
            if (age != null && !age.equals("0")) {
                etAge.setText(age);
            }

            String gender = intent.getStringExtra("gender");
            if (gender != null) {
                int position = getGenderPosition(gender);
                spinnerGender.setSelection(position);
            }

            String contact = intent.getStringExtra("contact");
            if (contact != null && !contact.isEmpty()) {
                // Remove country code if present
                String phoneOnly = contact.replaceAll("^\\+\\d+\\s*", "").trim();
                etPhone.setText(phoneOnly);
            }

            String email = intent.getStringExtra("email");
            if (email != null) {
                etEmail.setText(email);
            }
        }
    }

    private int getGenderPosition(String gender) {
        if (gender == null) return 0;

        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equalsIgnoreCase(gender)) {
                return i;
            }
        }
        return 0;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v -> {
            try {
                Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "ChangePasswordActivity not found", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        int age;
        try {
            age = Integer.parseInt(ageStr);
            if (age < 1 || age > 150) {
                etAge.setError("Please enter a valid age");
                etAge.requestFocus();
                return;
            }
        } catch (NumberFormatException e) {
            etAge.setError("Please enter a valid age");
            etAge.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (gender.equals("Gender")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine first and last name
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
        String contact = "" + phone;

        // Call API to update profile
        updateProfileOnServer(fullName, email, age, gender.toLowerCase(), contact);
    }

    private void updateProfileOnServer(String fullName, String email, int age, String gender, String contact) {
        String token = prefsManager.getBearerToken();

        android.util.Log.d("EditProfile", "=== UPDATE PROFILE DEBUG ===");
        android.util.Log.d("EditProfile", "Token: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "NULL"));
        android.util.Log.d("EditProfile", "Name: " + fullName);
        android.util.Log.d("EditProfile", "Email: " + email);
        android.util.Log.d("EditProfile", "Age: " + age);
        android.util.Log.d("EditProfile", "Gender: " + gender);
        android.util.Log.d("EditProfile", "Phone: " + contact);

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create request object
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(fullName);
        request.setEmail(email);
        request.setAge(age);
        request.setGender(gender);
        request.setPhoneNumber(contact);

        // Show loading state
        btnConfirm.setEnabled(false);
        btnConfirm.setText("Updating...");

        apiService.updateAuthenticatedUser(token, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");

                android.util.Log.d("EditProfile", "Response Code: " + response.code());
                android.util.Log.d("EditProfile", "Response URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    android.util.Log.d("EditProfile", "Status: " + userResponse.isStatus());

                    if (userResponse.isStatus() && userResponse.getData() != null) {
                        UserResponse.UserData userData = userResponse.getData();

                        android.util.Log.d("EditProfile", "Update Success!");
                        android.util.Log.d("EditProfile", "New Name: " + userData.getName());

                        // Save to preferences
                        prefsManager.saveUserProfile(
                                userData.getName(),
                                userData.getEmail(),
                                userData.getAge(),
                                userData.getGender(),
                                userData.getPhoneNumber()
                        );

                        Toast.makeText(EditProfileActivity.this,
                                "Profile updated successfully", Toast.LENGTH_SHORT).show();

                        // Return to previous activity
                        setResult(RESULT_OK);
                        finish();
                    } else {
                        String message = userResponse.getMessage();
                        if (message == null || message.isEmpty()) {
                            message = "Failed to update profile";
                        }
                        android.util.Log.e("EditProfile", "API Error: " + message);
                        android.util.Log.e("EditProfile", "Errors: " + userResponse.getErrors());

                        Toast.makeText(EditProfileActivity.this,
                                "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        android.util.Log.e("EditProfile", "HTTP Error " + response.code() + ": " + errorBody);
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to update (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        android.util.Log.e("EditProfile", "Error reading error body", e);
                        Toast.makeText(EditProfileActivity.this,
                                "Failed to update profile", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                btnConfirm.setEnabled(true);
                btnConfirm.setText("Confirm");

                android.util.Log.e("EditProfile", "Network Failure", t);
                android.util.Log.e("EditProfile", "Request URL: " + call.request().url());
                android.util.Log.e("EditProfile", "Error Message: " + t.getMessage());

                Toast.makeText(EditProfileActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}