package com.example.patholens;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class DiagnosisReportActivity extends AppCompatActivity {

    private ImageView btnBack, btnShare, btnDownload;
    private TextView tvDiagnosisTitle, tvPatientName, tvPatientAge, tvPatientGender;
    private TextView tvDiagnosisDate, tvDiagnosisResult, tvConfidenceScore;
    private TextView tvConfidencePercentage, tvRecommendations, tvNotes;
    private CardView cardPatientInfo, cardDiagnosisInfo, cardRecommendations;

    private String diagnosisId, patientName, gender, date;
    private int age, confidence;
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
        diagnosisId = getIntent().getStringExtra("diagnosis_id");
        patientName = getIntent().getStringExtra("name");
        age = getIntent().getIntExtra("age", 0);
        isPemphigus = getIntent().getBooleanExtra("pemphigus", false);
        date = getIntent().getStringExtra("date");
        confidence = getIntent().getIntExtra("confidence", 0);
        gender = getIntent().getStringExtra("gender");
    }

    private void displayReportData() {
        // Set diagnosis title
        tvDiagnosisTitle.setText(diagnosisId != null ? diagnosisId : "Diagnosis Report");

        // Set patient information
        tvPatientName.setText(patientName != null ? patientName : "N/A");
        tvPatientAge.setText(String.valueOf(age) + " years");
        tvPatientGender.setText(gender != null ? capitalizeFirst(gender) : "N/A");

        // Set diagnosis information
        tvDiagnosisDate.setText(date != null ? date : "N/A");
        tvDiagnosisResult.setText(isPemphigus ? "Pemphigus Detected" : "No Pemphigus Detected");
        tvConfidenceScore.setText(confidence + "%");
        tvConfidencePercentage.setText("Confidence: " + confidence + "%");

        // Set color based on result
        if (isPemphigus) {
            tvDiagnosisResult.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvDiagnosisResult.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }

        // Set recommendations based on diagnosis
        String recommendations = getRecommendations(isPemphigus, confidence);
        tvRecommendations.setText(recommendations);

        // Set clinical notes
        String notes = getClinicalNotes(isPemphigus);
        tvNotes.setText(notes);
    }

    private String getRecommendations(boolean isPemphigus, int confidence) {
        if (isPemphigus) {
            if (confidence >= 90) {
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
        } else {
            return "• Continue routine skin care practices\n" +
                    "• Monitor for any changes in skin condition\n" +
                    "• Schedule regular check-ups as needed\n" +
                    "• Maintain good hygiene practices";
        }
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
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shareReport();
            }
        });

        btnDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadReport();
            }
        });
    }

    private void shareReport() {
        // Implement share functionality
        Toast.makeText(this, "Share feature coming soon!", Toast.LENGTH_SHORT).show();

        // Example implementation:
        // Intent shareIntent = new Intent(Intent.ACTION_SEND);
        // shareIntent.setType("text/plain");
        // shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Diagnosis Report");
        // shareIntent.putExtra(Intent.EXTRA_TEXT, getReportText());
        // startActivity(Intent.createChooser(shareIntent, "Share Report"));
    }

    private void downloadReport() {
        // Implement download/save functionality
        Toast.makeText(this, "Download feature coming soon!", Toast.LENGTH_SHORT).show();

        // You can implement PDF generation or data export here
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