package com.example.patholens;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.OpenableColumns;
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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DiagnosingActivity extends AppCompatActivity {

    // UI Components
    private ProgressBar progressBar;
    private TextView messageText;
    private TextView percentText;
    private Handler handler;
    private int progressStatus = 0;

    // Patient information from previous activity
    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;
    private String imageUriString;

    // API Configuration
    private static final String API_URL = "http://192.168.0.110:8000/api/v1/classify-image";

    // Cheering messages
    private String[] cheeringMessages = {
            "Analyzing image patterns...",
            "Processing medical data...",
            "Our AI is working hard...",
            "Examining the details...",
            "Almost there! Hang tight...",
            "Getting your results ready...",
            "Finalizing diagnosis..."
    };
    private int currentMessageIndex = 0;

    // API Response data
    private String diagnosisLabel = "";
    private double confidence = 0.0;
    private boolean apiCallCompleted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosing);

        // Initialize views
        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        percentText = findViewById(R.id.percentText);
        handler = new Handler();

        // Receive data from ImageUploadActivity
        receiveDataFromPreviousActivity();

        // Start the diagnosis process
        startDiagnosing();
    }

    /**
     * Receive all data passed from ImageUploadActivity
     */
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
        // Start message animation
        startMessageAnimation();

        // Start API call in background thread
        callClassificationAPI();

        // Start progress bar animation
        startProgressAnimation();
    }

    /**
     * Call the Laravel classification API
     */
    private void callClassificationAPI() {
        new Thread(() -> {
            try {
                Uri imageUri = Uri.parse(imageUriString);

                // Convert URI to File
                File imageFile = getFileFromUri(imageUri);
                if (imageFile == null) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                    return;
                }

                // Create multipart request
                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
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

                // Execute request
                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                // DEBUG: Log the full response
                android.util.Log.e("API_RESPONSE", "Status Code: " + response.code());
                android.util.Log.e("API_RESPONSE", "Body: " + responseBody);

                if (response.isSuccessful()) {
                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(responseBody);

                    // DEBUG: Log parsed JSON
                    android.util.Log.e("API_RESPONSE", "JSON Status: " + jsonResponse.optString("status", "MISSING"));

                    String status = jsonResponse.optString("status", "");

                    if ("success".equals(status)) {
                        JSONObject data = jsonResponse.getJSONObject("data");
                        diagnosisLabel = data.getString("label");
                        confidence = data.getDouble("confidence");
                        apiCallCompleted = true;

                        android.util.Log.e("API_RESPONSE", "Success! Label: " + diagnosisLabel + ", Confidence: " + confidence);
                    } else if ("error".equals(status)) {
                        // Handle error response (e.g., NoneType, invalid image, non-mouth image)
                        String errorMessage = jsonResponse.optString("message", "Unable to process image");
                        android.util.Log.e("API_RESPONSE", "API Error: " + errorMessage);

                        // Set special values for invalid/unrecognizable images
                        diagnosisLabel = "Image Not Recognized";
                        confidence = 0.0;
                        apiCallCompleted = true;

                        runOnUiThread(() -> {
                            Toast.makeText(DiagnosingActivity.this,
                                    "Unable to recognize the image. Please upload a clear image of the mouth area.",
                                    Toast.LENGTH_LONG).show();
                        });
                    } else {
                        // Unknown status
                        String errorMessage = jsonResponse.optString("message", "Unknown error");
                        android.util.Log.e("API_RESPONSE", "Unknown Status: " + errorMessage);
                        throw new Exception("API returned unexpected status: " + errorMessage);
                    }
                } else {
                    android.util.Log.e("API_RESPONSE", "HTTP Error: " + response.code() + " - " + responseBody);
                    throw new Exception("API call failed: " + response.code());
                }

                // Clean up temporary file
                imageFile.delete();

            } catch (Exception e) {
                e.printStackTrace();
                android.util.Log.e("API_ERROR", "Exception: " + e.getMessage(), e);

                runOnUiThread(() -> {
                    Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();

                    // Use fallback mock data on error
                    diagnosisLabel = "Unable to classify";
                    confidence = 0.0;
                    apiCallCompleted = true;
                });
            }
        }).start();
    }

    /**
     * Convert URI to File for uploading
     */
    private File getFileFromUri(Uri uri) {
        try {
            ContentResolver contentResolver = getContentResolver();

            // Get file name
            String fileName = "temp_image.jpg";
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                int nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (nameIndex != -1) {
                    fileName = cursor.getString(nameIndex);
                }
                cursor.close();
            }

            // Create temporary file
            File tempFile = new File(getCacheDir(), fileName);

            // Copy URI content to file
            InputStream inputStream = contentResolver.openInputStream(uri);
            OutputStream outputStream = new FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            return tempFile;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Animate progress bar - waits for API response
     */
    private void startProgressAnimation() {
        new Thread(() -> {
            // Progress moves to 90% relatively quickly
            while (progressStatus < 90) {
                progressStatus += 1;

                final int currentProgress = progressStatus;
                handler.post(() -> {
                    progressBar.setProgress(currentProgress);
                    percentText.setText(currentProgress + "%");
                });

                try {
                    Thread.sleep(40); // Reaches 90% in ~3.6 seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Wait for API call to complete before going to 100%
            while (!apiCallCompleted) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Complete final 10% quickly
            while (progressStatus <= 100) {
                progressStatus += 1;

                final int currentProgress = progressStatus;
                handler.post(() -> {
                    progressBar.setProgress(currentProgress);
                    percentText.setText(currentProgress + "%");
                });

                try {
                    Thread.sleep(20);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Navigate to result screen
            handler.postDelayed(() -> {
                Intent intent = new Intent(DiagnosingActivity.this, ResultActivity.class);

                // Pass patient information
                intent.putExtra("user_id", userId);
                intent.putExtra("isForMe", isForMe);
                intent.putExtra("patientName", patientName);
                intent.putExtra("patientAge", patientAge);
                intent.putExtra("patientPhone", patientPhone);
                intent.putExtra("patientGender", patientGender);

                // Pass diagnosis results
                intent.putExtra("diagnosisLabel", diagnosisLabel);
                intent.putExtra("confidence", confidence);

                startActivity(intent);
                finish();
            }, 300);
        }).start();
    }

    /**
     * Animate message switching
     */
    private void startMessageAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressStatus < 100) {
                    // Fade out animation
                    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    fadeOut.setDuration(300);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Change message
                            currentMessageIndex = (currentMessageIndex + 1) % cheeringMessages.length;
                            messageText.setText(cheeringMessages[currentMessageIndex]);

                            // Fade in animation
                            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                            fadeIn.setDuration(300);
                            messageText.startAnimation(fadeIn);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    messageText.startAnimation(fadeOut);

                    // Schedule next message change
                    handler.postDelayed(this, 1800);
                }
            }
        }, 1800);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}