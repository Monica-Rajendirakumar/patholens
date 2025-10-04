package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {

    private CheckBox checkboxUnderstand;
    private Button btnAgree;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        checkboxUnderstand = findViewById(R.id.checkboxUnderstand);
        btnAgree = findViewById(R.id.btnAgree);

        // Enable button only when checkbox is checked
        checkboxUnderstand.setOnCheckedChangeListener((buttonView, isChecked) -> {
            btnAgree.setEnabled(isChecked);
            if (isChecked) {
                btnAgree.setAlpha(1.0f);
            } else {
                btnAgree.setAlpha(0.5f);
            }
        });

        // Navigate to login when agree is clicked
        btnAgree.setOnClickListener(v -> {
            Intent intent = new Intent(DisclaimerActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }
}