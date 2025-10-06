package com.example.patholens;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import java.util.Random;

public class DiagnosingActivity extends AppCompatActivity {

    private ProgressBar progressBar;
    private TextView messageText;
    private Handler handler;
    private int progressStatus = 0;
    private String[] cheeringMessages = {
            "Analyzing image patterns...",
            "Almost there! Hang tight...",
            "Processing medical data...",
            "Our AI is working hard...",
            "Examining the details...",
            "Just a moment more...",
            "Getting your results ready...",
            "Finalizing diagnosis..."
    };
    private Random random;
    private int currentMessageIndex = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_diagnosing);

        progressBar = findViewById(R.id.progressBar);
        messageText = findViewById(R.id.messageText);
        handler = new Handler();
        random = new Random();

        startDiagnosing();
    }

    private void startDiagnosing() {
        // Start progress bar animation
        new Thread(() -> {
            while (progressStatus < 100) {
                progressStatus += 1;
                handler.post(() -> progressBar.setProgress(progressStatus));

                try {
                    Thread.sleep(50); // Adjust speed here (50ms for ~5 seconds total)
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            // Navigate to result page after completion
            handler.postDelayed(() -> {
                Intent intent = new Intent(DiagnosingActivity.this, ResultActivity.class);
                // TODO: Pass actual result from backend
                intent.putExtra("isPemphigusDetected", random.nextBoolean()); // Mock result
                intent.putExtra("confidence", 85 + random.nextInt(15)); // Mock confidence
                startActivity(intent);
                finish();
            }, 500);
        }).start();

        // Start message switching animation
        startMessageAnimation();
    }

    private void startMessageAnimation() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (progressStatus < 100) {
                    // Fade out animation
                    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
                    fadeOut.setDuration(300);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {}

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            // Change message
                            currentMessageIndex = (currentMessageIndex + 1) % cheeringMessages.length;
                            messageText.setText(cheeringMessages[currentMessageIndex]);

                            // Fade in animation
                            AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
                            fadeIn.setDuration(300);
                            messageText.startAnimation(fadeIn);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {}
                    });
                    messageText.startAnimation(fadeOut);

                    // Schedule next message change
                    handler.postDelayed(this, 1500);
                }
            }
        }, 1500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}