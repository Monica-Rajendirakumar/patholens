package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class QuestionnaireActivity extends AppCompatActivity {

    private RadioGroup rgSkinLesions, rgPainLevel, rgLesionDuration;
    private CheckBox cbBlisters, cbItching, cbBurning, cbPain, cbRedness;
    private Spinner spinnerLesionLocation;
    private TextInputEditText etMedicalHistory, etCurrentMedications, etAllergies;
    private Button btnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        initializeViews();
        setupSpinner();
        setupListeners();
    }

    private void initializeViews() {
        rgSkinLesions = findViewById(R.id.rgSkinLesions);
        rgPainLevel = findViewById(R.id.rgPainLevel);
        rgLesionDuration = findViewById(R.id.rgLesionDuration);

        cbBlisters = findViewById(R.id.cbBlisters);
        cbItching = findViewById(R.id.cbItching);
        cbBurning = findViewById(R.id.cbBurning);
        cbPain = findViewById(R.id.cbPain);
        cbRedness = findViewById(R.id.cbRedness);

        spinnerLesionLocation = findViewById(R.id.spinnerLesionLocation);

        etMedicalHistory = findViewById(R.id.etMedicalHistory);
        etCurrentMedications = findViewById(R.id.etCurrentMedications);
        etAllergies = findViewById(R.id.etAllergies);

        btnNext = findViewById(R.id.btnNext);
    }

    private void setupSpinner() {
        String[] locations = {
                "Select Location",
                "Face",
                "Scalp",
                "Chest",
                "Back",
                "Arms",
                "Legs",
                "Hands",
                "Feet",
                "Oral cavity (mouth)",
                "Multiple locations"
        };

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                locations
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLesionLocation.setAdapter(adapter);
    }

    private void setupListeners() {
        btnNext.setOnClickListener(v -> {
            if (validateFields()) {
                navigateToImageUpload();
            }
        });
    }

    private boolean validateFields() {
        if (rgSkinLesions.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please answer if you have active skin lesions", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!cbBlisters.isChecked() && !cbItching.isChecked() &&
                !cbBurning.isChecked() && !cbPain.isChecked() && !cbRedness.isChecked()) {
            Toast.makeText(this, "Please select at least one symptom", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (spinnerLesionLocation.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Please select lesion location", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgLesionDuration.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please select lesion duration", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (rgPainLevel.getCheckedRadioButtonId() == -1) {
            Toast.makeText(this, "Please rate the pain level", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void navigateToImageUpload() {
        Intent intent = new Intent(QuestionnaireActivity.this, ImageUploadActivity.class);

        // Pass questionnaire data
        intent.putExtra("hasLesions", getRadioGroupAnswer(rgSkinLesions));
        intent.putExtra("symptoms", getSelectedSymptoms());
        intent.putExtra("lesionLocation", spinnerLesionLocation.getSelectedItem().toString());
        intent.putExtra("lesionDuration", getRadioGroupAnswer(rgLesionDuration));
        intent.putExtra("painLevel", getRadioGroupAnswer(rgPainLevel));
        intent.putExtra("medicalHistory", etMedicalHistory.getText().toString());
        intent.putExtra("currentMedications", etCurrentMedications.getText().toString());
        intent.putExtra("allergies", etAllergies.getText().toString());

        startActivity(intent);
    }

    private String getRadioGroupAnswer(RadioGroup rg) {
        int selectedId = rg.getCheckedRadioButtonId();
        if (selectedId == -1) return "";
        RadioButton rb = findViewById(selectedId);
        return rb.getText().toString();
    }

    private String getSelectedSymptoms() {
        StringBuilder symptoms = new StringBuilder();

        if (cbBlisters.isChecked()) symptoms.append("Blisters, ");
        if (cbItching.isChecked()) symptoms.append("Itching, ");
        if (cbBurning.isChecked()) symptoms.append("Burning, ");
        if (cbPain.isChecked()) symptoms.append("Pain, ");
        if (cbRedness.isChecked()) symptoms.append("Redness, ");

        if (symptoms.length() > 0) {
            symptoms.setLength(symptoms.length() - 2); // Remove last comma and space
        }

        return symptoms.toString();
    }
}