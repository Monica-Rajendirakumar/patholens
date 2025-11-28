package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {

    private static final float BUTTON_ALPHA_ENABLED = 1.0f;
    private static final float BUTTON_ALPHA_DISABLED = 0.5f;

    private CheckBox checkboxUnderstand;
    private Button btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        initializeViews();
        setupListeners();
    }

    private void initializeViews() {
        checkboxUnderstand = findViewById(R.id.checkboxUnderstand);
        btnAgree = findViewById(R.id.btnAgree);
    }

    private void setupListeners() {
        checkboxUnderstand.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateButtonState(isChecked);
        });

        btnAgree.setOnClickListener(v -> navigateToLogin());
    }

    private void updateButtonState(boolean isEnabled) {
        btnAgree.setEnabled(isEnabled);
        btnAgree.setAlpha(isEnabled ? BUTTON_ALPHA_ENABLED : BUTTON_ALPHA_DISABLED);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}