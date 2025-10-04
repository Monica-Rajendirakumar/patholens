package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.textfield.TextInputEditText;

public class RegistrationActivity extends AppCompatActivity {

    private TextInputEditText etName, etAge, etContact, etAddress, etEmail, etPassword;
    private Spinner spinnerGender;
    private Button btnRegister;
    private TextView tvLogin;
    private String selectedGender = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        // Initialize views
        etName = findViewById(R.id.etName);
        etAge = findViewById(R.id.etAge);
        etContact = findViewById(R.id.etContact);
        spinnerGender = findViewById(R.id.spinnerGender);
        etAddress = findViewById(R.id.etAddress);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnRegister = findViewById(R.id.btnRegister);
        tvLogin = findViewById(R.id.tvLogin);

        // Setup gender spinner
        setupGenderSpinner();

        // Register button click
        btnRegister.setOnClickListener(v -> {
            if (validateInput()) {
                // TODO: Implement your registration logic here
                // Save user data to database or send to server

                String name = etName.getText().toString().trim();
                String age = etAge.getText().toString().trim();
                String contact = etContact.getText().toString().trim();
                String address = etAddress.getText().toString().trim();
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                Toast.makeText(RegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                finish();
            }
        });

        // Login link click
        tvLogin.setOnClickListener(v -> {
            finish(); // Go back to login
        });
    }

    private void setupGenderSpinner() {
        String[] genders = {"Select Gender", "Male", "Female", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, genders);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        spinnerGender.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedGender = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                selectedGender = "";
            }
        });
    }

    private boolean validateInput() {
        String name = etName.getText().toString().trim();
        String age = etAge.getText().toString().trim();
        String contact = etContact.getText().toString().trim();
        String address = etAddress.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            etName.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(age)) {
            etAge.setError("Age is required");
            etAge.requestFocus();
            return false;
        }

        int ageValue = Integer.parseInt(age);
        if (ageValue < 1 || ageValue > 120) {
            etAge.setError("Please enter a valid age");
            etAge.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(contact)) {
            etContact.setError("Contact is required");
            etContact.requestFocus();
            return false;
        }

        if (contact.length() < 10) {
            etContact.setError("Please enter a valid contact number");
            etContact.requestFocus();
            return false;
        }

        if (selectedGender.equals("Select Gender") || TextUtils.isEmpty(selectedGender)) {
            Toast.makeText(this, "Please select a gender", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(address)) {
            etAddress.setError("Address is required");
            etAddress.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Please enter a valid email");
            etEmail.requestFocus();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            etPassword.requestFocus();
            return false;
        }

        return true;
    }
}