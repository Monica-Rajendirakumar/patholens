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

import com.example.patholens.api.ApiService;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PatientInformationActivity extends AppCompatActivity {

    private static final int MIN_AGE = 0;
    private static final int MAX_AGE = 150;
    private static final int MIN_PHONE_LENGTH = 10;

    private Spinner spinnerPatientType;
    private LinearLayout layoutPatientDetails;
    private TextInputLayout tilName, tilAge, tilPhone;
    private TextInputEditText etName, etAge, etPhone;
    private RadioGroup rgGender;
    private Button btnNext;
    private ProgressBar progressBar;

    private boolean isForMe = true;

    private String currentUserId = null;
    private String currentUserName = null;
    private String currentUserAge = null;
    private String currentUserPhone = null;
    private String currentUserGender = null;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_information);

        prefsManager = new PrefsManager(this);

        initializeViews();
        setupSpinner();
        setupListeners();
        loadUserData();
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
                isForMe = (position == 0);

                if (isForMe) {
                    layoutPatientDetails.setVisibility(View.GONE);
                    autoFillUserData();
                } else {
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

    private void loadUserData() {
        if (!prefsManager.isLoggedIn()) {
            redirectToLogin();
            return;
        }

        loadBasicUserInfo();
        fetchCompleteUserProfile();
    }

    private void redirectToLogin() {
        Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void loadBasicUserInfo() {
        currentUserId = String.valueOf(prefsManager.getUserId());
        currentUserName = prefsManager.getUserName();
    }

    private void fetchCompleteUserProfile() {
        showLoading(true);

        ApiService apiService = RetrofitClient.getInstance().getApiService();
        apiService.getAuthenticatedUser(prefsManager.getBearerToken())
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            handleUserDataResponse(response.body());
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

    private void handleUserDataResponse(UserResponse userResponse) {
        if (userResponse.isStatus() && userResponse.getData() != null) {
            UserResponse.UserData userData = userResponse.getData();

            updateCurrentUserData(userData);
            saveUserProfileToPrefs(userData);
            autoFillUserData();
        } else {
            Toast.makeText(this, "Failed to load user data", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateCurrentUserData(UserResponse.UserData userData) {
        currentUserId = String.valueOf(userData.getId());
        currentUserName = userData.getName() != null ? userData.getName() : "";
        currentUserAge = userData.getAge() != null ? String.valueOf(userData.getAge()) : "";
        currentUserPhone = userData.getPhoneNumber() != null ? userData.getPhoneNumber() : "";
        currentUserGender = userData.getGender() != null ? userData.getGender() : "";
    }

    private void saveUserProfileToPrefs(UserResponse.UserData userData) {
        if (userData.getAge() != null && userData.getGender() != null && userData.getPhoneNumber() != null) {
            prefsManager.saveUserProfile(
                    currentUserName,
                    userData.getEmail() != null ? userData.getEmail() : "",
                    userData.getAge(),
                    currentUserGender,
                    currentUserPhone
            );
        }
    }

    private void autoFillUserData() {
        if (isForMe && currentUserName != null) {
            etName.setText(currentUserName);
        }
    }

    private boolean validateFields() {
        if (isForMe) {
            return validateForMeFields();
        }
        return validateForOthersFields();
    }

    private boolean validateForMeFields() {
        if (currentUserName == null || currentUserName.isEmpty()) {
            Toast.makeText(this, "User data not loaded. Please try again.", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private boolean validateForOthersFields() {
        boolean isValid = true;

        isValid &= validateName();
        isValid &= validateAge();
        isValid &= validatePhone();
        isValid &= validateGender();

        return isValid;
    }

    private boolean validateName() {
        if (etName.getText().toString().trim().isEmpty()) {
            tilName.setError("Name is required");
            return false;
        }
        tilName.setError(null);
        return true;
    }

    private boolean validateAge() {
        String ageStr = etAge.getText().toString().trim();

        if (ageStr.isEmpty()) {
            tilAge.setError("Age is required");
            return false;
        }

        try {
            int age = Integer.parseInt(ageStr);
            if (age < MIN_AGE || age > MAX_AGE) {
                tilAge.setError("Age must be between " + MIN_AGE + " and " + MAX_AGE);
                return false;
            }
            tilAge.setError(null);
            return true;
        } catch (NumberFormatException e) {
            tilAge.setError("Invalid age");
            return false;
        }
    }

    private boolean validatePhone() {
        String phone = etPhone.getText().toString().trim();

        if (phone.isEmpty()) {
            tilPhone.setError("Phone number is required");
            return false;
        }

        if (phone.length() < MIN_PHONE_LENGTH) {
            tilPhone.setError("Invalid phone number");
            return false;
        }

        tilPhone.setError(null);
        return true;
    }

    private boolean validateGender() {
        if (rgGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select gender", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void clearFields() {
        etName.setText("");
        etAge.setText("");
        etPhone.setText("");
        rgGender.clearCheck();
    }

    private void navigateToQuestionnaire() {
        Intent intent = new Intent(this, QuestionnaireActivity.class);

        intent.putExtra("user_id", currentUserId);
        intent.putExtra("isForMe", isForMe);

        if (isForMe) {
            addCurrentUserData(intent);
        } else {
            addEnteredPatientData(intent);
        }

        startActivity(intent);
    }

    private void addCurrentUserData(Intent intent) {
        intent.putExtra("patientName", currentUserName);
        intent.putExtra("patientAge", currentUserAge);
        intent.putExtra("patientPhone", currentUserPhone);
        intent.putExtra("patientGender", currentUserGender);
    }

    private void addEnteredPatientData(Intent intent) {
        intent.putExtra("patientName", etName.getText().toString().trim());
        intent.putExtra("patientAge", etAge.getText().toString().trim());
        intent.putExtra("patientPhone", etPhone.getText().toString().trim());
        intent.putExtra("patientGender", getSelectedGender());
    }

    private String getSelectedGender() {
        if (isForMe && currentUserGender != null && !currentUserGender.isEmpty()) {
            return currentUserGender;
        }

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == -1) {
            return "";
        }

        RadioButton rb = findViewById(selectedId);
        return rb.getText().toString().toLowerCase();
    }

    private void showLoading(boolean show) {
        if (progressBar != null) {
            progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        }
        btnNext.setEnabled(!show);
    }
}