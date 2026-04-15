package com.example.memefy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProfileActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextView tvName, tvHandle, tvMemesCount, tvViewsCount, tvSharesCount;
    private TextView tvMemesTabCount, tvFavTabCount, tvSectionTitle;
    private LinearLayout emptyState, memesListContainer;
    private LinearLayout btnTabMemes, btnTabFavorites;

    private boolean showingMemes = true;
    private List<Map<String, Object>> allMemes     = new ArrayList<>();
    private List<Map<String, Object>> allFavorites = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        initViews();
        loadUserProfile();
        loadMemes();
        setupListeners();
    }

    private void initViews() {
        tvName            = findViewById(R.id.tv_profile_name);
        tvHandle          = findViewById(R.id.tv_profile_handle);
        tvMemesCount      = findViewById(R.id.tv_memes_count);
        tvViewsCount      = findViewById(R.id.tv_views_count);
        tvSharesCount     = findViewById(R.id.tv_shares_count);
        tvMemesTabCount   = findViewById(R.id.tv_memes_tab_count);
        tvFavTabCount     = findViewById(R.id.tv_favorites_tab_count);
        tvSectionTitle    = findViewById(R.id.tv_section_title);
        emptyState        = findViewById(R.id.empty_state);
        memesListContainer = findViewById(R.id.memes_list_container);
        btnTabMemes       = findViewById(R.id.btn_tab_memes);
        btnTabFavorites   = findViewById(R.id.btn_tab_favorites);
    }

    private void loadUserProfile() {
        // Set display name
        String displayName = currentUser.getDisplayName();
        String email       = currentUser.getEmail();

        if (displayName != null && !displayName.isEmpty()) {
            tvName.setText(displayName);
            tvHandle.setText("@" + displayName.toLowerCase().replace(" ", "_"));
        } else if (email != null) {
            String username = email.split("@")[0];
            tvName.setText(username);
            tvHandle.setText("@" + username);
        }
    }

    private void loadMemes() {
        String uid = currentUser.getUid();
        db.collection("users").document(uid).collection("memes")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    allMemes.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        allMemes.add(doc.getData());
                    }
                    tvMemesCount.setText(String.valueOf(allMemes.size()));
                    tvMemesTabCount.setText(String.valueOf(allMemes.size()));
                    if (showingMemes) renderMemeCards(allMemes);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load memes", Toast.LENGTH_SHORT).show());
    }

    private void renderMemeCards(List<Map<String, Object>> memes) {
        memesListContainer.removeAllViews();

        if (memes.isEmpty()) {
            emptyState.setVisibility(View.VISIBLE);
            memesListContainer.setVisibility(View.GONE);
            return;
        }

        emptyState.setVisibility(View.GONE);
        memesListContainer.setVisibility(View.VISIBLE);

        float density = getResources().getDisplayMetrics().density;

        for (Map<String, Object> meme : memes) {
            String title      = (String) meme.getOrDefault("title", "My Meme");
            String imageData  = (String) meme.get("imageData"); // base64
            long   views      = meme.containsKey("views")  ? (long) meme.get("views")  : 0;
            long   shares     = meme.containsKey("shares") ? (long) meme.get("shares") : 0;

            // Card
            CardView card = new CardView(this);
            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            cardParams.setMargins(0, 0, 0, (int)(16 * density));
            card.setLayoutParams(cardParams);
            card.setRadius(16 * density);
            card.setCardElevation(4 * density);
            card.setCardBackgroundColor(0xFF1e2235);

            LinearLayout inner = new LinearLayout(this);
            inner.setOrientation(LinearLayout.VERTICAL);
            inner.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));

            // Meme image
            ImageView imageView = new ImageView(this);
            int imgHeight = (int)(220 * density);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, imgHeight));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

            if (imageData != null) {
                try {
                    byte[] decoded = Base64.decode(imageData, Base64.DEFAULT);
                    Bitmap bmp = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);
                    imageView.setImageBitmap(bmp);
                } catch (Exception e) {
                    imageView.setImageResource(R.drawable.meme1);
                }
            } else {
                imageView.setImageResource(R.drawable.meme1);
            }

            // Title + stats row
            LinearLayout infoRow = new LinearLayout(this);
            infoRow.setOrientation(LinearLayout.VERTICAL);
            int pad = (int)(12 * density);
            infoRow.setPadding(pad, pad, pad, pad);

            TextView titleView = new TextView(this);
            titleView.setText(title);
            titleView.setTextColor(0xFFFFFFFF);
            titleView.setTextSize(16f);
            titleView.setTypeface(null, android.graphics.Typeface.BOLD);

            // Stats row
            LinearLayout statsRow = new LinearLayout(this);
            statsRow.setOrientation(LinearLayout.HORIZONTAL);
            LinearLayout.LayoutParams statsParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            statsParams.topMargin = (int)(8 * density);
            statsRow.setLayoutParams(statsParams);

            TextView viewsView = new TextView(this);
            viewsView.setText("👁 " + views);
            viewsView.setTextColor(0xFF8A8FA8);
            viewsView.setTextSize(13f);
            viewsView.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1f));

            TextView sharesView = new TextView(this);
            sharesView.setText("📤 " + shares);
            sharesView.setTextColor(0xFF8A8FA8);
            sharesView.setTextSize(13f);
            sharesView.setGravity(Gravity.END);

            statsRow.addView(viewsView);
            statsRow.addView(sharesView);

            infoRow.addView(titleView);
            infoRow.addView(statsRow);

            inner.addView(imageView);
            inner.addView(infoRow);
            card.addView(inner);
            memesListContainer.addView(card);
        }
    }

    private void setupListeners() {
        // Tabs
        btnTabMemes.setOnClickListener(v -> {
            showingMemes = true;
            btnTabMemes.setBackgroundResource(R.drawable.gradient_button);
            btnTabFavorites.setBackgroundResource(R.drawable.card_normal_border);
            tvSectionTitle.setText("MY MASTERPIECES");
            renderMemeCards(allMemes);
        });

        btnTabFavorites.setOnClickListener(v -> {
            showingMemes = false;
            btnTabFavorites.setBackgroundResource(R.drawable.gradient_button);
            btnTabMemes.setBackgroundResource(R.drawable.card_normal_border);
            tvSectionTitle.setText("MY FAVORITES");
            renderMemeCards(allFavorites);
        });

        findViewById(R.id.btn_refresh).setOnClickListener(v -> loadMemes());
        findViewById(R.id.btn_create_first_meme).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.btn_edit_profile).setOnClickListener(v ->
                Toast.makeText(this, "Edit profile coming soon!", Toast.LENGTH_SHORT).show());

        // Bottom nav
        findViewById(R.id.nav_home_btn).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_create_btn).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
        findViewById(R.id.nav_stats_btn).setOnClickListener(v ->
                startActivity(new Intent(this, DashboardActivity.class)));
        findViewById(R.id.nav_profile_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already on Profile!", Toast.LENGTH_SHORT).show());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMemes(); // refresh when returning from meme creation
    }
}