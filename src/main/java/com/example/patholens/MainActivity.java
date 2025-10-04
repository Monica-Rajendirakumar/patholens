package com.example.patholens;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);
        menuIcon = findViewById(R.id.menu_icon);
        sideNavigation = findViewById(R.id.side_navigation);

        // Menu icon click to open drawer
        menuIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });

        // Handle navigation menu item clicks
        sideNavigation.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.nav_profile) {
                    Intent intent = new Intent(MainActivity.this, UserProfileActivity.class);
                    startActivity(intent);

                } else if (id == R.id.nav_diagnosis) {
                    Toast.makeText(MainActivity.this, "Diagnosis", Toast.LENGTH_SHORT).show();
                    // Navigate to Diagnosis Activity
                } else if (id == R.id.nav_history) {
                    Intent intent = new Intent(MainActivity.this, DiagnosisHistoryActivity.class);
                       startActivity(intent);
                }

                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });
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