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
import com.example.patholens.api.ApiService;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;
import com.example.patholens.modules.ProfileImageResponse;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserProfileActivity extends AppCompatActivity {

    private CircleImageView profileImage;
    private ImageView btnBack, btnEditImage;
    private TextView tvFullName, tvAge, tvGender, tvContact;
    private TextView tvDiagnoses, tvConfidence;
    private CardView btnTerms, btnPrivacy;
    private LinearLayout btnEdit;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private Uri selectedImageUri;

    private PrefsManager prefsManager;
    private ApiService apiService;

    // User data
    private String fullName = "";
    private int age = 0;
    private String gender = "";
    private String contact = "";
    private String email = "";
    private int diagnoses = 3;
    private String confidence = "93%";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_user_profile);

            prefsManager = new PrefsManager(this);
            apiService = RetrofitClient.getInstance().getApiService();

            initializeViews();
            setupImagePicker();
            fetchUserProfile();
            fetchProfileImage();
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
                                // Show image immediately
                                Glide.with(this)
                                        .load(selectedImageUri)
                                        .placeholder(R.drawable.default_profile)
                                        .error(R.drawable.default_profile)
                                        .into(profileImage);

                                // Upload to server
                                uploadProfileImageToServer(selectedImageUri);
                            } catch (Exception e) {
                                Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }
        );
    }

    private void fetchUserProfile() {
        String token = prefsManager.getBearerToken();

        android.util.Log.d("UserProfile", "=== FETCH PROFILE DEBUG ===");
        android.util.Log.d("UserProfile", "Token: " + (token != null ? token.substring(0, Math.min(30, token.length())) + "..." : "NULL"));
        android.util.Log.d("UserProfile", "User ID: " + prefsManager.getUserId());

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getAuthenticatedUser(token).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                android.util.Log.d("UserProfile", "Response Code: " + response.code());
                android.util.Log.d("UserProfile", "Response URL: " + call.request().url());

                if (response.isSuccessful() && response.body() != null) {
                    UserResponse userResponse = response.body();
                    android.util.Log.d("UserProfile", "Status: " + userResponse.isStatus());

                    if (userResponse.isStatus() && userResponse.getData() != null) {
                        UserResponse.UserData userData = userResponse.getData();

                        android.util.Log.d("UserProfile", "User Data Received:");
                        android.util.Log.d("UserProfile", "Name: " + userData.getName());
                        android.util.Log.d("UserProfile", "Email: " + userData.getEmail());
                        android.util.Log.d("UserProfile", "Age: " + userData.getAge());
                        android.util.Log.d("UserProfile", "Gender: " + userData.getGender());
                        android.util.Log.d("UserProfile", "Phone: " + userData.getPhoneNumber());

                        // Update local data
                        fullName = userData.getName();
                        age = userData.getAge();
                        gender = userData.getGender();
                        contact = userData.getPhoneNumber();
                        email = userData.getEmail();

                        // Save to preferences
                        prefsManager.saveUserProfile(fullName, email, age, gender, contact);

                        // Update UI
                        loadUserData();
                        Toast.makeText(UserProfileActivity.this,
                                "Profile loaded successfully", Toast.LENGTH_SHORT).show();
                    } else {
                        String message = userResponse.getMessage() != null ?
                                userResponse.getMessage() : "Unknown error";
                        android.util.Log.e("UserProfile", "API Error: " + message);
                        Toast.makeText(UserProfileActivity.this,
                                "Error: " + message, Toast.LENGTH_LONG).show();
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        android.util.Log.e("UserProfile", "HTTP Error " + response.code() + ": " + errorBody);
                        Toast.makeText(UserProfileActivity.this,
                                "Failed to load profile (Code: " + response.code() + ")", Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        android.util.Log.e("UserProfile", "Error reading error body", e);
                        Toast.makeText(UserProfileActivity.this,
                                "Failed to load profile", Toast.LENGTH_SHORT).show();
                    }

                    // Load from preferences as fallback
                    loadDataFromPreferences();
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                android.util.Log.e("UserProfile", "Network Failure", t);
                android.util.Log.e("UserProfile", "Request URL: " + call.request().url());
                android.util.Log.e("UserProfile", "Error Message: " + t.getMessage());

                Toast.makeText(UserProfileActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                // Load from preferences as fallback
                loadDataFromPreferences();
            }
        });
    }

    private void loadDataFromPreferences() {
        fullName = prefsManager.getUserName();
        age = prefsManager.getUserAge();
        gender = prefsManager.getUserGender();
        contact = prefsManager.getUserPhone();
        email = prefsManager.getUserEmail();

        loadUserData();
    }

    private void loadUserData() {
        try {
            if (tvFullName != null) tvFullName.setText(fullName != null ? fullName : "N/A");
            if (tvAge != null) tvAge.setText(age > 0 ? String.valueOf(age) : "N/A");
            if (tvGender != null) tvGender.setText(gender != null ? capitalizeFirst(gender) : "N/A");
            if (tvContact != null) tvContact.setText(contact != null ? contact : "N/A");
            if (tvDiagnoses != null) tvDiagnoses.setText(String.valueOf(diagnoses));
            if (tvConfidence != null) tvConfidence.setText(confidence + " confidence");
        } catch (Exception e) {
            Toast.makeText(this, "Error loading data", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void fetchProfileImage() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            android.util.Log.d("ProfileImage", "No token available");
            return;
        }

        android.util.Log.d("ProfileImage", "Fetching profile image...");

        apiService.getProfileImage(token).enqueue(new Callback<ProfileImageResponse>() {
            @Override
            public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                android.util.Log.d("ProfileImage", "Response code: " + response.code());

                if (response.isSuccessful() && response.body() != null) {
                    ProfileImageResponse imageResponse = response.body();
                    android.util.Log.d("ProfileImage", "Response success: " + imageResponse.isSuccess());

                    if (imageResponse.isSuccess() && imageResponse.getData() != null) {
                        String imageUrl = imageResponse.getData().getProfileImageUrl();
                        android.util.Log.d("ProfileImage", "Image URL: " + imageUrl);

                        if (imageUrl != null && !imageUrl.isEmpty() && profileImage != null) {
                            android.util.Log.d("ProfileImage", "Loading image with Glide");
                            Glide.with(UserProfileActivity.this)
                                    .load(imageUrl)
                                    .placeholder(R.drawable.default_profile)
                                    .error(R.drawable.default_profile)
                                    .into(profileImage);
                        } else {
                            android.util.Log.d("ProfileImage", "Image URL is null or empty");
                        }
                    } else {
                        android.util.Log.d("ProfileImage", "No profile image data");
                    }
                } else {
                    android.util.Log.d("ProfileImage", "Response not successful");
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "No error body";
                        android.util.Log.d("ProfileImage", "Error: " + errorBody);
                    } catch (Exception e) {
                        android.util.Log.e("ProfileImage", "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                android.util.Log.e("ProfileImage", "Failed to fetch image: " + t.getMessage(), t);
            }
        });
    }

    private void uploadProfileImageToServer(Uri imageUri) {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Get file from URI
            java.io.InputStream inputStream = getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Toast.makeText(this, "Cannot read image file", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create temp file
            java.io.File tempFile = new java.io.File(getCacheDir(), "profile_image_" + System.currentTimeMillis() + ".jpg");
            java.io.FileOutputStream outputStream = new java.io.FileOutputStream(tempFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            outputStream.close();
            inputStream.close();

            // Create RequestBody
            okhttp3.RequestBody requestFile = okhttp3.RequestBody.create(
                    okhttp3.MediaType.parse("image/*"),
                    tempFile
            );

            okhttp3.MultipartBody.Part body = okhttp3.MultipartBody.Part.createFormData(
                    "profile_image",
                    tempFile.getName(),
                    requestFile
            );

            // Show loading
            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

            // Upload
            apiService.uploadProfileImage(token, body).enqueue(new Callback<ProfileImageResponse>() {
                @Override
                public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                    // Delete temp file
                    tempFile.delete();

                    android.util.Log.d("ProfileImage", "Upload response code: " + response.code());

                    if (response.isSuccessful() && response.body() != null) {
                        ProfileImageResponse imageResponse = response.body();

                        if (imageResponse.isSuccess()) {
                            Toast.makeText(UserProfileActivity.this,
                                    "Profile image updated successfully",
                                    Toast.LENGTH_SHORT).show();

                            // Refresh the profile image immediately
                            fetchProfileImage();

                            android.util.Log.d("ProfileImage", "Upload successful");
                        } else {
                            Toast.makeText(UserProfileActivity.this,
                                    "Failed: " + imageResponse.getMessage(),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(UserProfileActivity.this,
                                "Upload failed. Please try again.",
                                Toast.LENGTH_SHORT).show();
                        android.util.Log.e("ProfileImage", "Response code: " + response.code());
                        try {
                            String errorBody = response.errorBody() != null ?
                                    response.errorBody().string() : "No error body";
                            android.util.Log.e("ProfileImage", "Error: " + errorBody);
                        } catch (Exception e) {
                            android.util.Log.e("ProfileImage", "Error reading error body", e);
                        }
                    }
                }

                @Override
                public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                    tempFile.delete();
                    Toast.makeText(UserProfileActivity.this,
                            "Network error: " + t.getMessage(),
                            Toast.LENGTH_SHORT).show();
                    android.util.Log.e("ProfileImage", "Upload failed: " + t.getMessage(), t);
                }
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error preparing image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            android.util.Log.e("ProfileImage", "Error preparing image", e);
        }
    }


    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) return text;
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
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
                        intent.putExtra("age", String.valueOf(age));
                        intent.putExtra("gender", gender);
                        intent.putExtra("contact", contact);
                        intent.putExtra("email", email);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            // Refresh profile data and image after edit
            fetchUserProfile();
            fetchProfileImage();
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}