package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

public class QuestionnaireActivity extends AppCompatActivity {

    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;

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

        receivePatientInformation();
        initializeViews();
        setupListeners();
    }

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
        etPrimaryConcern = findViewById(R.id.etPrimaryConcern);

        cbTongue = findViewById(R.id.cbTongue);
        cbGingiva = findViewById(R.id.cbGingiva);
        cbFloorOfMouth = findViewById(R.id.cbFloorOfMouth);
        cbBuccalMucosa = findViewById(R.id.cbBuccalMucosa);
        cbPalate = findViewById(R.id.cbPalate);
        cbLips = findViewById(R.id.cbLips);

        etLesionOnsetDate = findViewById(R.id.etLesionOnsetDate);

        seekBarPainLevel = findViewById(R.id.seekBarPainLevel);
        tvPainLevel = findViewById(R.id.tvPainLevel);

        switchBleeding = findViewById(R.id.switchBleeding);
        switchSwelling = findViewById(R.id.switchSwelling);
        switchFever = findViewById(R.id.switchFever);
        etOtherSymptoms = findViewById(R.id.etOtherSymptoms);

        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etAllergies = findViewById(R.id.etAllergies);
        switchSmokingHistory = findViewById(R.id.switchSmokingHistory);

        btnNext = findViewById(R.id.btnNext);
    }

    private void setupListeners() {
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
        if (etPrimaryConcern.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please describe your primary concern", Toast.LENGTH_SHORT).show();
            etPrimaryConcern.requestFocus();
            return false;
        }

        if (!isAnyOralLocationSelected()) {
            Toast.makeText(this, "Please select at least one affected oral location", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isAnyOralLocationSelected() {
        return cbTongue.isChecked() || cbGingiva.isChecked() ||
                cbFloorOfMouth.isChecked() || cbBuccalMucosa.isChecked() ||
                cbPalate.isChecked() || cbLips.isChecked();
    }

    private void navigateToImageUpload() {
        Intent intent = new Intent(this, ImageUploadActivity.class);

        addPatientInformation(intent);
        addQuestionnaireData(intent);

        startActivity(intent);
    }

    private void addPatientInformation(Intent intent) {
        intent.putExtra("user_id", userId);
        intent.putExtra("isForMe", isForMe);
        intent.putExtra("patientName", patientName);
        intent.putExtra("patientAge", patientAge);
        intent.putExtra("patientPhone", patientPhone);
        intent.putExtra("patientGender", patientGender);
    }

    private void addQuestionnaireData(Intent intent) {
        intent.putExtra("primaryConcern", etPrimaryConcern.getText().toString().trim());
        intent.putExtra("affectedLocations", getSelectedOralLocations());
        intent.putExtra("lesionOnsetDate", etLesionOnsetDate.getText().toString().trim());
        intent.putExtra("painLevel", seekBarPainLevel.getProgress());

        intent.putExtra("bleeding", switchBleeding.isChecked());
        intent.putExtra("swelling", switchSwelling.isChecked());
        intent.putExtra("fever", switchFever.isChecked());
        intent.putExtra("otherSymptoms", etOtherSymptoms.getText().toString().trim());

        intent.putExtra("medicalConditions", etMedicalConditions.getText().toString().trim());
        intent.putExtra("currentMedications", etCurrentMedications.getText().toString().trim());
        intent.putExtra("allergies", etAllergies.getText().toString().trim());
        intent.putExtra("smokingHistory", switchSmokingHistory.isChecked());
    }

    private String getSelectedOralLocations() {
        StringBuilder locations = new StringBuilder();

        appendLocationIfChecked(locations, cbTongue, "Tongue");
        appendLocationIfChecked(locations, cbGingiva, "Gingiva");
        appendLocationIfChecked(locations, cbFloorOfMouth, "Floor of Mouth");
        appendLocationIfChecked(locations, cbBuccalMucosa, "Buccal Mucosa");
        appendLocationIfChecked(locations, cbPalate, "Palate");
        appendLocationIfChecked(locations, cbLips, "Lips");

        return removeTrailingComma(locations);
    }

    private void appendLocationIfChecked(StringBuilder builder, CheckBox checkBox, String location) {
        if (checkBox.isChecked()) {
            builder.append(location).append(", ");
        }
    }

    private String removeTrailingComma(StringBuilder builder) {
        if (builder.length() > 0) {
            builder.setLength(builder.length() - 2);
        }
        return builder.toString();
    }
}