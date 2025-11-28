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

    private static final String TAG = "DiagnosisHistory";
    private static final String STATUS_SUCCESS = "success";
    private static final String GENDER_FEMALE = "female";

    private ImageView btnBack;
    private LinearLayout diagnosisContainer;
    private ProgressBar progressBar;
    private TextView tvEmptyState;
    private NestedScrollView scrollView;

    private PrefsManager prefsManager;
    private List<Patient> patientList;

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
        btnBack = findViewById(R.id.btnBack);
        diagnosisContainer = findViewById(R.id.diagnosisContainer);
        scrollView = findViewById(R.id.scrollView);
        progressBar = findViewById(R.id.progressBar);
        tvEmptyState = findViewById(R.id.tvEmptyState);

        tvEmptyState.setGravity(Gravity.CENTER);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());
    }

    private void loadDiagnosisHistory() {
        if (!validateUserSession()) {
            return;
        }

        showLoading(true);

        String userId = String.valueOf(prefsManager.getUserId());
        String token = prefsManager.getBearerToken();

        Log.d(TAG, "Fetching history for user ID: " + userId);

        RetrofitClient.getInstance()
                .getApiService()
                .getUserPatientHistory(userId, token)
                .enqueue(createHistoryCallback());
    }

    private boolean validateUserSession() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
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
            PatientResponse patientResponse = response.body();
            processPatientResponse(patientResponse);
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
                Log.d(TAG, "Loaded " + patientList.size() + " patient records");
                displayDiagnosisHistory();
            } else {
                Log.d(TAG, "No patient records found");
                showEmptyState();
            }
        } else {
            String errorMsg = patientResponse.getMessage() != null ?
                    patientResponse.getMessage() : "Failed to load history";
            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show();
            showEmptyState();
        }
    }

    private void handleHistoryFailure(Throwable t) {
        Log.e(TAG, "Network error: " + t.getMessage(), t);
        Toast.makeText(this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
        showEmptyState();
    }

    private void displayDiagnosisHistory() {
        diagnosisContainer.removeAllViews();

        for (int i = 0; i < patientList.size(); i++) {
            Patient patient = patientList.get(i);
            View cardView = createDiagnosisCard(patient, i + 1);
            diagnosisContainer.addView(cardView);
        }

        showHistoryContent();
    }

    private void showHistoryContent() {
        tvEmptyState.setVisibility(View.GONE);
        scrollView.setVisibility(View.VISIBLE);
    }

    private View createDiagnosisCard(Patient patient, int position) {
        View cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_diagnosis_card, diagnosisContainer, false);

        populateCardViews(cardView, patient, position);
        setupCardClickListener(cardView, patient);

        return cardView;
    }

    private void populateCardViews(View cardView, Patient patient, int position) {
        TextView tvDiagnosisTitle = cardView.findViewById(R.id.tvDiagnosisTitle);
        TextView tvDate = cardView.findViewById(R.id.tvDate);
        TextView tvPatientName = cardView.findViewById(R.id.tvPatientName);
        TextView tvAge = cardView.findViewById(R.id.tvAge);
        TextView tvPemphigus = cardView.findViewById(R.id.tvPemphigus);
        TextView tvConfidence = cardView.findViewById(R.id.tvConfidence);
        ImageView ivGenderIcon = cardView.findViewById(R.id.ivGenderIcon);

        tvDiagnosisTitle.setText("Diagnosis " + position);
        tvDate.setText(patient.getFormattedDate());
        tvPatientName.setText("Name: " + patient.getPatientName());
        tvAge.setText("Age: " + patient.getAge());

        populateDiagnosisResult(tvPemphigus, tvConfidence, patient);
        setGenderIcon(ivGenderIcon, patient.getGender());
    }

    private void populateDiagnosisResult(TextView tvPemphigus, TextView tvConfidence, Patient patient) {
        boolean isPemphigus = patient.isPemphigus();
        tvPemphigus.setText("Pemphigus: " + (isPemphigus ? "Yes" : "No"));
        tvConfidence.setText("Confidence: " + String.format("%.0f%%", patient.getConfidence()));
    }

    private void setGenderIcon(ImageView ivGenderIcon, String gender) {
        int iconResource = GENDER_FEMALE.equalsIgnoreCase(gender) ?
                R.drawable.ic_female :
                R.drawable.ic_male;
        ivGenderIcon.setImageResource(iconResource);
    }

    private void setupCardClickListener(View cardView, Patient patient) {
        LinearLayout btnReport = cardView.findViewById(R.id.btnReport);
        btnReport.setOnClickListener(v -> openReport(patient));
    }

    private void openReport(Patient patient) {
        Intent intent = createReportIntent(patient);
        startActivity(intent);
    }

    private Intent createReportIntent(Patient patient) {
        Intent intent = new Intent(this, DiagnosisReportActivity.class);

        intent.putExtra("diagnosis_id", "Diagnosis Report #" + patient.getId());
        intent.putExtra("name", patient.getPatientName());
        intent.putExtra("age", patient.getAge());
        intent.putExtra("pemphigus", patient.isPemphigus());
        intent.putExtra("date", patient.getFormattedDate());
        intent.putExtra("confidence", (int) patient.getConfidence());
        intent.putExtra("gender", patient.getGender());
        intent.putExtra("contact_number", patient.getContactNumber());
        intent.putExtra("image_url", patient.getDiagnosisingImage());

        return intent;
    }

    private void showLoading(boolean show) {
        if (show) {
            progressBar.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.GONE);
        } else {
            progressBar.setVisibility(View.GONE);
        }
    }

    private void showEmptyState() {
        diagnosisContainer.removeAllViews();
        scrollView.setVisibility(View.GONE);
        tvEmptyState.setVisibility(View.VISIBLE);
    }
}