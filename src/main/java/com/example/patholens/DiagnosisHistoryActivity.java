package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class DiagnosisHistoryActivity extends AppCompatActivity {

    private ImageView btnBack;
    private LinearLayout btnReport1, btnReport2, btnReport3, btnReport4;
    private LinearLayout diagnosisContainer;

    // List to store diagnosis data
    private List<DiagnosisRecord> diagnosisRecords;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_history);

        initializeViews();
        loadDiagnosisData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        btnReport1 = findViewById(R.id.btnReport1);
        btnReport2 = findViewById(R.id.btnReport2);
        btnReport3 = findViewById(R.id.btnReport3);
        btnReport4 = findViewById(R.id.btnReport4);
        diagnosisContainer = findViewById(R.id.diagnosisContainer);
    }

    private void loadDiagnosisData() {
        // Initialize diagnosis records
        diagnosisRecords = new ArrayList<>();

        // Sample data - Replace with database or API data
        diagnosisRecords.add(new DiagnosisRecord(
                "Diagnoses 4", "abc", 20, true, "14/06/25", 92, "male"
        ));
        diagnosisRecords.add(new DiagnosisRecord(
                "Diagnoses 3", "pqr", 34, true, "10/06/25", 93, "male"
        ));
        diagnosisRecords.add(new DiagnosisRecord(
                "Diagnoses 2", "xyz", 24, false, "08/06/25", 94, "female"
        ));
        diagnosisRecords.add(new DiagnosisRecord(
                "Diagnoses 1", "hjg", 23, false, "27/05/25", 93, "male"
        ));
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Report button listeners
        btnReport1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReport(diagnosisRecords.get(0));
            }
        });

        btnReport2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReport(diagnosisRecords.get(1));
            }
        });

        btnReport3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReport(diagnosisRecords.get(2));
            }
        });

        btnReport4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openReport(diagnosisRecords.get(3));
            }
        });
    }

    private void openReport(DiagnosisRecord record) {
        // Open detailed report activity
        Intent intent = new Intent(DiagnosisHistoryActivity.this, DiagnosisReportActivity.class);
        intent.putExtra("diagnosis_id", record.getDiagnosisName());
        intent.putExtra("name", record.getName());
        intent.putExtra("age", record.getAge());
        intent.putExtra("pemphigus", record.isPemphigus());
        intent.putExtra("date", record.getDate());
        intent.putExtra("confidence", record.getConfidence());
        intent.putExtra("gender", record.getGender());

        try {
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Report feature coming soon!", Toast.LENGTH_SHORT).show();
        }
    }

    // Inner class to store diagnosis data
    public static class DiagnosisRecord {
        private String diagnosisName;
        private String name;
        private int age;
        private boolean isPemphigus;
        private String date;
        private int confidence;
        private String gender;

        public DiagnosisRecord(String diagnosisName, String name, int age,
                               boolean isPemphigus, String date, int confidence, String gender) {
            this.diagnosisName = diagnosisName;
            this.name = name;
            this.age = age;
            this.isPemphigus = isPemphigus;
            this.date = date;
            this.confidence = confidence;
            this.gender = gender;
        }

        // Getters
        public String getDiagnosisName() { return diagnosisName; }
        public String getName() { return name; }
        public int getAge() { return age; }
        public boolean isPemphigus() { return isPemphigus; }
        public String getDate() { return date; }
        public int getConfidence() { return confidence; }
        public String getGender() { return gender; }
    }
}