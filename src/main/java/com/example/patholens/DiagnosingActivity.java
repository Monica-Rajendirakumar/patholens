package com.example.patholens;

import static android.media.FaceDetector.Face.CONFIDENCE_THRESHOLD;

import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.tensorflow.lite.Interpreter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class DiagnosingActivity extends AppCompatActivity {

    private static final String TAG = "DiagnosingActivity";
    private static final int PROGRESS_TARGET = 90;
    private static final int PROGRESS_MAX = 100;
    private static final int PROGRESS_DELAY_MS = 40;
    private static final int MESSAGE_DELAY_MS = 1800;
    private static final int FADE_DURATION_MS = 300;
    private static final int NAVIGATION_DELAY_MS = 300;

    // Model configuration
    private static final int INPUT_SIZE = 150; // Change based on your model
    private static final int NUM_CLASSES = 2; // 3 classes: pemphigus, no-pemphigus, unsure
    private static final String MODEL_FILE = "model.tflite"; // Your model file name in assets folder

    // Class labels - IMPORTANT: Order must match your training data
    private static final String[] CLASS_LABELS = {
            "no-pemphigus",      // Index 0
            "pemphigus",         // Index 1
            "unsure"             // Index 2
    };

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
    private boolean classificationCompleted = false;
    private int progressStatus = 0;
    private int currentMessageIndex = 0;

    private Interpreter tflite;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "=== DiagnosingActivity onCreate started ===");
        setContentView(R.layout.activity_diagnosing);

        initializeViews();
        receiveDataFromPreviousActivity();
        loadModel();

        if (tflite != null) {
            Log.d(TAG, "Model loaded successfully, starting diagnosis");
            startDiagnosing();
        } else {
            Log.e(TAG, "Model is null, cannot start diagnosis");
            Toast.makeText(this, "Failed to initialize AI model. Please try again.", Toast.LENGTH_LONG).show();

            // Still continue to show UI but with error
            startDiagnosing();
        }
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

        Log.d(TAG, "Received data from previous activity:");
        Log.d(TAG, "  userId: " + userId);
        Log.d(TAG, "  isForMe: " + isForMe);
        Log.d(TAG, "  patientName: " + patientName);
        Log.d(TAG, "  patientAge: " + patientAge);
        Log.d(TAG, "  imageUri: " + imageUriString);
    }

    private void loadModel() {
        try {
            Log.d(TAG, "Attempting to load model from assets...");
            MappedByteBuffer modelBuffer = loadModelFile();
            Log.d(TAG, "Model file loaded into buffer, size: " + modelBuffer.capacity());

            // Try with InterpreterOptions for better compatibility
            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);

            tflite = new Interpreter(modelBuffer, options);
            Log.d(TAG, "Model loaded successfully - Interpreter created");

            // Log model details
            try {
                Log.d(TAG, "Input tensor count: " + tflite.getInputTensorCount());
                Log.d(TAG, "Output tensor count: " + tflite.getOutputTensorCount());

                // Get input tensor details
                int[] inputShape = tflite.getInputTensor(0).shape();
                Log.d(TAG, "Input tensor shape: " + java.util.Arrays.toString(inputShape));
                Log.d(TAG, "Expected: [1, 150, 150, 3]");

                // Get output tensor details
                int[] outputShape = tflite.getOutputTensor(0).shape();
                Log.d(TAG, "Output tensor shape: " + java.util.Arrays.toString(outputShape));
                Log.d(TAG, "Expected: [1, 2]");

            } catch (Exception e) {
                Log.w(TAG, "Could not log tensor details: " + e.getMessage());
            }

        } catch (IllegalArgumentException e) {
            Log.e(TAG, "Model compatibility error - Full details:", e);
            String errorMsg = e.getMessage();
            if (errorMsg != null && errorMsg.contains("version")) {
                Toast.makeText(this,
                        "Model version incompatible. Please update TensorFlow Lite library or reconvert the model.",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Failed to load AI model: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error loading model - Full stack trace:", e);
            Toast.makeText(this, "Failed to load AI model: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private MappedByteBuffer loadModelFile() throws Exception {
        Log.d(TAG, "Opening model file: " + MODEL_FILE);

        // Check if file exists in assets
        String[] assetFiles = getAssets().list("");
        Log.d(TAG, "Assets folder contains: " + java.util.Arrays.toString(assetFiles));

        // Load model from assets folder
        AssetFileDescriptor fileDescriptor = getAssets().openFd(MODEL_FILE);
        Log.d(TAG, "File descriptor opened, length: " + fileDescriptor.getLength());

        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();

        Log.d(TAG, "Mapping file - Start: " + startOffset + ", Length: " + declaredLength);
        MappedByteBuffer buffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);

        inputStream.close();
        fileDescriptor.close();

        return buffer;
    }

    private void startDiagnosing() {
        startMessageAnimation();
        runLocalClassification();
        startProgressAnimation();
    }

    private void runLocalClassification() {
        new Thread(() -> {
            try {
                Log.d(TAG, "Starting classification thread");

                if (tflite == null) {
                    Log.e(TAG, "TFLite interpreter is null!");
                    handleModelNotLoadedError();
                    return;
                }

                Log.d(TAG, "Parsing image URI: " + imageUriString);
                Uri imageUri = Uri.parse(imageUriString);
                Bitmap bitmap = loadBitmapFromUri(imageUri);

                if (bitmap == null) {
                    Log.e(TAG, "Failed to load bitmap from URI");
                    handleFileError();
                    return;
                }

                Log.d(TAG, "Bitmap loaded - Width: " + bitmap.getWidth() + ", Height: " + bitmap.getHeight());

                // Preprocess the image
                Bitmap resizedBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, true);
                Log.d(TAG, "Bitmap resized to: " + INPUT_SIZE + "x" + INPUT_SIZE);

                ByteBuffer inputBuffer = convertBitmapToByteBuffer(resizedBitmap);
                Log.d(TAG, "Bitmap converted to ByteBuffer, capacity: " + inputBuffer.capacity());

                // IMPORTANT: Rewind buffer before use
                inputBuffer.rewind();

                // Prepare output array
                float[][] output = new float[1][NUM_CLASSES];

                Log.d(TAG, "Running TFLite inference...");

                // Run inference with error handling
                try {
                    tflite.run(inputBuffer, output);
                    Log.d(TAG, "Inference completed. Raw output: " + java.util.Arrays.toString(output[0]));
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Inference failed - Input/Output mismatch", e);
                    throw new Exception("Model input/output error: " + e.getMessage());
                }

                // Process results with softmax probabilities
                // output[0][0] = probability of "no-pemphigus"
                // output[0][1] = probability of "pemphigus"

                float noPemphigusProbability = output[0][0];
                float pemphigusProbability = output[0][1];

                Log.d(TAG, "No-Pemphigus probability: " + noPemphigusProbability);
                Log.d(TAG, "Pemphigus probability: " + pemphigusProbability);

                // Determine the diagnosis
                int maxIndex;
                float maxConfidence;

                if (pemphigusProbability > noPemphigusProbability) {
                    maxIndex = 1; // pemphigus
                    maxConfidence = pemphigusProbability;
                } else {
                    maxIndex = 0; // no-pemphigus
                    maxConfidence = noPemphigusProbability;
                }

                // Check if confidence is too low (unsure case)
                if (maxConfidence < CONFIDENCE_THRESHOLD) {
                    diagnosisLabel = "unsure";
                    confidence = maxConfidence * 100.0;
                    Log.d(TAG, "Low confidence - Result: UNSURE (" + confidence + "%)");
                } else {
                    diagnosisLabel = CLASS_LABELS[maxIndex];
                    confidence = maxConfidence * 100.0;
                    Log.d(TAG, "Classification successful - Label: " + diagnosisLabel + ", Confidence: " + confidence + "%");
                }

                classificationCompleted = true;

            } catch (Exception e) {
                handleClassificationError(e);
            }
        }).start();
    }

    private Bitmap loadBitmapFromUri(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (inputStream != null) {
                inputStream.close();
            }
            return bitmap;
        } catch (Exception e) {
            Log.e(TAG, "Error loading bitmap from URI", e);
            return null;
        }
    }

    private ByteBuffer convertBitmapToByteBuffer(Bitmap bitmap) {
        // Allocate buffer: 4 bytes per float * width * height * channels
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * 3);
        byteBuffer.order(ByteOrder.nativeOrder());

        int[] intValues = new int[INPUT_SIZE * INPUT_SIZE];
        bitmap.getPixels(intValues, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        int pixel = 0;
        for (int i = 0; i < INPUT_SIZE; i++) {
            for (int j = 0; j < INPUT_SIZE; j++) {
                int val = intValues[pixel++];

                // Extract RGB values
                float r = ((val >> 16) & 0xFF);
                float g = ((val >> 8) & 0xFF);
                float b = (val & 0xFF);

                // MobileNetV2 preprocessing: Normalize to [-1, 1]
                // This matches the preprocessing used during training
                byteBuffer.putFloat((r / 127.5f) - 1.0f);
                byteBuffer.putFloat((g / 127.5f) - 1.0f);
                byteBuffer.putFloat((b / 127.5f) - 1.0f);
            }
        }

        return byteBuffer;
    }

    private void handleModelNotLoadedError() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Model not loaded. Please check if model.tflite is in assets folder.", Toast.LENGTH_LONG).show();
            diagnosisLabel = "Model Error";
            confidence = 0.0;
            classificationCompleted = true;
        });
    }

    private void handleFileError() {
        runOnUiThread(() -> {
            Toast.makeText(this, "Failed to process image", Toast.LENGTH_SHORT).show();
            diagnosisLabel = "Image Error";
            confidence = 0.0;
            classificationCompleted = true;
        });
    }

    private void handleClassificationError(Exception e) {
        Log.e(TAG, "Classification Error: " + e.getMessage(), e);

        runOnUiThread(() -> {
            Toast.makeText(this, "Classification error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            // Set default values instead of leaving them null
            diagnosisLabel = "Error";
            confidence = 0.0;
            classificationCompleted = true;
        });
    }

    private void startProgressAnimation() {
        new Thread(() -> {
            animateProgressToTarget();
            waitForClassificationCompletion();
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

    private void waitForClassificationCompletion() {
        while (!classificationCompleted) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Log.e(TAG, "Classification wait interrupted", e);
            }
        }
    }

    private void completeProgress() {
        while (progressStatus < PROGRESS_MAX) {
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
        if (tflite != null) {
            tflite.close();
        }
    }
}