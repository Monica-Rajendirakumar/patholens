package com.example.patholens;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class ImageUploadActivity extends AppCompatActivity {

    private String userId;
    private boolean isForMe;
    private String patientName;
    private String patientAge;
    private String patientPhone;
    private String patientGender;

    private ImageView ivPreview;
    private Button btnSelectImage, btnSubmit;
    private Uri imageUri;
    private boolean imageSelected = false;

    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Intent> pickImageLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_upload);

        receivePatientInformation();
        initializeViews();
        initializeLaunchers();
        showImportantAlert();
        setupListeners();
    }

    private void receivePatientInformation() {
        Intent intent = getIntent();
        userId = intent.getStringExtra("user_id");
        isForMe = intent.getBooleanExtra("isForMe", true);
        patientName = intent.getStringExtra("patientName");
        patientAge = intent.getStringExtra("patientAge");
        patientPhone = intent.getStringExtra("patientPhone");
        patientGender = intent.getStringExtra("patientGender");
    }

    private void initializeViews() {
        ivPreview = findViewById(R.id.ivPreview);
        btnSelectImage = findViewById(R.id.btnSelectImage);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void initializeLaunchers() {
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        openGallery();
                    } else {
                        Toast.makeText(this, "Storage permission denied. Cannot access images.",
                                Toast.LENGTH_SHORT).show();
                    }
                });

        pickImageLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        handleImageSelection(result.getData().getData());
                    }
                });
    }

    private void handleImageSelection(Uri uri) {
        imageUri = uri;
        ivPreview.setImageURI(imageUri);
        imageSelected = true;
        Toast.makeText(this, "Image selected successfully", Toast.LENGTH_SHORT).show();
    }

    private void showImportantAlert() {
        new AlertDialog.Builder(this)
                .setTitle("⚠️ Important Notice")
                .setMessage(getImportantNoticeMessage())
                .setPositiveButton("I Understand", null)
                .setCancelable(false)
                .show();
    }

    private String getImportantNoticeMessage() {
        return "Please upload ONLY microscopic images of Pemphigus disease.\n\n" +
                "Uploading incorrect images (such as clinical photos, unrelated skin conditions, " +
                "or non-microscopic images) may lead to:\n\n" +
                "• Misdiagnosis\n" +
                "• Wrong treatment recommendations\n" +
                "• Rejection of your submission\n\n" +
                "Ensure the image is a clear microscopic view taken through a microscope.";
    }

    private void setupListeners() {
        btnSelectImage.setOnClickListener(v -> checkPermissionAndOpenGallery());

        btnSubmit.setOnClickListener(v -> {
            if (!imageSelected) {
                Toast.makeText(this, "Please upload a microscopic image first",
                        Toast.LENGTH_SHORT).show();
                return;
            }
            showConfirmationDialog();
        });
    }

    private void checkPermissionAndOpenGallery() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            checkAndRequestPermission(Manifest.permission.READ_MEDIA_IMAGES);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            openGallery();
        } else {
            checkAndRequestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void checkAndRequestPermission(String permission) {
        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestPermissionLauncher.launch(permission);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        pickImageLauncher.launch(intent);
    }

    private void showConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Submission")
                .setMessage("Are you sure this is a microscopic image of Pemphigus disease?\n\n" +
                        "Submitting incorrect images may result in rejection.")
                .setPositiveButton("Yes, Submit", (dialog, which) -> navigateToDiagnosing())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void navigateToDiagnosing() {
        Intent intent = new Intent(this, DiagnosingActivity.class);

        addPatientInformation(intent);
        intent.putExtra("imageUri", imageUri.toString());

        startActivity(intent);
        finish();
    }

    private void addPatientInformation(Intent intent) {
        intent.putExtra("user_id", userId);
        intent.putExtra("isForMe", isForMe);
        intent.putExtra("patientName", patientName);
        intent.putExtra("patientAge", patientAge);
        intent.putExtra("patientPhone", patientPhone);
        intent.putExtra("patientGender", patientGender);
    }
}