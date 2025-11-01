package com.example.patholens;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.patholens.api.RetrofitClient;
import com.example.patholens.api.ApiService;
import com.example.patholens.modules.PatientRequest;
import com.example.patholens.modules.PatientResponse;
import com.example.patholens.utils.PrefsManager;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private static final String TAG = "ResultActivity";

    // UI Components
    private ImageView resultIcon;
    private TextView resultTitle;
    private TextView resultMessage;
    private TextView confidenceText;
    private CardView resultCard;
    private Button doneButton;
    private Button retryButton;

    // Patient information from previous activity
    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;
    private String imagePath; // Image path for upload

    // Diagnosis results from DiagnosingActivity
    private String diagnosisLabel;
    private double confidence;

    // API Service
    private ApiService apiService;
    private String authToken;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize API service
        apiService = RetrofitClient.getInstance().getApiService();

        // Initialize PrefsManager
        prefsManager = new PrefsManager(this);

        // Get auth token from PrefsManager
        authToken = prefsManager.getToken();

        Log.d(TAG, "Auth token retrieved: " + (authToken != null && !authToken.isEmpty() ? "Valid" : "Empty"));
        Log.d(TAG, "User ID: " + prefsManager.getUserId());
        Log.d(TAG, "Is Logged In: " + prefsManager.isLoggedIn());

        // Initialize views
        initializeViews();

        // Get data from intent
        receiveDataFromPreviousActivity();

        // Display results
        displayResults();

        // Send to Laravel API only if we have valid diagnosis
        if (!diagnosisLabel.equals("Image Not Recognized")) {
            sendDiagnosisToServer();
        } else {
            // Don't save unrecognized images
            retryButton.setText("Image Not Valid");
            retryButton.setEnabled(false);
        }

        // Set button listeners
        setupButtonListeners();
    }

    private void initializeViews() {
        resultIcon = findViewById(R.id.resultIcon);
        resultTitle = findViewById(R.id.resultTitle);
        resultMessage = findViewById(R.id.resultMessage);
        confidenceText = findViewById(R.id.confidenceText);
        resultCard = findViewById(R.id.resultCard);
        doneButton = findViewById(R.id.doneButton);
        retryButton = findViewById(R.id.retryButton);
    }

    /**
     * Receive all data from DiagnosingActivity
     */
    private void receiveDataFromPreviousActivity() {
        Intent intent = getIntent();

        // Patient information
        userId = intent.getStringExtra("user_id");
        isForMe = intent.getBooleanExtra("isForMe", true);
        patientName = intent.getStringExtra("patientName");
        patientAge = intent.getStringExtra("patientAge");
        patientPhone = intent.getStringExtra("patientPhone");
        patientGender = intent.getStringExtra("patientGender");
        imagePath = intent.getStringExtra("imagePath");

        // Diagnosis results
        diagnosisLabel = intent.getStringExtra("diagnosisLabel");
        confidence = intent.getDoubleExtra("confidence", 0.0);

        // If userId is not provided, get it from PrefsManager
        if (userId == null || userId.isEmpty()) {
            userId = String.valueOf(prefsManager.getUserId());
            Log.d(TAG, "Retrieved userId from PrefsManager: " + userId);
        }
    }

    /**
     * Display diagnosis results with appropriate styling
     */
    private void displayResults() {
        // Handle "Image Not Recognized" case
        if (diagnosisLabel.equals("Image Not Recognized")) {
            resultIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            resultIcon.setColorFilter(getResources().getColor(android.R.color.holo_orange_dark));

            resultTitle.setText("Image Not Recognized");
            resultTitle.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));

            resultMessage.setText("The uploaded image could not be recognized as a mouth/oral cavity image. " +
                    "Please ensure you upload a clear, well-lit image of the mouth area. " +
                    "Try again with a better quality image.");

            confidenceText.setText("Confidence: 0%");
            resultCard.setCardBackgroundColor(0xFFFFF4E6); // Light orange background
            return;
        }

        // Format confidence as percentage
        int confidencePercent = (int) (confidence * 100);
        confidenceText.setText("Confidence: " + confidencePercent + "%");

        // Determine if Pemphigus was detected based on label
        boolean isPemphigusDetected = isPemphigusPositive(diagnosisLabel);

        if (isPemphigusDetected) {
            // Positive result - Pemphigus detected
            resultIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            resultIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));

            resultTitle.setText(diagnosisLabel);
            resultTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));

            resultMessage.setText("Our AI analysis indicates the presence of " + diagnosisLabel + ". " +
                    "This requires immediate medical attention. Please consult with a dermatologist " +
                    "or healthcare professional for proper diagnosis, confirmation, and treatment planning.");

            resultCard.setCardBackgroundColor(0xFFFFE6E6); // Light red background

        } else {
            // Negative result or other classification
            resultIcon.setImageResource(android.R.drawable.ic_dialog_info);
            resultIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));

            resultTitle.setText(diagnosisLabel);
            resultTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));

            if (diagnosisLabel.equalsIgnoreCase("Normal") ||
                    diagnosisLabel.equalsIgnoreCase("No Pemphigus Detected") ||
                    diagnosisLabel.equalsIgnoreCase("Negative")) {
                resultMessage.setText("Our AI analysis shows no signs of pemphigus in the provided image. " +
                        "However, if you have ongoing concerns or symptoms, please consult with a healthcare professional.");
            } else {
                resultMessage.setText("Classification result: " + diagnosisLabel + ". " +
                        "Please consult with a healthcare professional for proper medical interpretation and guidance.");
            }

            resultCard.setCardBackgroundColor(0xFFE6FFE6); // Light green background
        }
    }

    /**
     * Determine if diagnosis indicates Pemphigus
     */
    private boolean isPemphigusPositive(String label) {
        if (label == null) return false;

        String lowerLabel = label.toLowerCase();
        return lowerLabel.contains("pemphigus") &&
                !lowerLabel.contains("no pemphigus") &&
                !lowerLabel.contains("negative");
    }

    /**
     * Send diagnosis data to Laravel API server
     */
    private void sendDiagnosisToServer() {
        if (authToken == null || authToken.isEmpty()) {
            Log.e(TAG, "No auth token found - User not logged in");

            runOnUiThread(() -> {
                retryButton.setText("Login Required");
                retryButton.setEnabled(true);
                retryButton.setOnClickListener(v -> {
                    // Navigate to login
                    Intent loginIntent = new Intent(ResultActivity.this, LoginActivity.class);
                    startActivity(loginIntent);
                });
            });
            return;
        }

        Log.d(TAG, "Sending diagnosis to server with token");

        // Show loading state
        retryButton.setEnabled(false);
        retryButton.setText("Uploading to server...");

        // Check if image path exists
        if (imagePath != null && !imagePath.isEmpty()) {
            sendWithImage();
        } else {
            sendWithoutImage();
        }
    }

    /**
     * Send diagnosis data with image
     */
    private void sendWithImage() {
        try {
            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                Log.e(TAG, "Image file not found: " + imagePath);
                sendWithoutImage();
                return;
            }

            // Create RequestBody for image
            RequestBody requestFile = RequestBody.create(
                    MediaType.parse("image/*"),
                    imageFile
            );
            MultipartBody.Part imagePart = MultipartBody.Part.createFormData(
                    "diagnosising_image",
                    imageFile.getName(),
                    requestFile
            );

            // Create RequestBody for other fields
            RequestBody userIdBody = RequestBody.create(MediaType.parse("text/plain"), userId);
            RequestBody nameBody = RequestBody.create(MediaType.parse("text/plain"), patientName);
            RequestBody ageBody = RequestBody.create(MediaType.parse("text/plain"), patientAge);
            RequestBody genderBody = RequestBody.create(MediaType.parse("text/plain"), patientGender.toLowerCase());
            RequestBody phoneBody = RequestBody.create(MediaType.parse("text/plain"), patientPhone);
            RequestBody resultBody = RequestBody.create(MediaType.parse("text/plain"), diagnosisLabel);
            RequestBody confidenceBody = RequestBody.create(MediaType.parse("text/plain"), String.valueOf(confidence));

            Log.d(TAG, "Uploading with image. Token: Bearer " + authToken.substring(0, Math.min(20, authToken.length())) + "...");

            // Make API call with image
            Call<PatientResponse> call = apiService.storePatientWithImage(
                    "Bearer " + authToken,
                    userIdBody,
                    nameBody,
                    ageBody,
                    genderBody,
                    phoneBody,
                    resultBody,
                    confidenceBody,
                    imagePart
            );

            call.enqueue(new Callback<PatientResponse>() {
                @Override
                public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                    handleServerResponse(response);
                }

                @Override
                public void onFailure(Call<PatientResponse> call, Throwable t) {
                    handleServerError(t);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload: " + e.getMessage());
            e.printStackTrace();
            sendWithoutImage();
        }
    }

    /**
     * Send diagnosis data without image
     */
    private void sendWithoutImage() {
        PatientRequest request = new PatientRequest(
                userId,
                patientName,
                patientAge,
                patientGender.toLowerCase(),
                patientPhone,
                diagnosisLabel,
                confidence
        );

        Log.d(TAG, "Uploading without image. Token: Bearer " + authToken.substring(0, Math.min(20, authToken.length())) + "...");

        Call<PatientResponse> call = apiService.storePatient("Bearer " + authToken, request);

        call.enqueue(new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                handleServerResponse(response);
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                handleServerError(t);
            }
        });
    }

    /**
     * Handle successful server response
     */
    private void handleServerResponse(Response<PatientResponse> response) {
        Log.d(TAG, "Server response code: " + response.code());

        if (response.code() == 401) {
            // Unauthorized - token expired or invalid
            Log.e(TAG, "Unauthorized - Token may be expired");
            runOnUiThread(() -> {
                retryButton.setText("Session Expired - Login Again");
                retryButton.setEnabled(true);
                Toast.makeText(ResultActivity.this,
                        "Your session has expired. Please login again.",
                        Toast.LENGTH_LONG).show();

                retryButton.setOnClickListener(v -> {
                    // Clear old token and navigate to login
                    prefsManager.clearLoginData();
                    Intent loginIntent = new Intent(ResultActivity.this, LoginActivity.class);
                    loginIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(loginIntent);
                    finish();
                });
            });
            return;
        }

        if (response.isSuccessful() && response.body() != null) {
            PatientResponse patientResponse = response.body();

            if ("success".equals(patientResponse.getStatus())) {
                Log.d(TAG, "Diagnosis uploaded to server successfully");

                runOnUiThread(() -> {
                    retryButton.setText("✓ Saved to cloud");
                    retryButton.setEnabled(false);
                    Toast.makeText(ResultActivity.this,
                            "Diagnosis saved successfully!",
                            Toast.LENGTH_SHORT).show();
                });
            } else {
                Log.e(TAG, "Server returned error status: " + patientResponse.getMessage());
                handleUploadFailure();
            }
        } else {
            Log.e(TAG, "Server response unsuccessful: " + response.code());
            handleUploadFailure();
        }
    }

    /**
     * Handle server error
     */
    private void handleServerError(Throwable t) {
        Log.e(TAG, "Network error: " + t.getMessage());
        t.printStackTrace();
        handleUploadFailure();
    }

    /**
     * Handle upload failure
     */
    private void handleUploadFailure() {
        runOnUiThread(() -> {
            retryButton.setText("Retry Upload");
            retryButton.setEnabled(true);
            Toast.makeText(ResultActivity.this,
                    "Failed to upload to server. Please try again.",
                    Toast.LENGTH_LONG).show();
        });
    }

    /**
     * Setup button click listeners
     */
    private void setupButtonListeners() {
        doneButton.setOnClickListener(v -> {
            // Navigate back to MainActivity
            Intent mainIntent = new Intent(ResultActivity.this, MainActivity.class);
            mainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(mainIntent);
            finish();
        });

        retryButton.setOnClickListener(v -> {
            if (retryButton.isEnabled() && !retryButton.getText().toString().contains("Login")) {
                // Retry upload
                sendDiagnosisToServer();
            }
        });
    }
}