package com.example.patholens;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.Patient;
import com.example.patholens.modules.PatientResponse;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final String DEFAULT_USER_NAME   = "User";
    private static final String GUEST_NAME          = "Guest";
    private static final String WELCOME_MESSAGE     = "Welcome";
    private static final String WELCOME_BACK_MESSAGE = "Welcome Back";
    private static final String STATUS_SUCCESS      = "success";

    private static final float ORB_SCALE_MIN = 1f;
    private static final float ORB_SCALE_MAX = 1.2f;

    // ── Core UI ──────────────────────────────────────────────────────
    private DrawerLayout     drawerLayout;
    private ImageView        menuIcon;
    private NavigationView   sideNavigation;
    private TextView         tvWelcome;
    private TextView         tvUserName;

    // ── Diagnostics Summary Card ──────────────────────────────────────
    // Root view of the inflated item_diagnosis_summary.xml
    private View             summaryCardRoot;
    private ProgressBar      summaryLoadingBar;   // a small bar shown while fetching

    // ── Background orbs ───────────────────────────────────────────────
    private View floatingOrb1;
    private View floatingOrb2;
    private View floatingOrb3;

    private PrefsManager prefsManager;

    // ─────────────────────────────────────────────────────────────────
    // Lifecycle
    // ─────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);

        initializeViews();
        setupWelcomeMessage();
        setupNavigationDrawer();
        fetchUserProfile();
        fetchDiagnosisSummary();   // replaces fetchNews()
        startBackgroundAnimations();
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserProfile();
        fetchDiagnosisSummary();   // refresh stats each time user returns
    }

    // ─────────────────────────────────────────────────────────────────
    // View initialisation
    // ─────────────────────────────────────────────────────────────────

    private void initializeViews() {
        drawerLayout   = findViewById(R.id.drawer_layout);
        menuIcon       = findViewById(R.id.menu_icon);
        sideNavigation = findViewById(R.id.side_navigation);
        tvWelcome      = findViewById(R.id.tv_welcome);
        tvUserName     = findViewById(R.id.user_name);
        floatingOrb1   = findViewById(R.id.floating_orb_1);
        floatingOrb2   = findViewById(R.id.floating_orb_2);
        floatingOrb3   = findViewById(R.id.floating_orb_3);

        // ── Diagnostics summary card ──────────────────────────────
        // The card is declared directly in activity_main.xml where the old
        // RecyclerView + "Latest Research" section used to live.
        // Use an <include> tag pointing to item_diagnosis_summary.xml, e.g.:
        //
        //   <include
        //       android:id="@+id/diagnosis_summary_card"
        //       layout="@layout/item_diagnosis_summary" />
        //
        // Then find it here:
        summaryCardRoot  = findViewById(R.id.diagnosis_summary_card);
        summaryLoadingBar = findViewById(R.id.summary_loading_bar); // optional shimmer/progress

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    // ─────────────────────────────────────────────────────────────────
    // Welcome message
    // ─────────────────────────────────────────────────────────────────

    private void setupWelcomeMessage() {
        boolean isNewUser = prefsManager.isNewUser();
        tvWelcome.setText(isNewUser ? WELCOME_MESSAGE : WELCOME_BACK_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────
    // Navigation Drawer
    // ─────────────────────────────────────────────────────────────────

    private void setupNavigationDrawer() {
        sideNavigation.setNavigationItemSelectedListener(item -> {
            handleNavigationItemClick(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void handleNavigationItemClick(int itemId) {
        if      (itemId == R.id.nav_profile)   navigateToActivity(UserProfileActivity.class);
        else if (itemId == R.id.nav_diagnosis) navigateToActivity(PatientInformationActivity.class);
        else if (itemId == R.id.nav_history)   navigateToActivity(DiagnosisHistoryActivity.class);
        else if (itemId == R.id.nav_logout)    performLogout();
    }

    private void navigateToActivity(Class<?> activityClass) {
        startActivity(new Intent(this, activityClass));
    }

    private void performLogout() {
        prefsManager.clearAll();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    // ─────────────────────────────────────────────────────────────────
    // User profile
    // ─────────────────────────────────────────────────────────────────

    private void fetchUserProfile() {
        String token = prefsManager.getBearerToken();
        if (token == null) { tvUserName.setText(GUEST_NAME); return; }

        RetrofitClient.getInstance()
                .getApiService()
                .getAuthenticatedUser(token)
                .enqueue(createUserProfileCallback());
    }

    private Callback<UserResponse> createUserProfileCallback() {
        return new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null)
                    handleUserProfileResponse(response.body());
                else
                    displaySavedUserName();
            }
            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
                displaySavedUserName();
            }
        };
    }

    private void handleUserProfileResponse(UserResponse userResponse) {
        if (userResponse.isStatus() && userResponse.getData() != null) {
            UserResponse.UserData user = userResponse.getData();
            String name = user.getName();
            tvUserName.setText(name != null ? name : DEFAULT_USER_NAME);
            saveUserProfile(user);
        } else {
            displaySavedUserName();
        }
    }

    private void saveUserProfile(UserResponse.UserData user) {
        prefsManager.saveUserProfile(
                user.getName(), user.getEmail(),
                user.getAge(),  user.getGender(), user.getPhoneNumber());
    }

    private void displaySavedUserName() {
        String saved = prefsManager.getUserName();
        tvUserName.setText(saved != null ? saved : DEFAULT_USER_NAME);
    }

    // ─────────────────────────────────────────────────────────────────
    // Diagnosis Summary  (replaces the old news fetch)
    // ─────────────────────────────────────────────────────────────────

    private void fetchDiagnosisSummary() {
        String token = prefsManager.getBearerToken();
        if (token == null) return;

        // Show loading state
        if (summaryCardRoot  != null) summaryCardRoot.setAlpha(0.4f);
        if (summaryLoadingBar != null) summaryLoadingBar.setVisibility(View.VISIBLE);

        String userId = String.valueOf(prefsManager.getUserId());

        RetrofitClient.getInstance()
                .getApiService()
                .getUserPatientHistory(userId, token)
                .enqueue(createSummaryCallback());
    }

    private Callback<PatientResponse> createSummaryCallback() {
        return new Callback<PatientResponse>() {
            @Override
            public void onResponse(Call<PatientResponse> call, Response<PatientResponse> response) {
                if (summaryLoadingBar != null) summaryLoadingBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    processSummaryResponse(response.body());
                } else {
                    Log.e(TAG, "Summary fetch failed: " + response.code());
                    if (summaryCardRoot != null) summaryCardRoot.setAlpha(1f);
                }
            }

            @Override
            public void onFailure(Call<PatientResponse> call, Throwable t) {
                Log.e(TAG, "Summary network error: " + t.getMessage(), t);
                if (summaryLoadingBar != null) summaryLoadingBar.setVisibility(View.GONE);
                if (summaryCardRoot != null) summaryCardRoot.setAlpha(1f);
            }
        };
    }

    private void processSummaryResponse(PatientResponse patientResponse) {
        if (!STATUS_SUCCESS.equalsIgnoreCase(patientResponse.getStatus())) return;

        List<Patient> patients = patientResponse.getData();
        if (patients == null) return;

        if (summaryCardRoot != null) {
            DiagnosticsSummaryCard.populate(summaryCardRoot, patients);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Background orb animations (unchanged)
    // ─────────────────────────────────────────────────────────────────

    private void startBackgroundAnimations() {
        animateOrb(floatingOrb1,  150f,  80f, 12000, 15000,  8000);
        animateOrb(floatingOrb2, -100f, -60f, 10000, 13000,  9000);
        animateOrb(floatingOrb3,  120f, -90f, 14000, 11000, 10000);
    }

    private void animateOrb(View orb, float translateY, float translateX,
                            long durationY, long durationX, long scaleDuration) {
        startTranslationYAnimation(orb, translateY, durationY);
        startTranslationXAnimation(orb, translateX, durationX);
        startScaleAnimation(orb, scaleDuration);
    }

    private void startTranslationYAnimation(View orb, float translateY, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(orb, "translationY", 0f, translateY);
        configureInfiniteAnimator(animator, duration);
        animator.start();
    }

    private void startTranslationXAnimation(View orb, float translateX, long duration) {
        ObjectAnimator animator = ObjectAnimator.ofFloat(orb, "translationX", 0f, translateX);
        configureInfiniteAnimator(animator, duration);
        animator.start();
    }

    private void startScaleAnimation(View orb, long duration) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(orb, "scaleX", ORB_SCALE_MIN, ORB_SCALE_MAX);
        configureInfiniteAnimator(scaleX, duration);
        scaleX.start();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(orb, "scaleY", ORB_SCALE_MIN, ORB_SCALE_MAX);
        configureInfiniteAnimator(scaleY, duration);
        scaleY.start();
    }

    private void configureInfiniteAnimator(ObjectAnimator animator, long duration) {
        animator.setDuration(duration);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
    }

    // ─────────────────────────────────────────────────────────────────
    // Back press
    // ─────────────────────────────────────────────────────────────────

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
            drawerLayout.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }
}