package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class QuestionnaireActivity extends AppCompatActivity {

    // Patient information from previous activity
    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;

    // New UI components based on JSON
    private TextInputEditText etPrimaryConcern;
    private CheckBox cbTongue, cbGingiva, cbFloorOfMouth, cbBuccalMucosa, cbPalate, cbLips;
    private TextInputEditText etLesionOnsetDate;
    private SeekBar seekBarPainLevel;
    private TextView tvPainLevel;
    private Switch switchBleeding, switchSwelling, switchFever, switchSmokingHistory;
    private TextInputEditText etOtherSymptoms, etMedicalConditions, etCurrentMedications, etAllergies;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        // Receive patient information from PatientInformationActivity
        receivePatientInformation();

        initializeViews();
        setupListeners();
    }

    /**
     * Receive all patient information passed from PatientInformationActivity
     */
    private void receivePatientInformation() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        isForMe = intent.getBooleanExtra("isForMe", true);
        patientName = intent.getStringExtra("patientName");
        patientAge = intent.getStringExtra("patientAge");
        patientPhone = intent.getStringExtra("patientPhone");
        patientGender = intent.getStringExtra("patientGender");
    }

    private void initializeViews() {
        // Chief Complaint
        etPrimaryConcern = findViewById(R.id.etPrimaryConcern);

        // Oral Lesions - Affected Locations
        cbTongue = findViewById(R.id.cbTongue);
        cbGingiva = findViewById(R.id.cbGingiva);
        cbFloorOfMouth = findViewById(R.id.cbFloorOfMouth);
        cbBuccalMucosa = findViewById(R.id.cbBuccalMucosa);
        cbPalate = findViewById(R.id.cbPalate);
        cbLips = findViewById(R.id.cbLips);

        // Lesion onset date
        etLesionOnsetDate = findViewById(R.id.etLesionOnsetDate);

        // Pain level slider
        seekBarPainLevel = findViewById(R.id.seekBarPainLevel);
        tvPainLevel = findViewById(R.id.tvPainLevel);

        // Associated Symptoms
        switchBleeding = findViewById(R.id.switchBleeding);
        switchSwelling = findViewById(R.id.switchSwelling);
        switchFever = findViewById(R.id.switchFever);
        etOtherSymptoms = findViewById(R.id.etOtherSymptoms);

        // Medical History
        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etAllergies = findViewById(R.id.etAllergies);
        switchSmokingHistory = findViewById(R.id.switchSmokingHistory);

        btnNext = findViewById(R.id.btnNext);
    }

    private void setupListeners() {
        // Pain level slider
        seekBarPainLevel.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvPainLevel.setText("Pain Level: " + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        btnNext.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToImageUpload();
            }
        });
    }

    private boolean validateFields() {
        // Validate Chief Complaint (Required)
        if (etPrimaryConcern.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please describe your primary concern", Toast.LENGTH_SHORT).show();
            etPrimaryConcern.requestFocus();
            return false;
        }

        // Validate at least one affected oral location is selected (Required)
        if (!cbTongue.isChecked() && !cbGingiva.isChecked() &&
                !cbFloorOfMouth.isChecked() && !cbBuccalMucosa.isChecked() &&
                !cbPalate.isChecked() && !cbLips.isChecked()) {
            Toast.makeText(this, "Please select at least one affected oral location", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToImageUpload() {
        Intent intent = new Intent(QuestionnaireActivity.this, ImageUploadActivity.class);

        // ✅ Pass through patient information unchanged
        intent.putExtra("user_id", userId);
        intent.putExtra("isForMe", isForMe);
        intent.putExtra("patientName", patientName);
        intent.putExtra("patientAge", patientAge);
        intent.putExtra("patientPhone", patientPhone);
        intent.putExtra("patientGender", patientGender);

        // Pass questionnaire data
        intent.putExtra("primaryConcern", etPrimaryConcern.getText().toString().trim());
        intent.putExtra("affectedLocations", getSelectedOralLocations());
        intent.putExtra("lesionOnsetDate", etLesionOnsetDate.getText().toString().trim());
        intent.putExtra("painLevel", seekBarPainLevel.getProgress());

        // Associated symptoms
        intent.putExtra("bleeding", switchBleeding.isChecked());
        intent.putExtra("swelling", switchSwelling.isChecked());
        intent.putExtra("fever", switchFever.isChecked());
        intent.putExtra("otherSymptoms", etOtherSymptoms.getText().toString().trim());

        // Medical history
        intent.putExtra("medicalConditions", etMedicalConditions.getText().toString().trim());
        intent.putExtra("currentMedications", etCurrentMedications.getText().toString().trim());
        intent.putExtra("allergies", etAllergies.getText().toString().trim());
        intent.putExtra("smokingHistory", switchSmokingHistory.isChecked());

        startActivity(intent);
    }

    private String getSelectedOralLocations() {
        StringBuilder locations = new StringBuilder();

        if (cbTongue.isChecked()) locations.append("Tongue, ");
        if (cbGingiva.isChecked()) locations.append("Gingiva, ");
        if (cbFloorOfMouth.isChecked()) locations.append("Floor of Mouth, ");
        if (cbBuccalMucosa.isChecked()) locations.append("Buccal Mucosa, ");
        if (cbPalate.isChecked()) locations.append("Palate, ");
        if (cbLips.isChecked()) locations.append("Lips, ");

        if (locations.length() > 0) {
            locations.setLength(locations.length() - 2); // Remove last comma and space
        }

        return locations.toString();
    }
}