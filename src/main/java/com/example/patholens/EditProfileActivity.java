package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
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
    private static final String[] GENDER_OPTIONS = {"Gender", "Male", "Female", "Other"};
    private static final int MIN_AGE = 1;
    private static final int MAX_AGE = 150;

    private ImageView btnBack;
    private EditText etFirstName, etLastName, etAge, etEmail, etPhone;
    private Spinner spinnerGender;
    private Button btnConfirm, btnChangePassword;

    private PrefsManager prefsManager;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeDependencies();
        initializeViews();
        setupSpinner();
        loadExistingData();
        setupClickListeners();
    }

    private void initializeDependencies() {
        prefsManager = new PrefsManager(this);
        apiService = RetrofitClient.getInstance().getApiService();
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
                GENDER_OPTIONS
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadExistingData() {
        Intent intent = getIntent();
        if (intent != null) {
            loadNameData(intent.getStringExtra("fullName"));
            loadAgeData(intent.getStringExtra("age"));
            loadGenderData(intent.getStringExtra("gender"));
            loadPhoneData(intent.getStringExtra("contact"));
            loadEmailData(intent.getStringExtra("email"));
        }
    }

    private void loadNameData(String fullName) {
        if (fullName != null && fullName.contains(" ")) {
            String[] names = fullName.split(" ", 2);
            etFirstName.setText(names[0]);
            etLastName.setText(names.length > 1 ? names[1] : "");
        } else if (fullName != null) {
            etFirstName.setText(fullName);
        }
    }

    private void loadAgeData(String age) {
        if (age != null && !age.equals("0")) {
            etAge.setText(age);
        }
    }

    private void loadGenderData(String gender) {
        if (gender != null) {
            int position = getGenderPosition(gender);
            spinnerGender.setSelection(position);
        }
    }

    private void loadPhoneData(String contact) {
        if (contact != null && !contact.isEmpty()) {
            String phoneOnly = contact.replaceAll("^\\+\\d+\\s*", "").trim();
            etPhone.setText(phoneOnly);
        }
    }

    private void loadEmailData(String email) {
        if (email != null) {
            etEmail.setText(email);
        }
    }

    private int getGenderPosition(String gender) {
        if (gender == null) {
            return 0;
        }

        for (int i = 0; i < GENDER_OPTIONS.length; i++) {
            if (GENDER_OPTIONS[i].equalsIgnoreCase(gender)) {
                return i;
            }
        }
        return 0;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnConfirm.setOnClickListener(v -> saveProfile());
        btnChangePassword.setOnClickListener(v -> openChangePasswordActivity());
    }

    private void openChangePasswordActivity() {
        Intent intent = new Intent(this, ChangePasswordActivity.class);
        startActivity(intent);
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        if (!validateInputs(firstName, ageStr, email, phone, gender)) {
            return;
        }

        int age = Integer.parseInt(ageStr);
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
        String contact = phone;

        updateProfileOnServer(fullName, email, age, gender.toLowerCase(), contact);
    }

    private boolean validateInputs(String firstName, String ageStr, String email,
                                   String phone, String gender) {
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return false;
        }

        if (ageStr.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        if (!isValidAge(ageStr)) {
            etAge.setError("Please enter a valid age");
            etAge.requestFocus();
            return false;
        }

        if (!isValidEmail(email)) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return false;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return false;
        }

        if (gender.equals("Gender")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidAge(String ageStr) {
        try {
            int age = Integer.parseInt(ageStr);
            return age >= MIN_AGE && age <= MAX_AGE;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private boolean isValidEmail(String email) {
        return !email.isEmpty() &&
                android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void updateProfileOnServer(String fullName, String email, int age,
                                       String gender, String contact) {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        UpdateUserRequest request = createUpdateRequest(fullName, email, age, gender, contact);
        setLoadingState(true);

        apiService.updateAuthenticatedUser(token, request).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                setLoadingState(false);

                if (response.isSuccessful() && response.body() != null) {
                    handleUpdateResponse(response.body());
                } else {
                    handleUpdateError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                setLoadingState(false);
                Toast.makeText(EditProfileActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private UpdateUserRequest createUpdateRequest(String fullName, String email, int age,
                                                  String gender, String contact) {
        UpdateUserRequest request = new UpdateUserRequest();
        request.setName(fullName);
        request.setEmail(email);
        request.setAge(age);
        request.setGender(gender);
        request.setPhoneNumber(contact);
        return request;
    }

    private void handleUpdateResponse(UserResponse userResponse) {
        if (userResponse.isStatus() && userResponse.getData() != null) {
            UserResponse.UserData userData = userResponse.getData();

            prefsManager.saveUserProfile(
                    userData.getName(),
                    userData.getEmail(),
                    userData.getAge(),
                    userData.getGender(),
                    userData.getPhoneNumber()
            );

            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            setResult(RESULT_OK);
            finish();
        } else {
            String message = userResponse.getMessage() != null && !userResponse.getMessage().isEmpty()
                    ? userResponse.getMessage()
                    : "Failed to update profile";
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleUpdateError(Response<UserResponse> response) {
        Toast.makeText(this, "Failed to update (Code: " + response.code() + ")",
                Toast.LENGTH_SHORT).show();
    }

    private void setLoadingState(boolean isLoading) {
        btnConfirm.setEnabled(!isLoading);
        btnConfirm.setText(isLoading ? "Updating..." : "Confirm");
    }
}