package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class PatientInformationActivity extends AppCompatActivity {

    private Spinner spinnerPatientType;
    private LinearLayout layoutPatientDetails;
    private TextInputLayout tilName, tilAge, tilPhone, tilCountry, tilState, tilPincode;
    private TextInputEditText etName, etAge, etPhone, etCountry, etState, etPincode;
    private RadioGroup rgGender;
    private Button btnNext;

    private boolean isForMe = true;
    private String currentUserName = "John Doe"; // Simulated logged-in user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_information);

        initializeViews();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        spinnerPatientType = findViewById(R.id.spinnerPatientType);
        layoutPatientDetails = findViewById(R.id.layoutPatientDetails);
        tilName = findViewById(R.id.tilName);
        tilAge = findViewById(R.id.tilAge);
        tilPhone = findViewById(R.id.tilPhone);
        tilCountry = findViewById(R.id.tilCountry);
        tilState = findViewById(R.id.tilState);
        tilPincode = findViewById(R.id.tilPincode);
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etPhone = findViewById(R.id.etPhone);
        etCountry = findViewById(R.id.etCountry);
        etState = findViewById(R.id.etState);
        etPincode = findViewById(R.id.etPincode);
        rgGender = findViewById(R.id.rgGender);
        btnNext = findViewById(R.id.btnNext);
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
                    // Auto-fill with current user data
                    etName.setText(currentUserName);
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

    private boolean validateFields() {
        if (isForMe) {
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
            tilAge.setError(null);
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

        if (etCountry.getText().toString().trim().isEmpty()) {
            tilCountry.setError("Country is required");
            isValid = false;
        } else {
            tilCountry.setError(null);
        }

        if (etState.getText().toString().trim().isEmpty()) {
            tilState.setError("State is required");
            isValid = false;
        } else {
            tilState.setError(null);
        }

        if (etPincode.getText().toString().trim().isEmpty()) {
            tilPincode.setError("Pincode is required");
            isValid = false;
        } else {
            tilPincode.setError(null);
        }

        return isValid;
    }

    private void clearFields() {
        etName.setText("");
        etAge.setText("");
        etPhone.setText("");
        etCountry.setText("");
        etState.setText("");
        etPincode.setText("");
        rgGender.clearCheck();
    }

    private void navigateToQuestionnaire() {
        Intent intent = new Intent(PatientInformationActivity.this, QuestionnaireActivity.class);

        // Pass patient data
        intent.putExtra("isForMe", isForMe);
        intent.putExtra("patientName", isForMe ? currentUserName : etName.getText().toString());
        intent.putExtra("patientAge", isForMe ? "30" : etAge.getText().toString());
        intent.putExtra("patientPhone", isForMe ? "1234567890" : etPhone.getText().toString());
        intent.putExtra("patientGender", getSelectedGender());
        intent.putExtra("patientCountry", isForMe ? "USA" : etCountry.getText().toString());
        intent.putExtra("patientState", isForMe ? "California" : etState.getText().toString());
        intent.putExtra("patientPincode", isForMe ? "90001" : etPincode.getText().toString());

        startActivity(intent);
    }

    private String getSelectedGender() {
        if (isForMe) return "Male";

        int selectedId = rgGender.getCheckedRadioButtonId();
        if (selectedId == -1) return "";

        RadioButton rb = findViewById(selectedId);
        return rb.getText().toString();
    }
}