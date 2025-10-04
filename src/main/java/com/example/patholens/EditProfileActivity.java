package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView btnBack;
    private EditText etFirstName, etLastName, etAge, etEmail, etPhone;
    private Spinner spinnerGender;
    private Button btnConfirm, btnChangePassword;

    private String[] genderOptions = {"Gender", "Male", "Female", "Other"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initializeViews();
        setupSpinner();
        loadExistingData();
        setupClickListeners();
    }

    private void initializeViews() {
        btnBack = findViewById(R.id.btnBack);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName = findViewById(R.id.etLastName);
        etAge = findViewById(R.id.etAge);
        etEmail = findViewById(R.id.etEmail);
        etPhone = findViewById(R.id.etPhone);
        spinnerGender = findViewById(R.id.spinnerGender);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnChangePassword = findViewById(R.id.btnChangePassword);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                genderOptions
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);
    }

    private void loadExistingData() {
        Intent intent = getIntent();
        if (intent != null) {
            String fullName = intent.getStringExtra("fullName");
            if (fullName != null && fullName.contains(" ")) {
                String[] names = fullName.split(" ", 2);
                etFirstName.setText(names[0]);
                etLastName.setText(names.length > 1 ? names[1] : "");
            } else {
                etFirstName.setText(fullName);
            }

            String age = intent.getStringExtra("age");
            if (age != null) {
                etAge.setText(age);
            }

            String gender = intent.getStringExtra("gender");
            if (gender != null) {
                int position = getGenderPosition(gender);
                spinnerGender.setSelection(position);
            }

            String contact = intent.getStringExtra("contact");
            if (contact != null && contact.length() > 4) {
                etPhone.setText(contact.substring(4).trim());
            }

            String email = intent.getStringExtra("email");
            if (email != null) {
                etEmail.setText(email);
            }
        }
    }

    private int getGenderPosition(String gender) {
        for (int i = 0; i < genderOptions.length; i++) {
            if (genderOptions[i].equalsIgnoreCase(gender)) {
                return i;
            }
        }
        return 0;
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnConfirm.setOnClickListener(v -> saveProfile());

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(EditProfileActivity.this, ChangePasswordActivity.class);
            startActivity(intent);
        });
    }

    private void saveProfile() {
        String firstName = etFirstName.getText().toString().trim();
        String lastName = etLastName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String gender = spinnerGender.getSelectedItem().toString();

        // Validation
        if (firstName.isEmpty()) {
            etFirstName.setError("First name is required");
            etFirstName.requestFocus();
            return;
        }

        if (age.isEmpty()) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return;
        }

        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            etEmail.requestFocus();
            return;
        }

        if (phone.isEmpty()) {
            etPhone.setError("Phone number is required");
            etPhone.requestFocus();
            return;
        }

        if (gender.equals("Gender")) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return;
        }

        // Combine first and last name
        String fullName = firstName + (lastName.isEmpty() ? "" : " " + lastName);
        String contact = "+234 " + phone;

        // Save to SharedPreferences or database
        saveToPreferences(fullName, age, gender, contact, email);

        // Return data to previous activity
        Intent resultIntent = new Intent();
        resultIntent.putExtra("fullName", fullName);
        resultIntent.putExtra("age", age);
        resultIntent.putExtra("gender", gender);
        resultIntent.putExtra("contact", contact);
        resultIntent.putExtra("email", email);
        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void saveToPreferences(String fullName, String age, String gender, String contact, String email) {
        getSharedPreferences("UserProfile", MODE_PRIVATE)
                .edit()
                .putString("fullName", fullName)
                .putString("age", age)
                .putString("gender", gender)
                .putString("contact", contact)
                .putString("email", email)
                .apply();
    }
}