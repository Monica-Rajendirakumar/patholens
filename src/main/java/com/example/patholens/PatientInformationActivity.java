package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import com.example.patholens.api.ApiService;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientInformationActivity extends AppCompatActivity {

    private Spinner spinnerPatientType;
    private LinearLayout layoutPatientDetails;
    private TextInputLayout tilName, tilAge, tilPhone;
    private TextInputEditText etName, etAge, etPhone;
    private RadioGroup rgGender;
    private Button btnNext;
    private ProgressBar progressBar;

    private boolean isForMe = true;

    // User data
    private String currentUserId = null;
    private String currentUserName = null;
    private String currentUserAge = null;
    private String currentUserPhone = null;
    private String currentUserGender = null;

    // PrefsManager
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_information);

        // Initialize PrefsManager
        prefsManager = new PrefsManager(this);

        initializeViews();
        loadUserData();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        spinnerPatientType = findViewById(R.id.spinnerPatientType);
        layoutPatientDetails = findViewById(R.id.layoutPatientDetails);
        tilName = findViewById(R.id.tilName);
        tilAge = findViewById(R.id.tilAge);
        tilPhone = findViewById(R.id.tilPhone);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        rgGender = findViewById(R.id.rgGender);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
    }

    /**
     * ✅ CHANGE POINT #1: Load authenticated user data from backend
     * Calls GET /api/v1/me to fetch complete user details
     */
    private void loadUserData() {
        // Check if user is logged in
        if (!prefsManager.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        // Get basic info from PrefsManager (saved during login)
        currentUserId = String.valueOf(prefsManager.getUserId());
        currentUserName = prefsManager.getUserName();

        // Fetch complete profile from backend (includes age, gender, phone)
        showLoading(true);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        Call<UserResponse> call = apiService.getAuthenticatedUser(prefsManager.getBearerToken());

        call.enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                showLoading(false);

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();

                    if (userResponse.isStatus() && userResponse.getData() != null) {
                        UserResponse.UserData userData = userResponse.getData();

                        currentUserId = String.valueOf(userData.getId());
                        currentUserName = userData.getName() != null ? userData.getName() : "";
                        currentUserAge = userData.getAge() != null ? String.valueOf(userData.getAge()) : "";
                        currentUserPhone = userData.getPhoneNumber() != null ? userData.getPhoneNumber() : "";
                        currentUserGender = userData.getGender() != null ? userData.getGender() : "";

                        // Save to PrefsManager for future use
                        if (userData.getAge() != null && userData.getGender() != null && userData.getPhoneNumber() != null) {
                            prefsManager.saveUserProfile(
                                    currentUserName,
                                    userData.getEmail() != null ? userData.getEmail() : "",
                                    userData.getAge(),
                                    currentUserGender,
                                    currentUserPhone
                            );
                        }

                        // Auto-fill if "For Me" is selected
                        if (isForMe) {
                            etName.setText(currentUserName);
                        }
                    } else {
                        Toast.makeText(PatientInformationActivity.this,
                                "Failed to load user data", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(PatientInformationActivity.this,
                            "Failed to fetch user data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                showLoading(false);
                Toast.makeText(PatientInformationActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupSpinner() {
        String[] patientTypes = {"For Me", "For Others"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                patientTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPatientType.setAdapter(adapter);
    }

    private void setupListeners() {
        spinnerPatientType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) { // For Me
                    isForMe = true;
                    layoutPatientDetails.setVisibility(View.GONE);
                    // Auto-fill with current user data from backend
                    if (currentUserName != null) {
                        etName.setText(currentUserName);
                    }
                } else { // For Others
                    isForMe = false;
                    layoutPatientDetails.setVisibility(View.VISIBLE);
                    clearFields();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        btnNext.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToQuestionnaire();
            }
        });
    }

    /**
     * ✅ CHANGE POINT #2: Simplified validation - only name, age, phone, gender
     */
    private boolean validateFields() {
        if (isForMe) {
            // For "For Me", ensure user data is loaded
            if (currentUserName == null || currentUserName.isEmpty()) {
                Toast.makeText(this, "User data not loaded. Please try again.", Toast.LENGTH_SHORT).show();
                return false;
            }
            return true;
        }

        boolean isValid = true;

        if (etName.getText().toString().trim().isEmpty()) {
            tilName.setError("Name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (etAge.getText().toString().trim().isEmpty()) {
            tilAge.setError("Age is required");
            isValid = false;
        } else {
            try {
                int age = Integer.parseInt(etAge.getText().toString().trim());
                if (age < 0 || age > 150) {
                    tilAge.setError("Age must be between 0 and 150");
                    isValid = false;
                } else {
                    tilAge.setError(null);
                }
            } catch (NumberFormatException e) {
                tilAge.setError("Invalid age");
                isValid = false;
            }
        }

        if (etPhone.getText().toString().trim().isEmpty()) {
            tilPhone.setError("Phone number is required");
            isValid = false;
        } else if (etPhone.getText().toString().trim().length() < 10) {
            tilPhone.setError("Invalid phone number");
            isValid = false;
        } else {
            tilPhone.setError(null);
        }

        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            isValid = false;
        }

        return isValid;
    }

    private void clearFields() {
        etName.setText("");
        etAge.setText("");
        etPhone.setText("");
        rgGender.clearCheck();
    }

    /**
     * ✅ CHANGE POINT #3: Pass patient data to QuestionnaireActivity
     * QuestionnaireActivity will create the patient record after diagnosis
     */
    private void navigateToQuestionnaire() {
        Intent intent = new Intent(PatientInformationActivity.this, QuestionnaireActivity.class);

        // Pass user_id for patient record
        intent.putExtra("user_id", currentUserId);
        intent.putExtra("isForMe", isForMe);

        if (isForMe) {
            // Use current user's data from backend
            intent.putExtra("patientName", currentUserName);
            intent.putExtra("patientAge", currentUserAge);
            intent.putExtra("patientPhone", currentUserPhone);
            intent.putExtra("patientGender", currentUserGender);
        } else {
            // Use entered data for others
            intent.putExtra("patientName", etName.getText().toString().trim());
            intent.putExtra("patientAge", etAge.getText().toString().trim());
            intent.putExtra("patientPhone", etPhone.getText().toString().trim());
            intent.putExtra("patientGender", getSelectedGender());
        }

        startActivity(intent);
    }

    private String getSelectedGender() {
        if (isForMe && currentUserGender != null && !currentUserGender.isEmpty()) {
            return currentUserGender;
        }

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == -1) return "";

        RadioButton rb = findViewById(selectedId);
        return rb.getText().toString().toLowerCase();
    }

    /**
     * ✅ CHANGE POINT #4: Loading indicator management
     */
    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnNext.setEnabled(!show);
    }
}