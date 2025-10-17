package com.example.patholens;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
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

    private DrawerLayout drawerLayout;
    private ImageView menuIcon;
    private NavigationView sideNavigation;
    private TextView tvWelcome, tvUserName;
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;

    private View floatingOrb1, floatingOrb2, floatingOrb3;
    private PrefsManager prefsManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        prefsManager = new PrefsManager(this);

        initializeViews();
        setupWelcomeMessage();
        setupRecyclerView();
        fetchUserProfile();
        fetchNews();
        startBackgroundAnimations();
        setupNavigationDrawer();
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
        tvWelcome.setText(isNewUser ? "Welcome" : "Welcome Back");
    }

    private void setupRecyclerView() {
        newsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        newsRecyclerView.setHasFixedSize(true);
    }

    private void fetchUserProfile() {
        String token = prefsManager.getBearerToken();

        if (token == null) {
            tvUserName.setText("Guest");
            return;
        }

        RetrofitClient.getInstance().getApiService()
                .getAuthenticatedUser(token)
                .enqueue(new Callback<UserResponse>() {
                    @Override
                    public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            UserResponse userResponse = response.body();
                            if (userResponse.isStatus() && userResponse.getData() != null) {
                                String name = userResponse.getData().getName();
                                tvUserName.setText(name != null ? name : "User");

                                prefsManager.saveUserProfile(
                                        userResponse.getData().getName(),
                                        userResponse.getData().getEmail(),
                                        userResponse.getData().getAge(),
                                        userResponse.getData().getGender(),
                                        userResponse.getData().getPhoneNumber()
                                );
                            }
                        } else {
                            String savedName = prefsManager.getUserName();
                            tvUserName.setText(savedName != null ? savedName : "User");
                        }
                    }

                    @Override
                    public void onFailure(Call<UserResponse> call, Throwable t) {
                        String savedName = prefsManager.getUserName();
                        tvUserName.setText(savedName != null ? savedName : "User");
                    }
                });
    }

    private void fetchNews() {
        RetrofitClient.getInstance().getApiService()
                .getNews()
                .enqueue(new Callback<NewsResponse>() {
                    @Override
                    public void onResponse(Call<NewsResponse> call, Response<NewsResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<NewsResponse.NewsArticle> articles = response.body().getResults();
                            if (articles != null && !articles.isEmpty()) {
                                newsAdapter = new NewsAdapter(MainActivity.this, articles);
                                newsRecyclerView.setAdapter(newsAdapter);
                            } else {
                                Toast.makeText(MainActivity.this,
                                        "No news available", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(MainActivity.this,
                                    "Failed to load news", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<NewsResponse> call, Throwable t) {
                        Toast.makeText(MainActivity.this,
                                "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupNavigationDrawer() {
        sideNavigation.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_profile) {
                startActivity(new Intent(MainActivity.this, UserProfileActivity.class));
            } else if (id == R.id.nav_diagnosis) {
                startActivity(new Intent(MainActivity.this, PatientInformationActivity.class));
            } else if (id == R.id.nav_history) {
                startActivity(new Intent(MainActivity.this, DiagnosisHistoryActivity.class));
            } else if (id == R.id.nav_logout) {
                prefsManager.clearAll();
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
                Toast.makeText(MainActivity.this, "Logged out successfully", Toast.LENGTH_SHORT).show();
            }

            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        });
    }

    private void startBackgroundAnimations() {
        animateOrb(floatingOrb1, 150f, 80f, 12000, 15000, 8000);
        animateOrb(floatingOrb2, -100f, -60f, 10000, 13000, 9000);
        animateOrb(floatingOrb3, 120f, -90f, 14000, 11000, 10000);
    }

    private void animateOrb(View orb, float translateY, float translateX,
                            long durationY, long durationX, long scaleDuration) {
        ObjectAnimator translateYAnim = ObjectAnimator.ofFloat(orb, "translationY", 0f, translateY);
        translateYAnim.setDuration(durationY);
        translateYAnim.setRepeatCount(ValueAnimator.INFINITE);
        translateYAnim.setRepeatMode(ValueAnimator.REVERSE);
        translateYAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateYAnim.start();

        ObjectAnimator translateXAnim = ObjectAnimator.ofFloat(orb, "translationX", 0f, translateX);
        translateXAnim.setDuration(durationX);
        translateXAnim.setRepeatCount(ValueAnimator.INFINITE);
        translateXAnim.setRepeatMode(ValueAnimator.REVERSE);
        translateXAnim.setInterpolator(new AccelerateDecelerateInterpolator());
        translateXAnim.start();

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(orb, "scaleX", 1f, 1.2f);
        scaleX.setDuration(scaleDuration);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();

        ObjectAnimator scaleY = ObjectAnimator.ofFloat(orb, "scaleY", 1f, 1.2f);
        scaleY.setDuration(scaleDuration);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleY.start();
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