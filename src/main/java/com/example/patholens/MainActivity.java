package com.example.patholens;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.patholens.adapters.NewsAdapter;
import com.example.patholens.api.RetrofitClient;
import com.example.patholens.modules.NewsResponse;
import com.example.patholens.modules.UserResponse;
import com.example.patholens.utils.PrefsManager;
import com.google.android.material.navigation.NavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private static final String DEFAULT_USER_NAME = "User";
    private static final String GUEST_NAME = "Guest";
    private static final String WELCOME_MESSAGE = "Welcome";
    private static final String WELCOME_BACK_MESSAGE = "Welcome Back";

    private static final float ORB_SCALE_MIN = 1f;
    private static final float ORB_SCALE_MAX = 1.2f;

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private NavigationView sideNavigation;
    private TextView tvWelcome;
    private TextView tvUserName;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    private View floatingOrb1;
    private View floatingOrb2;
    private View floatingOrb3;

    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);

        initializeViews();
        setupWelcomeMessage();
        setupRecyclerView();
        setupNavigationDrawer();
        fetchUserProfile();
        fetchNews();
        startBackgroundAnimations();
    }

    private void initializeViews() {
        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        sideNavigation = findViewById(R.id.side_navigation);
        tvWelcome = findViewById(R.id.tv_welcome);
        tvUserName = findViewById(R.id.user_name);
        newsRecyclerView = findViewById(R.id.news_recycler_view);
        floatingOrb1 = findViewById(R.id.floating_orb_1);
        floatingOrb2 = findViewById(R.id.floating_orb_2);
        floatingOrb3 = findViewById(R.id.floating_orb_3);

        menuIcon.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.START));
    }

    private void setupWelcomeMessage() {
        boolean isNewUser = prefsManager.isNewUser();
        tvWelcome.setText(isNewUser ? WELCOME_MESSAGE : WELCOME_BACK_MESSAGE);
    }

    private void setupRecyclerView() {
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setHasFixedSize(true);
    }

    private void setupNavigationDrawer() {
        sideNavigation.setNavigationItemSelectedListener(item -> {
            handleNavigationItemClick(item.getItemId());
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void handleNavigationItemClick(int itemId) {
        if (itemId == R.id.nav_profile) {
            navigateToActivity(UserProfileActivity.class);
        } else if (itemId == R.id.nav_diagnosis) {
            navigateToActivity(PatientInformationActivity.class);
        } else if (itemId == R.id.nav_history) {
            navigateToActivity(DiagnosisHistoryActivity.class);
        } else if (itemId == R.id.nav_logout) {
            performLogout();
        }
    }

    private void navigateToActivity(Class<?> activityClass) {
        Intent intent = new Intent(this, activityClass);
        startActivity(intent);
    }

    private void performLogout() {
        prefsManager.clearAll();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
    }

    private void fetchUserProfile() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            tvUserName.setText(GUEST_NAME);
            return;
        }

        RetrofitClient.getInstance()
                .getApiService()
                .getAuthenticatedUser(token)
                .enqueue(createUserProfileCallback());
    }

    private Callback<UserResponse> createUserProfileCallback() {
        return new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleUserProfileResponse(response.body());
                } else {
                    displaySavedUserName();
                }
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
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getGender(),
                user.getPhoneNumber()
        );
    }

    private void displaySavedUserName() {
        String savedName = prefsManager.getUserName();
        tvUserName.setText(savedName != null ? savedName : DEFAULT_USER_NAME);
    }

    private void fetchNews() {
        RetrofitClient.getInstance()
                .getApiService()
                .getNews()
                .enqueue(createNewsCallback());
    }

    private Callback<NewsResponse> createNewsCallback() {
        return new Callback<NewsResponse>() {
            @Override
            public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    handleNewsResponse(response.body());
                } else {
                    showToast("Failed to load news");
                }
            }

            @Override
            public void onFailure(Call<NewsResponse> call, Throwable t) {
                showToast("Error: " + t.getMessage());
            }
        };
    }

    private void handleNewsResponse(NewsResponse newsResponse) {
        List<NewsResponse.NewsArticle> articles = newsResponse.getResults();

        if (articles != null && !articles.isEmpty()) {
            displayNews(articles);
        } else {
            showToast("No news available");
        }
    }

    private void displayNews(List<NewsResponse.NewsArticle> articles) {
        newsAdapter = new NewsAdapter(this, articles);
        newsRecyclerView.setAdapter(newsAdapter);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void startBackgroundAnimations() {
        animateOrb(floatingOrb1, 150f, 80f, 12000, 15000, 8000);
        animateOrb(floatingOrb2, -100f, -60f, 10000, 13000, 9000);
        animateOrb(floatingOrb3, 120f, -90f, 14000, 11000, 10000);
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

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        fetchUserProfile();
    }
}