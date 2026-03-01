package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.widget.NestedScrollView;

import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.Patient;
import com.example.patholens.modules.PatientResponse;
import com.example.patholens.utils.PrefsManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DiagnosisHistoryActivity extends AppCompatActivity {

    private static final String TAG           = "DiagnosisHistory";
    private static final String STATUS_SUCCESS = "success";
    private static final String GENDER_FEMALE  = "female";

    private ImageView      btnBack;
    private LinearLayout   diagnosisContainer;
    private ProgressBar    progressBar;
    private TextView       tvEmptyState;
    private NestedScrollView scrollView;

    private PrefsManager   prefsManager;
    private List<Patient>  patientList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosis_history);
        initializeViews();
        prefsManager = new PrefsManager(this);
        setupClickListeners();
        loadDiagnosisHistory();
    }

    private void initializeViews() {
        btnBack            = findViewById(R.id.btnBack);
        diagnosisContainer = findViewById(R.id.diagnosisContainer);
        scrollView         = findViewById(R.id.scrollView);
        progressBar        = findViewById(R.id.progressBar);
        tvEmptyState       = findViewById(R.id.tvEmptyState);
        tvEmptyState.setGravity(Gravity.CENTER);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    // ─────────────────────────────────────────────────────────
    // Network
    // ─────────────────────────────────────────────────────────

    private void loadDiagnosisHistory() {
        if (!validateUserSession()) return;
        showLoading(true);

        String userId = String.valueOf(prefsManager.getUserId());
        String token  = prefsManager.getBearerToken();
        Log.d(TAG, "Fetching history for user ID: " + userId);

        RetrofitClient.getInstance()
                .getApiService()
                .getUserPatientHistory(userId, token)
                .enqueue(createHistoryCallback());
    }

    private boolean validateUserSession() {
        if (prefsManager.getBearerToken() == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            finish();
            return false;
        }
        return true;
    }

    private Callback<PatientResponse> createHistoryCallback() {
        return new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                showLoading(false);
                handleHistoryResponse(response);
            }
            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                showLoading(false);
                handleHistoryFailure(t);
            }
        };
    }

    private void handleHistoryResponse(Response<PatientResponse> response) {
        if (response.isSuccessful() && response.body() != null) {
            processPatientResponse(response.body());
        } else {
            Log.e(TAG, "Response failed: " + response.code());
            Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void processPatientResponse(PatientResponse patientResponse) {
        if (STATUS_SUCCESS.equalsIgnoreCase(patientResponse.getStatus())) {
            patientList = patientResponse.getData();
            if (patientList != null && !patientList.isEmpty()) {
                Log.d(TAG, "Loaded " + patientList.size() + " records");
                displayDiagnosisHistory();
            } else {
                Log.d(TAG, "No records found");
                showEmptyState();
            }
        } else {
            String msg = patientResponse.getMessage() != null
                    ? patientResponse.getMessage() : "Failed to load history";
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void handleHistoryFailure(Throwable t) {
        Log.e(TAG, "Network error: " + t.getMessage(), t);
        Toast.makeText(this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        showEmptyState();
    }

    // ─────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────

    private void displayDiagnosisHistory() {
        diagnosisContainer.removeAllViews();
        for (int i = 0; i < patientList.size(); i++) {
            diagnosisContainer.addView(createDiagnosisCard(patientList.get(i), i + 1));
        }
        tvEmptyState.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private View createDiagnosisCard(Patient patient, int position) {
        View card = LayoutInflater.from(this)
                .inflate(R.layout.item_diagnosis_card, diagnosisContainer, false);
        populateCardViews(card, patient, position);
        card.findViewById(R.id.btnReport).setOnClickListener(v -> openReport(patient));
        return card;
    }

    private void populateCardViews(View card, Patient patient, int position) {
        TextView tvDiagnosisTitle = card.findViewById(R.id.tvDiagnosisTitle);
        TextView tvDate           = card.findViewById(R.id.tvDate);
        TextView tvPatientName    = card.findViewById(R.id.tvPatientName);
        TextView tvAge            = card.findViewById(R.id.tvAge);
        TextView tvPemphigus      = card.findViewById(R.id.tvPemphigus);
        TextView tvConfidence     = card.findViewById(R.id.tvConfidence);
        ImageView ivGenderIcon    = card.findViewById(R.id.ivGenderIcon);

        tvDiagnosisTitle.setText("Diagnosis " + position);
        tvDate.setText(patient.getFormattedDate());
        tvPatientName.setText("Name: " + patient.getPatientName());
        tvAge.setText("Age: " + patient.getAge());

        String resultLabel = patient.getResult();
        boolean isPemphigus = false;
        if (resultLabel != null) {
            String lower = resultLabel.toLowerCase();
            isPemphigus = lower.contains("pemphigus")
                    && !lower.contains("no-pemphigus")
                    && !lower.contains("no pemphigus")
                    && !lower.contains("negative");
        }
        tvPemphigus.setText("Pemphigus: " + (isPemphigus ? "Yes" : "No"));

        // confidence stored as decimal (e.g. 0.98) → multiply by 100
        double rawConfidence = patient.getConfidence();
        double confidencePercent = rawConfidence;

        if (confidencePercent == 0) {
            tvConfidence.setText("Confidence: Unable to classify");
        } else {
            tvConfidence.setText("Confidence: " + String.format("%.0f%%", confidencePercent));
        }

        ivGenderIcon.setImageResource(
                GENDER_FEMALE.equalsIgnoreCase(patient.getGender())
                        ? R.drawable.ic_female
                        : R.drawable.ic_male);
    }

    private void openReport(Patient patient) {
        Intent intent = new Intent(this, DiagnosisReportActivity.class);

        // Confidence is already stored as whole number (e.g. 87), NO multiplication needed
        int confidenceInt = (int) Math.round(patient.getConfidence());

        // Derive isPemphigus from the result label to avoid "no-pemphigus" being treated as true
        String resultLabel = patient.getResult(); // adjust getter name if different
        boolean isPemphigus = false;
        if (resultLabel != null) {
            String lower = resultLabel.toLowerCase();
            isPemphigus = lower.contains("pemphigus")
                    && !lower.contains("no-pemphigus")
                    && !lower.contains("no pemphigus")
                    && !lower.contains("negative");
        }

        intent.putExtra("diagnosis_id",   "Diagnosis Report #" + patient.getId());
        intent.putExtra("name",           patient.getPatientName());
        intent.putExtra("age",            patient.getAge());
        intent.putExtra("pemphigus",      isPemphigus);           // fixed
        intent.putExtra("result",         resultLabel);           // pass raw label too
        intent.putExtra("date",           patient.getFormattedDate());
        intent.putExtra("confidence",     confidenceInt);         // fixed: no * 100
        intent.putExtra("gender",         patient.getGender());
        intent.putExtra("contact_number", patient.getContactNumber());
        intent.putExtra("image_url",      patient.getDiagnosisingImage());

        startActivity(intent);
    }

    // ─────────────────────────────────────────────────────────
    // UI state
    // ─────────────────────────────────────────────────────────

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) {
            scrollView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        diagnosisContainer.removeAllViews();
        scrollView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }
}