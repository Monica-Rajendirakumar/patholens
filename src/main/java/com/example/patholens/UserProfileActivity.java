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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class UserProfileActivity extends AppCompatActivity {

    private static final int EDIT_PROFILE_REQUEST = 100;
    private static final String TAG = "UserProfile";

    private CircleImageView profileImage;
    private ImageView btnBack, btnEditImage;
    private TextView tvFullName, tvAge, tvGender, tvContact;
    private TextView tvDiagnoses, tvConfidence;
    private CardView btnTerms, btnPrivacy;
    private LinearLayout btnEdit;

    private ActivityResultLauncher<Intent> imagePickerLauncher;
    private PrefsManager prefsManager;
    private ApiService apiService;

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
        setContentView(R.layout.activity_user_profile);

        initializeDependencies();
        initializeViews();
        setupImagePicker();
        fetchUserProfile();
        fetchProfileImage();
        setupClickListeners();
    }

    private void initializeDependencies() {
        prefsManager = new PrefsManager(this);
        apiService = RetrofitClient.getInstance().getApiService();
    }

    private void initializeViews() {
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
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri selectedImageUri = result.getData().getData();
                        if (selectedImageUri != null) {
                            displaySelectedImage(selectedImageUri);
                            uploadProfileImageToServer(selectedImageUri);
                        }
                    }
                }
        );
    }

    private void displaySelectedImage(Uri imageUri) {
        Glide.with(this)
                .load(imageUri)
                .placeholder(R.drawable.default_profile)
                .error(R.drawable.default_profile)
                .into(profileImage);
    }

    private void fetchUserProfile() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            loadDataFromPreferences();
            return;
        }

        apiService.getAuthenticatedUser(token).enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleProfileResponse(response.body());
                } else {
                    handleProfileError(response);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                Toast.makeText(UserProfileActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                loadDataFromPreferences();
            }
        });
    }

    private void handleProfileResponse(UserResponse userResponse) {
        if (userResponse.isStatus() && userResponse.getData() != null) {
            UserResponse.UserData userData = userResponse.getData();

            fullName = userData.getName();
            age = userData.getAge();
            gender = userData.getGender();
            contact = userData.getPhoneNumber();
            email = userData.getEmail();

            prefsManager.saveUserProfile(fullName, email, age, gender, contact);
            loadUserData();
            Toast.makeText(this, "Profile loaded successfully", Toast.LENGTH_SHORT).show();
        } else {
            String message = userResponse.getMessage() != null ?
                    userResponse.getMessage() : "Unknown error";
            Toast.makeText(this, "Error: " + message, Toast.LENGTH_SHORT).show();
            loadDataFromPreferences();
        }
    }

    private void handleProfileError(Response<UserResponse> response) {
        Toast.makeText(this, "Failed to load profile (Code: " + response.code() + ")",
                Toast.LENGTH_SHORT).show();
        loadDataFromPreferences();
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
        tvFullName.setText(fullName != null ? fullName : "N/A");
        tvAge.setText(age > 0 ? String.valueOf(age) : "N/A");
        tvGender.setText(gender != null ? capitalizeFirst(gender) : "N/A");
        tvContact.setText(contact != null ? contact : "N/A");
        tvDiagnoses.setText(String.valueOf(diagnoses));
        tvConfidence.setText(confidence + " confidence");
    }

    private void fetchProfileImage() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            return;
        }

        apiService.getProfileImage(token).enqueue(new Callback<ProfileImageResponse>() {
            @Override
            public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleImageResponse(response.body());
                }
            }

            @Override
            public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                // Silent failure for profile image
            }
        });
    }

    private void handleImageResponse(ProfileImageResponse imageResponse) {
        if (imageResponse.isSuccess() && imageResponse.getData() != null) {
            String imageUrl = imageResponse.getData().getProfileImageUrl();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(profileImage);
            }
        }
    }

    private void uploadProfileImageToServer(Uri imageUri) {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            Toast.makeText(this, "Please login again", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File tempFile = createTempFileFromUri(imageUri);
            MultipartBody.Part body = createImageMultipart(tempFile);

            Toast.makeText(this, "Uploading image...", Toast.LENGTH_SHORT).show();

            uploadImage(token, body, tempFile);
        } catch (Exception e) {
            Toast.makeText(this, "Error preparing image: " + e.getMessage(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private File createTempFileFromUri(Uri imageUri) throws Exception {
        InputStream inputStream = getContentResolver().openInputStream(imageUri);
        if (inputStream == null) {
            throw new Exception("Cannot read image file");
        }

        File tempFile = new File(getCacheDir(), "profile_image_" + System.currentTimeMillis() + ".jpg");
        FileOutputStream outputStream = new FileOutputStream(tempFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, bytesRead);
        }

        outputStream.close();
        inputStream.close();

        return tempFile;
    }

    private MultipartBody.Part createImageMultipart(File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), file);
        return MultipartBody.Part.createFormData("profile_image", file.getName(), requestFile);
    }

    private void uploadImage(String token, MultipartBody.Part body, File tempFile) {
        apiService.uploadProfileImage(token, body).enqueue(new Callback<ProfileImageResponse>() {
            @Override
            public void onResponse(Call<ProfileImageResponse> call, Response<ProfileImageResponse> response) {
                tempFile.delete();

                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(UserProfileActivity.this,
                            "Profile image updated successfully", Toast.LENGTH_SHORT).show();
                    fetchProfileImage();
                } else {
                    Toast.makeText(UserProfileActivity.this,
                            "Upload failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProfileImageResponse> call, Throwable t) {
                tempFile.delete();
                Toast.makeText(UserProfileActivity.this,
                        "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private String capitalizeFirst(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnEditImage.setOnClickListener(v -> openImagePicker());

        btnEdit.setOnClickListener(v -> openEditProfile());

        btnTerms.setOnClickListener(v -> openTermsActivity());

        btnPrivacy.setOnClickListener(v -> openPrivacyActivity());
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void openEditProfile() {
        Intent intent = new Intent(this, EditProfileActivity.class);
        intent.putExtra("fullName", fullName);
        intent.putExtra("age", String.valueOf(age));
        intent.putExtra("gender", gender);
        intent.putExtra("contact", contact);
        intent.putExtra("email", email);
        startActivityForResult(intent, EDIT_PROFILE_REQUEST);
    }

    private void openTermsActivity() {
        Intent intent = new Intent(this, TermsActivity.class);
        startActivity(intent);
    }

    private void openPrivacyActivity() {
        Intent intent = new Intent(this, PrivacyActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_PROFILE_REQUEST && resultCode == RESULT_OK) {
            fetchUserProfile();
            fetchProfileImage();
            Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        }
    }
}