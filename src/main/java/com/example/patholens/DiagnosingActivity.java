package com.example.patholens;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiagnosingActivity extends AppCompatActivity {

    private static final String TAG = "DiagnosingActivity";
    private static final String API_URL = "http://10.111.90.57:8000/api/v1/classify-image";
    private static final int API_TIMEOUT_SECONDS = 30;
    private static final int PROGRESS_TARGET = 90;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_DELAY_MS = 40;
    private static final int MESSAGE_DELAY_MS = 1800;
    private static final int FADE_DURATION_MS = 300;
    private static final int NAVIGATION_DELAY_MS = 300;

    private static final String[] CHEERING_MESSAGES = {
            "Analyzing image patterns...",
            "Processing medical data...",
            "Our AI is working hard...",
            "Examining the details...",
            "Almost there! Hang tight...",
            "Getting your results ready...",
            "Finalizing diagnosis..."
    };

    private ProgressBar progressBar;
    private TextView messageText;
    private TextView percentText;
    private Handler handler;

    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;
    private String imageUriString;

    private String diagnosisLabel = "";
    private double confidence = 0.0;
    private boolean apiCallCompleted = false;
    private int progressStatus = 0;
    private int currentMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosing);

        initializeViews();
        receiveDataFromPreviousActivity();
        startDiagnosing();
    }

    private void initializeViews() {
        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        percentText = findViewById(R.id.percentText);
        handler = new Handler();
    }

    private void receiveDataFromPreviousActivity() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        isForMe = intent.getBooleanExtra("isForMe", true);
        patientName = intent.getStringExtra("patientName");
        patientAge = intent.getStringExtra("patientAge");
        patientPhone = intent.getStringExtra("patientPhone");
        patientGender = intent.getStringExtra("patientGender");
        imageUriString = intent.getStringExtra("imageUri");
    }

    private void startDiagnosing() {
        startMessageAnimation();
        callClassificationAPI();
        startProgressAnimation();
    }

    private void callClassificationAPI() {
        new Thread(() -> {
            try {
                Uri imageUri = Uri.parse(imageUriString);
                File imageFile = getFileFromUri(imageUri);

                if (imageFile == null) {
                    handleFileError();
                    return;
                }

                Response response = executeApiRequest(imageFile);
                String responseBody = response.body().string();

                logApiResponse(response.code(), responseBody);

                if (response.isSuccessful()) {
                    handleSuccessfulApiResponse(responseBody);
                } else {
                    throw new Exception("API call failed: " + response.code());
                }

                imageFile.delete();

            } catch (Exception e) {
                handleApiError(e);
            }
        }).start();
    }

    private Response executeApiRequest(File imageFile) throws Exception {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(API_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .build();

        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("image", imageFile.getName(),
                        RequestBody.create(MediaType.parse("image/*"), imageFile))
                .build();

        Request request = new Request.Builder()
                .url(API_URL)
                .post(requestBody)
                .addHeader("Accept", "application/json")
                .build();

        return client.newCall(request).execute();
    }

    private void handleSuccessfulApiResponse(String responseBody) throws Exception {
        JSONObject jsonResponse = new JSONObject(responseBody);
        String status = jsonResponse.optString("status", "");

        Log.d(TAG, "API Status: " + status);

        if ("success".equals(status)) {
            parseSuccessResponse(jsonResponse);
        } else if ("error".equals(status)) {
            handleErrorResponse(jsonResponse);
        } else {
            String errorMessage = jsonResponse.optString("message", "Unknown error");
            throw new Exception("API returned unexpected status: " + errorMessage);
        }
    }

    private void parseSuccessResponse(JSONObject jsonResponse) throws Exception {
        JSONObject data = jsonResponse.getJSONObject("data");
        diagnosisLabel = data.getString("label");
        confidence = data.getDouble("confidence");
        apiCallCompleted = true;

        Log.d(TAG, "Classification successful - Label: " + diagnosisLabel + ", Confidence: " + confidence);
    }

    private void handleErrorResponse(JSONObject jsonResponse) {
        String errorMessage = jsonResponse.optString("message", "Unable to process image");
        Log.e(TAG, "API Error: " + errorMessage);

        diagnosisLabel = "Image Not Recognized";
        confidence = 0.0;
        apiCallCompleted = true;

        runOnUiThread(() -> {
            Toast.makeText(this,
                    "Unable to recognize the image. Please upload a clear image of the mouth area.",
                    Toast.LENGTH_LONG).show();
        });
    }

    private void handleFileError() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void handleApiError(Exception e) {
        Log.e(TAG, "API Error: " + e.getMessage(), e);

        runOnUiThread(() -> {
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            diagnosisLabel = "Unable to classify";
            confidence = 0.0;
            apiCallCompleted = true;
        });
    }

    private void logApiResponse(int statusCode, String responseBody) {
        Log.d(TAG, "API Status Code: " + statusCode);
        Log.d(TAG, "API Response Body: " + responseBody);
    }

    private File getFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();
            String fileName = getFileNameFromUri(uri, contentResolver);
            File tempFile = new File(getCacheDir(), fileName);

            copyUriToFile(uri, tempFile, contentResolver);

            return tempFile;

        } catch (Exception e) {
            Log.e(TAG, "Error converting URI to File", e);
            return null;
        }
    }

    private String getFileNameFromUri(Uri uri, ContentResolver contentResolver) {
        String fileName = "temp_image.jpg";

        try (Cursor cursor = contentResolver.query(uri, null, null, null, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
            }
        }

        return fileName;
    }

    private void copyUriToFile(Uri uri, File tempFile, ContentResolver contentResolver) throws Exception {
        try (InputStream inputStream = contentResolver.openInputStream(uri);
             OutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }
    }

    private void startProgressAnimation() {
        new Thread(() -> {
            animateProgressToTarget();
            waitForApiCompletion();
            completeProgress();
            navigateToResult();
        }).start();
    }

    private void animateProgressToTarget() {
        while (progressStatus < PROGRESS_TARGET) {
            progressStatus++;
            updateProgressUI(progressStatus);

            try {
                Thread.sleep(PROGRESS_DELAY_MS);
            } catch (InterruptedException e) {
                Log.e(TAG, "Progress animation interrupted", e);
            }
        }
    }

    private void waitForApiCompletion() {
        while (!apiCallCompleted) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "API wait interrupted", e);
            }
        }
    }

    private void completeProgress() {
        while (progressStatus <= PROGRESS_MAX) {
            progressStatus++;
            updateProgressUI(progressStatus);

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                Log.e(TAG, "Progress completion interrupted", e);
            }
        }
    }

    private void updateProgressUI(int progress) {
        handler.post(() -> {
            progressBar.setProgress(progress);
            percentText.setText(progress + "%");
        });
    }

    private void navigateToResult() {
        handler.postDelayed(() -> {
            Intent intent = createResultIntent();
            startActivity(intent);
            finish();
        }, NAVIGATION_DELAY_MS);
    }

    private Intent createResultIntent() {
        Intent intent = new Intent(this, ResultActivity.class);

        intent.putExtra("user_id", userId);
        intent.putExtra("isForMe", isForMe);
        intent.putExtra("patientName", patientName);
        intent.putExtra("patientAge", patientAge);
        intent.putExtra("patientPhone", patientPhone);
        intent.putExtra("patientGender", patientGender);
        intent.putExtra("diagnosisLabel", diagnosisLabel);
        intent.putExtra("confidence", confidence);

        return intent;
    }

    private void startMessageAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressStatus < PROGRESS_MAX) {
                    animateMessageChange();
                    handler.postDelayed(this, MESSAGE_DELAY_MS);
                }
            }
        }, MESSAGE_DELAY_MS);
    }

    private void animateMessageChange() {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(FADE_DURATION_MS);
        fadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation) {
                updateMessage();
                fadeInMessage();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
        messageText.startAnimation(fadeOut);
    }

    private void updateMessage() {
        currentMessageIndex = (currentMessageIndex + 1) % CHEERING_MESSAGES.length;
        messageText.setText(CHEERING_MESSAGES[currentMessageIndex]);
    }

    private void fadeInMessage() {
        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(FADE_DURATION_MS);
        messageText.startAnimation(fadeIn);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}