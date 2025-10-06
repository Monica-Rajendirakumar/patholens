package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class ResultActivity extends AppCompatActivity {

    private ImageView resultIcon;
    private TextView resultTitle;
    private TextView resultMessage;
    private TextView confidenceText;
    private CardView resultCard;
    private Button doneButton;
    private Button retryButton;

    private boolean isPemphigusDetected;
    private int confidence;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // Initialize views
        resultIcon = findViewById(R.id.resultIcon);
        resultTitle = findViewById(R.id.resultTitle);
        resultMessage = findViewById(R.id.resultMessage);
        confidenceText = findViewById(R.id.confidenceText);
        resultCard = findViewById(R.id.resultCard);
        doneButton = findViewById(R.id.doneButton);
        retryButton = findViewById(R.id.retryButton);

        // Get data from intent
        Intent intent = getIntent();
        isPemphigusDetected = intent.getBooleanExtra("isPemphigusDetected", false);
        confidence = intent.getIntExtra("confidence", 0);

        // Display results
        displayResults();

        // Set button listeners
        doneButton.setOnClickListener(v -> {
            Intent mainIntent = new Intent(ResultActivity.this, MainActivity.class);
            startActivity(mainIntent);
            finish();
        });
    }

    private void displayResults() {
        if (isPemphigusDetected) {
            // Positive result
            resultIcon.setImageResource(android.R.drawable.ic_dialog_alert);
            resultIcon.setColorFilter(getResources().getColor(android.R.color.holo_red_dark));
            resultTitle.setText("Pemphigus Detected");
            resultTitle.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            resultMessage.setText("Our analysis indicates the presence of pemphigus. " +
                    "Please consult with a healthcare professional for proper diagnosis and treatment.");
            resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_red_light));
            confidenceText.setText("Confidence: " + confidence + "%");
        } else {
            // Negative result
            resultIcon.setImageResource(android.R.drawable.ic_dialog_info);
            resultIcon.setColorFilter(getResources().getColor(android.R.color.holo_green_dark));
            resultTitle.setText("No Pemphigus Detected");
            resultTitle.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
            resultMessage.setText("Our analysis shows no signs of pemphigus. " +
                    "However, if you have concerns, please consult with a healthcare professional.");
            resultCard.setCardBackgroundColor(getResources().getColor(android.R.color.holo_green_light));
            confidenceText.setText("Confidence: " + confidence + "%");
        }
    }
}