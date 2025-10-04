package com.example.patholens;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.bumptech.glide.Glide;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private ImageView btnBack, btnEditImage;
    private TextView tvFullName, tvAge, tvGender, tvContact, tvAddress;
    private TextView tvDiagnoses, tvConfidence;
    private CardView btnTerms, btnPrivacy;
    private LinearLayout btnEdit;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    // User data
    private String fullName = "user_name";
    private String age = "20";
    private String gender = "Female";
    private String contact = "+0000";
    private String address = "123 Main st, Anytown, USA";
    private int diagnoses = 3;
    private String confidence = "93%";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_user_profile);
            initializeViews();
            setupImagePicker();
            loadUserData();
            setupClickListeners();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeViews() {
        try {
            profileImage = findViewById(R.id.profileImage);
            btnBack = findViewById(R.id.btnBack);
            btnEditImage = findViewById(R.id.btnEditImage);
            btnEdit = findViewById(R.id.btnEdit);

            tvFullName = findViewById(R.id.tvFullName);
            tvAge = findViewById(R.id.tvAge);
            tvGender = findViewById(R.id.tvGender);
            tvContact = findViewById(R.id.tvContact);
            tvAddress = findViewById(R.id.tvAddress);
            tvDiagnoses = findViewById(R.id.tvDiagnoses);
            tvConfidence = findViewById(R.id.tvConfidence);

            btnTerms = findViewById(R.id.btnTerms);
            btnPrivacy = findViewById(R.id.btnPrivacy);
        } catch (Exception e) {
            Toast.makeText(this, "View initialization error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedImageUri = result.getData().getData();
                        if (profileImage != null && selectedImageUri != null) {
                            try {
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .into(profileImage);
                                saveProfileImage(selectedImageUri);
                            } catch (Exception e) {
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void loadUserData() {
        try {
            if (tvFullName != null) tvFullName.setText(fullName);
            if (tvAge != null) tvAge.setText(age);
            if (tvGender != null) tvGender.setText(gender);
            if (tvContact != null) tvContact.setText(contact);
            if (tvAddress != null) tvAddress.setText(address);
            if (tvDiagnoses != null) tvDiagnoses.setText(String.valueOf(diagnoses));
            if (tvConfidence != null) tvConfidence.setText(confidence + " confidence");
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        if (btnBack != null) {
            btnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    finish();
                }
            });
        }

        if (btnEditImage != null) {
            btnEditImage.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    openImagePicker();
                }
            });
        }

        if (btnEdit != null) {
            btnEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
                        intent.putExtra("fullName", fullName);
                        intent.putExtra("age", age);
                        intent.putExtra("gender", gender);
                        intent.putExtra("contact", contact);
                        intent.putExtra("email", "");
                        startActivityForResult(intent, 100);
                    } catch (Exception e) {
                        Toast.makeText(UserProfileActivity.this, "EditProfileActivity not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnTerms != null) {
            btnTerms.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(UserProfileActivity.this, TermsActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(UserProfileActivity.this, "TermsActivity not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        if (btnPrivacy != null) {
            btnPrivacy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(UserProfileActivity.this, PrivacyActivity.class);
                        startActivity(intent);
                    } catch (Exception e) {
                        Toast.makeText(UserProfileActivity.this, "PrivacyActivity not found", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private void openImagePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Cannot open image picker", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveProfileImage(Uri imageUri) {
        try {
            getSharedPreferences("UserProfile", MODE_PRIVATE)
                    .edit()
                    .putString("profileImageUri", imageUri.toString())
                    .apply();
            Toast.makeText(this, "Profile picture updated", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Error saving image", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            try {
                fullName = data.getStringExtra("fullName");
                age = data.getStringExtra("age");
                gender = data.getStringExtra("gender");
                contact = data.getStringExtra("contact");
                loadUserData();
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                Toast.makeText(this, "Error updating profile", Toast.LENGTH_SHORT).show();
            }
        }
    }
}