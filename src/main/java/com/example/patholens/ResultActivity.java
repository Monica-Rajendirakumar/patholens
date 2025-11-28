package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.patholens.api.ApiService;
import com.example.patholens.api.RetrofitClient;
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
    private static final int UNAUTHORIZED_CODE = 401;
    private static final int COLOR_ORANGE_DARK = android.R.color.holo_orange_dark;
    private static final int COLOR_RED_DARK = android.R.color.holo_red_dark;
    private static final int COLOR_GREEN_DARK = android.R.color.holo_green_dark;

    private ImageView resultIcon;
    private TextView resultTitle;
    private TextView resultMessage;
    private TextView confidenceText;
    private CardView resultCard;
    private Button doneButton;
    private Button retryButton;

    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;
    private String imagePath;

    private String diagnosisLabel;
    private double confidence;

    private ApiService apiService;
    private String authToken;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        initializeDependencies();
        initializeViews();
        receiveDataFromPreviousActivity();
        displayResults();

        if (!diagnosisLabel.equals("Image Not Recognized")) {
            sendDiagnosisToServer();
        } else {
            handleUnrecognizedImage();
        }

        setupButtonListeners();
    }

    private void initializeDependencies() {
        apiService = RetrofitClient.getInstance().getApiService();
        prefsManager = new PrefsManager(this);
        authToken = prefsManager.getToken();
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

    private void receiveDataFromPreviousActivity() {
        Intent intent = getIntent();

        userId = intent.getStringExtra("user_id");
        isForMe = intent.getBooleanExtra("isForMe", true);
        patientName = intent.getStringExtra("patientName");
        patientAge = intent.getStringExtra("patientAge");
        patientPhone = intent.getStringExtra("patientPhone");
        patientGender = intent.getStringExtra("patientGender");
        imagePath = intent.getStringExtra("imagePath");

        diagnosisLabel = intent.getStringExtra("diagnosisLabel");
        confidence = intent.getDoubleExtra("confidence", 0.0);

        if (userId == null || userId.isEmpty()) {
            userId = String.valueOf(prefsManager.getUserId());
        }
    }

    private void displayResults() {
        if (diagnosisLabel.equals("Image Not Recognized")) {
            displayUnrecognizedImageResult();
        } else {
            displayDiagnosisResult();
        }
    }

    private void displayUnrecognizedImageResult() {
        resultIcon.setImageResource(android.R.drawable.ic_dialog_alert);
        resultIcon.setColorFilter(getResources().getColor(COLOR_ORANGE_DARK));

        resultTitle.setText("Image Not Recognized");
        resultTitle.setTextColor(getResources().getColor(COLOR_ORANGE_DARK));

        resultMessage.setText("The uploaded image could not be recognized as a mouth/oral cavity image. " +
                "Please ensure you upload a clear, well-lit image of the mouth area. " +
                "Try again with a better quality image.");

        confidenceText.setText("Confidence: 0%");
        resultCard.setCardBackgroundColor(0xFFFFF4E6);
    }

    private void displayDiagnosisResult() {
        int confidencePercent = (int) (confidence * 100);
        confidenceText.setText("Confidence: " + confidencePercent + "%");

        boolean isPemphigusDetected = isPemphigusPositive(diagnosisLabel);

        if (isPemphigusDetected) {
            displayPositiveResult();
        } else {
            displayNegativeResult();
        }
    }

    private void displayPositiveResult() {
        resultIcon.setImageResource(android.R.drawable.ic_dialog_alert);
        resultIcon.setColorFilter(getResources().getColor(COLOR_RED_DARK));

        resultTitle.setText(diagnosisLabel);
        resultTitle.setTextColor(getResources().getColor(COLOR_RED_DARK));

        resultMessage.setText("Our AI analysis indicates the presence of " + diagnosisLabel + ". " +
                "This requires immediate medical attention. Please consult with a dermatologist " +
                "or healthcare professional for proper diagnosis, confirmation, and treatment planning.");

        resultCard.setCardBackgroundColor(0xFFFFE6E6);
    }

    private void displayNegativeResult() {
        resultIcon.setImageResource(android.R.drawable.ic_dialog_info);
        resultIcon.setColorFilter(getResources().getColor(COLOR_GREEN_DARK));

        resultTitle.setText(diagnosisLabel);
        resultTitle.setTextColor(getResources().getColor(COLOR_GREEN_DARK));

        resultMessage.setText(getNegativeResultMessage());
        resultCard.setCardBackgroundColor(0xFFE6FFE6);
    }

    private String getNegativeResultMessage() {
        if (isNormalResult(diagnosisLabel)) {
            return "Our AI analysis shows no signs of pemphigus in the provided image. " +
                    "However, if you have ongoing concerns or symptoms, please consult with a healthcare professional.";
        } else {
            return "Classification result: " + diagnosisLabel + ". " +
                    "Please consult with a healthcare professional for proper medical interpretation and guidance.";
        }
    }

    private boolean isNormalResult(String label) {
        if (label == null) return false;
        String lowerLabel = label.toLowerCase();
        return lowerLabel.equals("normal") ||
                lowerLabel.equals("no pemphigus detected") ||
                lowerLabel.equals("negative");
    }

    private boolean isPemphigusPositive(String label) {
        if (label == null) return false;
        String lowerLabel = label.toLowerCase();
        return lowerLabel.contains("pemphigus") &&
                !lowerLabel.contains("no pemphigus") &&
                !lowerLabel.contains("negative");
    }

    private void handleUnrecognizedImage() {
        retryButton.setText("Image Not Valid");
        retryButton.setEnabled(false);
    }

    private void sendDiagnosisToServer() {
        if (!isUserAuthenticated()) {
            handleUnauthenticatedUser();
            return;
        }

        setUploadingState();

        if (imagePath != null && !imagePath.isEmpty()) {
            sendWithImage();
        } else {
            sendWithoutImage();
        }
    }

    private boolean isUserAuthenticated() {
        return authToken != null && !authToken.isEmpty();
    }

    private void handleUnauthenticatedUser() {
        Log.e(TAG, "No auth token found - User not logged in");
        runOnUiThread(() -> {
            retryButton.setText("Login Required");
            retryButton.setEnabled(true);
            retryButton.setOnClickListener(v -> navigateToLogin());
        });
    }

    private void setUploadingState() {
        retryButton.setEnabled(false);
        retryButton.setText("Uploading to server...");
    }

    private void sendWithImage() {
        try {
            File imageFile = new File(imagePath);

            if (!imageFile.exists()) {
                Log.e(TAG, "Image file not found: " + imagePath);
                sendWithoutImage();
                return;
            }

            MultipartBody.Part imagePart = createImagePart(imageFile);
            uploadWithImage(imagePart);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload: " + e.getMessage());
            sendWithoutImage();
        }
    }

    private MultipartBody.Part createImagePart(File imageFile) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), imageFile);
        return MultipartBody.Part.createFormData("diagnosising_image", imageFile.getName(), requestFile);
    }

    private void uploadWithImage(MultipartBody.Part imagePart) {
        RequestBody userIdBody = createTextBody(userId);
        RequestBody nameBody = createTextBody(patientName);
        RequestBody ageBody = createTextBody(patientAge);
        RequestBody genderBody = createTextBody(patientGender.toLowerCase());
        RequestBody phoneBody = createTextBody(patientPhone);
        RequestBody resultBody = createTextBody(diagnosisLabel);
        RequestBody confidenceBody = createTextBody(String.valueOf(confidence));

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

        call.enqueue(createResponseCallback());
    }

    private RequestBody createTextBody(String text) {
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }

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

        Call<PatientResponse> call = apiService.storePatient("Bearer " + authToken, request);
        call.enqueue(createResponseCallback());
    }

    private Callback<PatientResponse> createResponseCallback() {
        return new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                handleServerResponse(response);
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                handleServerError(t);
            }
        };
    }

    private void handleServerResponse(Response<PatientResponse> response) {
        Log.d(TAG, "Server response code: " + response.code());

        if (response.code() == UNAUTHORIZED_CODE) {
            handleUnauthorizedResponse();
            return;
        }

        if (response.isSuccessful() && response.body() != null) {
            handleSuccessfulResponse(response.body());
        } else {
            Log.e(TAG, "Server response unsuccessful: " + response.code());
            handleUploadFailure();
        }
    }

    private void handleUnauthorizedResponse() {
        Log.e(TAG, "Unauthorized - Token may be expired");
        runOnUiThread(() -> {
            retryButton.setText("Session Expired - Login Again");
            retryButton.setEnabled(true);
            Toast.makeText(this, "Your session has expired. Please login again.",
                    Toast.LENGTH_LONG).show();
            retryButton.setOnClickListener(v -> {
                prefsManager.clearLoginData();
                navigateToLoginAndClearStack();
            });
        });
    }

    private void handleSuccessfulResponse(PatientResponse patientResponse) {
        if ("success".equals(patientResponse.getStatus())) {
            Log.d(TAG, "Diagnosis uploaded to server successfully");
            runOnUiThread(() -> {
                retryButton.setText("✓ Saved to cloud");
                retryButton.setEnabled(false);
                Toast.makeText(this, "Diagnosis saved successfully!", Toast.LENGTH_SHORT).show();
            });
        } else {
            Log.e(TAG, "Server returned error status: " + patientResponse.getMessage());
            handleUploadFailure();
        }
    }

    private void handleServerError(Throwable t) {
        Log.e(TAG, "Network error: " + t.getMessage());
        handleUploadFailure();
    }

    private void handleUploadFailure() {
        runOnUiThread(() -> {
            retryButton.setText("Retry Upload");
            retryButton.setEnabled(true);
            Toast.makeText(this, "Failed to upload to server. Please try again.",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void setupButtonListeners() {
        doneButton.setOnClickListener(v -> navigateToMainActivity());

        retryButton.setOnClickListener(v -> {
            if (retryButton.isEnabled() && !retryButton.getText().toString().contains("Login")) {
                sendDiagnosisToServer();
            }
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }

    private void navigateToLoginAndClearStack() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }
}