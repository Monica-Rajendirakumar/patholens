package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DiagnosisReportActivity extends AppCompatActivity {

    private static final int HIGH_CONFIDENCE_THRESHOLD = 90;

    private ImageView btnBack;
    private ImageView btnShare;
    private ImageView btnDownload;
    private TextView tvDiagnosisTitle;
    private TextView tvPatientName;
    private TextView tvPatientAge;
    private TextView tvPatientGender;
    private TextView tvDiagnosisDate;
    private TextView tvDiagnosisResult;
    private TextView tvConfidenceScore;
    private TextView tvConfidencePercentage;
    private TextView tvRecommendations;
    private TextView tvNotes;
    private CardView cardPatientInfo;
    private CardView cardDiagnosisInfo;
    private CardView cardRecommendations;

    private String diagnosisId;
    private String patientName;
    private String gender;
    private String date;
    private int age;
    private int confidence;
    private boolean isPemphigus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_report);

        initializeViews();
        loadDataFromIntent();
        displayReportData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnShare = findViewById(R.id.btnShare);
        btnDownload = findViewById(R.id.btnDownload);

        tvDiagnosisTitle = findViewById(R.id.tvDiagnosisTitle);
        tvPatientName = findViewById(R.id.tvPatientName);
        tvPatientAge = findViewById(R.id.tvPatientAge);
        tvPatientGender = findViewById(R.id.tvPatientGender);
        tvDiagnosisDate = findViewById(R.id.tvDiagnosisDate);
        tvDiagnosisResult = findViewById(R.id.tvDiagnosisResult);
        tvConfidenceScore = findViewById(R.id.tvConfidenceScore);
        tvConfidencePercentage = findViewById(R.id.tvConfidencePercentage);
        tvRecommendations = findViewById(R.id.tvRecommendations);
        tvNotes = findViewById(R.id.tvNotes);

        cardPatientInfo = findViewById(R.id.cardPatientInfo);
        cardDiagnosisInfo = findViewById(R.id.cardDiagnosisInfo);
        cardRecommendations = findViewById(R.id.cardRecommendations);
    }

    private void loadDataFromIntent() {
        Intent intent = getIntent();
        diagnosisId = intent.getStringExtra("diagnosis_id");
        patientName = intent.getStringExtra("name");
        age = intent.getIntExtra("age", 0);
        isPemphigus = intent.getBooleanExtra("pemphigus", false);
        date = intent.getStringExtra("date");
        confidence = intent.getIntExtra("confidence", 0);
        gender = intent.getStringExtra("gender");
    }

    private void displayReportData() {
        displayDiagnosisTitle();
        displayPatientInformation();
        displayDiagnosisInformation();
        displayRecommendationsAndNotes();
    }

    private void displayDiagnosisTitle() {
        String title = diagnosisId != null ? diagnosisId : "Diagnosis Report";
        tvDiagnosisTitle.setText(title);
    }

    private void displayPatientInformation() {
        tvPatientName.setText(patientName != null ? patientName : "N/A");
        tvPatientAge.setText(age + " years");
        tvPatientGender.setText(gender != null ? capitalizeFirst(gender) : "N/A");
    }

    private void displayDiagnosisInformation() {
        tvDiagnosisDate.setText(date != null ? date : "N/A");

        String resultText = isPemphigus ? "Pemphigus Detected" : "No Pemphigus Detected";
        tvDiagnosisResult.setText(resultText);

        int resultColor = isPemphigus ?
                android.R.color.holo_red_dark :
                android.R.color.holo_green_dark;
        tvDiagnosisResult.setTextColor(getResources().getColor(resultColor));

        String confidenceText = confidence + "%";
        tvConfidenceScore.setText(confidenceText);
        tvConfidencePercentage.setText("Confidence: " + confidenceText);
    }

    private void displayRecommendationsAndNotes() {
        String recommendations = getRecommendations(isPemphigus, confidence);
        tvRecommendations.setText(recommendations);

        String notes = getClinicalNotes(isPemphigus);
        tvNotes.setText(notes);
    }

    private String getRecommendations(boolean isPemphigus, int confidence) {
        if (isPemphigus) {
            return getPositiveDiagnosisRecommendations(confidence);
        } else {
            return getNegativeDiagnosisRecommendations();
        }
    }

    private String getPositiveDiagnosisRecommendations(int confidence) {
        if (confidence >= HIGH_CONFIDENCE_THRESHOLD) {
            return "• Immediate consultation with a dermatologist is strongly recommended\n" +
                    "• Consider starting corticosteroid therapy\n" +
                    "• Perform biopsy for confirmation\n" +
                    "• Monitor for secondary infections\n" +
                    "• Regular follow-up appointments required";
        } else {
            return "• Consult with a dermatologist for further evaluation\n" +
                    "• Additional diagnostic tests may be required\n" +
                    "• Monitor symptoms closely\n" +
                    "• Schedule follow-up examination";
        }
    }

    private String getNegativeDiagnosisRecommendations() {
        return "• Continue routine skin care practices\n" +
                "• Monitor for any changes in skin condition\n" +
                "• Schedule regular check-ups as needed\n" +
                "• Maintain good hygiene practices";
    }

    private String getClinicalNotes(boolean isPemphigus) {
        if (isPemphigus) {
            return "Pemphigus is an autoimmune disorder that causes blistering of the skin and mucous membranes. " +
                    "Early diagnosis and treatment are crucial for better outcomes. This AI-assisted diagnosis should " +
                    "be confirmed by a qualified healthcare professional through clinical examination and laboratory tests.";
        } else {
            return "No signs of pemphigus detected in the analysis. However, this AI-assisted diagnosis should not " +
                    "replace professional medical consultation. If you experience any concerning symptoms, please consult " +
                    "a healthcare provider for proper evaluation.";
        }
    }

    private String capitalizeFirst(String str) {
        if (str == null || str.isEmpty()) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
        btnShare.setOnClickListener(v -> shareReport());
        btnDownload.setOnClickListener(v -> downloadReport());
    }

    private void shareReport() {
        Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show();

        // Example implementation:
        // Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // shareIntent.setType("text/plain");
        // shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Diagnosis Report");
        // shareIntent.putExtra(Intent.EXTRA_TEXT, getReportText());
        // startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }

    private void downloadReport() {
        Toast.makeText(this, "Download feature coming soon!", Toast.LENGTH_SHORT).show();
    }

    private String getReportText() {
        return "Diagnosis Report\n\n" +
                "Patient: " + patientName + "\n" +
                "Age: " + age + " years\n" +
                "Gender: " + gender + "\n" +
                "Date: " + date + "\n\n" +
                "Result: " + (isPemphigus ? "Pemphigus Detected" : "No Pemphigus Detected") + "\n" +
                "Confidence: " + confidence + "%\n";
    }
}