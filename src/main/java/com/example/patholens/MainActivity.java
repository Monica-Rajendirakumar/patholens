package com.example.patholens;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private NavigationView sideNavigation;

    private View floatingOrb1;
    private View floatingOrb2;
    private View floatingOrb3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        sideNavigation = findViewById(R.id.side_navigation);

        floatingOrb1 = findViewById(R.id.floating_orb_1);
        floatingOrb2 = findViewById(R.id.floating_orb_2);
        floatingOrb3 = findViewById(R.id.floating_orb_3);

        startBackgroundAnimations();

        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        sideNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_diagnosis) {
                    Intent intent = new Intent(MainActivity.this, PatientInformationActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(MainActivity.this, DiagnosisHistoryActivity.class);
                    startActivity(intent);
                }
                else if (id == R.id.nav_logout) {
                    // Handle logout
                    // Clear user session/token here
                    Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
    }

    private void startBackgroundAnimations() {
        // Animate Floating Orb 1 - Slow vertical and horizontal float
        ObjectAnimator orb1TranslateY = ObjectAnimator.ofFloat(floatingOrb1, "translationY", 0f, 150f);
        orb1TranslateY.setDuration(12000);
        orb1TranslateY.setRepeatCount(ValueAnimator.INFINITE);
        orb1TranslateY.setRepeatMode(ValueAnimator.REVERSE);
        orb1TranslateY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb1TranslateY.start();

        ObjectAnimator orb1TranslateX = ObjectAnimator.ofFloat(floatingOrb1, "translationX", 0f, 80f);
        orb1TranslateX.setDuration(15000);
        orb1TranslateX.setRepeatCount(ValueAnimator.INFINITE);
        orb1TranslateX.setRepeatMode(ValueAnimator.REVERSE);
        orb1TranslateX.setInterpolator(new AccelerateDecelerateInterpolator());
        orb1TranslateX.start();

        ObjectAnimator orb2TranslateY = ObjectAnimator.ofFloat(floatingOrb2, "translationY", 0f, -100f);
        orb2TranslateY.setDuration(10000);
        orb2TranslateY.setRepeatCount(ValueAnimator.INFINITE);
        orb2TranslateY.setRepeatMode(ValueAnimator.REVERSE);
        orb2TranslateY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb2TranslateY.start();

        ObjectAnimator orb2TranslateX = ObjectAnimator.ofFloat(floatingOrb2, "translationX", 0f, -60f);
        orb2TranslateX.setDuration(13000);
        orb2TranslateX.setRepeatCount(ValueAnimator.INFINITE);
        orb2TranslateX.setRepeatMode(ValueAnimator.REVERSE);
        orb2TranslateX.setInterpolator(new AccelerateDecelerateInterpolator());
        orb2TranslateX.start();

        ObjectAnimator orb3TranslateY = ObjectAnimator.ofFloat(floatingOrb3, "translationY", 0f, 120f);
        orb3TranslateY.setDuration(14000);
        orb3TranslateY.setRepeatCount(ValueAnimator.INFINITE);
        orb3TranslateY.setRepeatMode(ValueAnimator.REVERSE);
        orb3TranslateY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb3TranslateY.start();

        ObjectAnimator orb3TranslateX = ObjectAnimator.ofFloat(floatingOrb3, "translationX", 0f, -90f);
        orb3TranslateX.setDuration(11000);
        orb3TranslateX.setRepeatCount(ValueAnimator.INFINITE);
        orb3TranslateX.setRepeatMode(ValueAnimator.REVERSE);
        orb3TranslateX.setInterpolator(new AccelerateDecelerateInterpolator());
        orb3TranslateX.start();

        ObjectAnimator orb1Scale = ObjectAnimator.ofFloat(floatingOrb1, "scaleX", 1f, 1.2f);
        orb1Scale.setDuration(8000);
        orb1Scale.setRepeatCount(ValueAnimator.INFINITE);
        orb1Scale.setRepeatMode(ValueAnimator.REVERSE);
        orb1Scale.setInterpolator(new AccelerateDecelerateInterpolator());
        orb1Scale.start();

        ObjectAnimator orb1ScaleY = ObjectAnimator.ofFloat(floatingOrb1, "scaleY", 1f, 1.2f);
        orb1ScaleY.setDuration(8000);
        orb1ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        orb1ScaleY.setRepeatMode(ValueAnimator.REVERSE);
        orb1ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb1ScaleY.start();

        ObjectAnimator orb2Scale = ObjectAnimator.ofFloat(floatingOrb2, "scaleX", 1f, 1.15f);
        orb2Scale.setDuration(9000);
        orb2Scale.setRepeatCount(ValueAnimator.INFINITE);
        orb2Scale.setRepeatMode(ValueAnimator.REVERSE);
        orb2Scale.setInterpolator(new AccelerateDecelerateInterpolator());
        orb2Scale.start();

        ObjectAnimator orb2ScaleY = ObjectAnimator.ofFloat(floatingOrb2, "scaleY", 1f, 1.15f);
        orb2ScaleY.setDuration(9000);
        orb2ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        orb2ScaleY.setRepeatMode(ValueAnimator.REVERSE);
        orb2ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb2ScaleY.start();

        ObjectAnimator orb3Scale = ObjectAnimator.ofFloat(floatingOrb3, "scaleX", 1f, 1.25f);
        orb3Scale.setDuration(10000);
        orb3Scale.setRepeatCount(ValueAnimator.INFINITE);
        orb3Scale.setRepeatMode(ValueAnimator.REVERSE);
        orb3Scale.setInterpolator(new AccelerateDecelerateInterpolator());
        orb3Scale.start();

        ObjectAnimator orb3ScaleY = ObjectAnimator.ofFloat(floatingOrb3, "scaleY", 1f, 1.25f);
        orb3ScaleY.setDuration(10000);
        orb3ScaleY.setRepeatCount(ValueAnimator.INFINITE);
        orb3ScaleY.setRepeatMode(ValueAnimator.REVERSE);
        orb3ScaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        orb3ScaleY.start();
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}