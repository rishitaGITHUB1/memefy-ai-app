package com.example.memefy;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.navigation.NavigationView;

public class DashboardActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu;
    private CardView cardCreateMeme, cardBrowseGallery, cardViewProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        initViews();
        setupDrawer();
        setupListeners();
    }

    private void initViews() {
        drawerLayout      = findViewById(R.id.drawer_layout);
        navigationView    = findViewById(R.id.nav_view);
        btnMenu           = findViewById(R.id.btn_menu);
        cardCreateMeme    = findViewById(R.id.card_create_meme);
        cardBrowseGallery = findViewById(R.id.card_browse_gallery);
        cardViewProfile   = findViewById(R.id.card_view_profile);
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);

        // Null check prevents crash if btn_close_drawer is missing in nav_header
        View headerView = navigationView.getHeaderView(0);
        if (headerView != null) {
            ImageView btnCloseDrawer = headerView.findViewById(R.id.btn_close_drawer);
            if (btnCloseDrawer != null) {
                btnCloseDrawer.setOnClickListener(v ->
                        drawerLayout.closeDrawer(GravityCompat.END));
            }
        }
    }

    private void setupListeners() {
        if (btnMenu != null) {
            btnMenu.setOnClickListener(v ->
                    drawerLayout.openDrawer(GravityCompat.END));
        }

        if (cardCreateMeme != null) {
            cardCreateMeme.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, GeneratorActivity.class)));
        }

        if (cardBrowseGallery != null) {
            cardBrowseGallery.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, GalleryActivity.class)));
        }

        if (cardViewProfile != null) {
            cardViewProfile.setOnClickListener(v ->
                    startActivity(new Intent(DashboardActivity.this, ProfileActivity.class)));
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            startActivity(new Intent(this, MainActivity.class));
        } else if (id == R.id.nav_dashboard) {
            Toast.makeText(this, "Already on Dashboard", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_generator) {
            startActivity(new Intent(this, GeneratorActivity.class));
        } else if (id == R.id.nav_gallery) {
            startActivity(new Intent(this, GalleryActivity.class));
        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_dark_mode) {
            Toast.makeText(this, "Dark mode is already enabled!", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_demo) {
            startActivity(new Intent(this, DemoActivity.class));
        } else if (id == R.id.nav_logout) {
            Toast.makeText(this, "Logout is disabled", Toast.LENGTH_SHORT).show();
        }

        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}