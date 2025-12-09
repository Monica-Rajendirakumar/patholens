package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.widget.SwitchCompat;
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

    // Where are the sores/blisters located in your mouth?
    private CheckBox cbInnerCheeks, cbRoofOfMouth, cbInsideOfLips, cbTongue, cbGums, cbFloorOfMouth;

    // Are there sores in other areas?
    private CheckBox cbOtherAreas;
    private TextInputEditText etOtherAreasSpecify;

    private TextInputEditText etWhenStarted;

    // Symptoms
    private SwitchCompat switchPainfulSores, switchBleedingEasily, switchDifficultyEating, switchDifficultySwallowing;
    private TextInputEditText etOtherSymptoms;

    // Medical history
    private TextInputEditText etMedicalConditions, etCurrentMedications, etAllergies;

    // Nikolsky's sign (if available)
    private RadioGroup rgNikolskySign;
    private RadioButton rbNikolskyPositive, rbNikolskyNegative, rbNikolskyNotMentioned;

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

        // Oral locations
        cbInnerCheeks = findViewById(R.id.cbInnerCheeks);
        cbRoofOfMouth = findViewById(R.id.cbRoofOfMouth);
        cbInsideOfLips = findViewById(R.id.cbInsideOfLips);
        cbTongue = findViewById(R.id.cbTongue);
        cbGums = findViewById(R.id.cbGums);
        cbFloorOfMouth = findViewById(R.id.cbFloorOfMouth);

        // Other areas
        cbOtherAreas = findViewById(R.id.cbOtherAreas);
        etOtherAreasSpecify = findViewById(R.id.etOtherAreasSpecify);

        etWhenStarted = findViewById(R.id.etWhenStarted);

        // Symptoms
        switchPainfulSores = findViewById(R.id.switchPainfulSores);
        switchBleedingEasily = findViewById(R.id.switchBleedingEasily);
        switchDifficultyEating = findViewById(R.id.switchDifficultyEating);
        switchDifficultySwallowing = findViewById(R.id.switchDifficultySwallowing);
        etOtherSymptoms = findViewById(R.id.etOtherSymptoms);

        // Medical history
        etMedicalConditions = findViewById(R.id.etMedicalConditions);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etAllergies = findViewById(R.id.etAllergies);

        // Nikolsky's sign
        rgNikolskySign = findViewById(R.id.rgNikolskySign);
        rbNikolskyPositive = findViewById(R.id.rbNikolskyPositive);
        rbNikolskyNegative = findViewById(R.id.rbNikolskyNegative);
        rbNikolskyNotMentioned = findViewById(R.id.rbNikolskyNotMentioned);

        btnNext = findViewById(R.id.btnNext);
    }

    private void setupListeners() {
        // Enable/disable "Other Areas" text field based on checkbox
        cbOtherAreas.setOnCheckedChangeListener((buttonView, isChecked) -> {
            etOtherAreasSpecify.setEnabled(isChecked);
            if (!isChecked) {
                etOtherAreasSpecify.setText("");
            }
        });

        btnNext.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToImageUpload();
            }
        });
    }

    private boolean validateFields() {
        if (etPrimaryConcern.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please describe what you're experiencing", Toast.LENGTH_SHORT).show();
            etPrimaryConcern.requestFocus();
            return false;
        }

        if (!isAnyLocationSelected()) {
            Toast.makeText(this, "Please select where the sores/blisters are located", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Validate "Other Areas" specification if checkbox is selected
        if (cbOtherAreas.isChecked() && etOtherAreasSpecify.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Please specify where else the sores are located", Toast.LENGTH_SHORT).show();
            etOtherAreasSpecify.requestFocus();
            return false;
        }

        return true;
    }

    private boolean isAnyLocationSelected() {
        return cbInnerCheeks.isChecked() || cbRoofOfMouth.isChecked() ||
                cbInsideOfLips.isChecked() || cbTongue.isChecked() ||
                cbGums.isChecked() || cbFloorOfMouth.isChecked() ||
                cbOtherAreas.isChecked();
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
        intent.putExtra("affectedLocations", getSelectedLocations());
        intent.putExtra("whenStarted", etWhenStarted.getText().toString().trim());

        intent.putExtra("painfulSores", switchPainfulSores.isChecked());
        intent.putExtra("bleedingEasily", switchBleedingEasily.isChecked());
        intent.putExtra("difficultyEating", switchDifficultyEating.isChecked());
        intent.putExtra("difficultySwallowing", switchDifficultySwallowing.isChecked());
        intent.putExtra("otherSymptoms", etOtherSymptoms.getText().toString().trim());

        intent.putExtra("medicalConditions", etMedicalConditions.getText().toString().trim());
        intent.putExtra("currentMedications", etCurrentMedications.getText().toString().trim());
        intent.putExtra("allergies", etAllergies.getText().toString().trim());

        // Nikolsky's sign
        intent.putExtra("nikolskySign", getNikolskySignValue());
    }

    private String getSelectedLocations() {
        StringBuilder locations = new StringBuilder();

        // Oral cavity locations
        appendLocationIfChecked(locations, cbInnerCheeks, "Inner cheeks");
        appendLocationIfChecked(locations, cbRoofOfMouth, "Roof of the mouth");
        appendLocationIfChecked(locations, cbInsideOfLips, "Inside of the lips");
        appendLocationIfChecked(locations, cbTongue, "Tongue");
        appendLocationIfChecked(locations, cbGums, "Gums");
        appendLocationIfChecked(locations, cbFloorOfMouth, "Floor of the mouth");

        // Other areas
        if (cbOtherAreas.isChecked()) {
            String otherAreasText = etOtherAreasSpecify.getText().toString().trim();
            if (!otherAreasText.isEmpty()) {
                locations.append("Other areas: ").append(otherAreasText).append(", ");
            }
        }

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

    private String getNikolskySignValue() {
        int selectedId = rgNikolskySign.getCheckedRadioButtonId();

        if (selectedId == rbNikolskyPositive.getId()) {
            return "Positive";
        } else if (selectedId == rbNikolskyNegative.getId()) {
            return "Negative";
        } else {
            return "Not mentioned";
        }
    }
}