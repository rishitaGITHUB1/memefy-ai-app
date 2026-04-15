package com.example.memefy;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.gridlayout.widget.GridLayout;

public class MainActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;

    // Meme data
    private final int[] memeImages = {
            R.drawable.meme1, R.drawable.meme2, R.drawable.meme3,
            R.drawable.meme4, R.drawable.meme5, R.drawable.meme6
    };
    private final String[] memeCaptions = {
            "This is fire 🔥", "So relatable 💯", "Big mood",
            "Facts no printer", "ayega na bhidu", "Send this to everyone"
    };
    // Which memes get the 🔥 fire badge (index)
    private final boolean[] isHot = { false, true, false, false, false, false };

    // Track selected meme card for pink highlight
    private View selectedCard = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        drawerLayout = findViewById(R.id.drawer_layout);

        setupTopNav();
        setupDrawer();
        setupFeatureCards();
        setupMemesGrid();
        setupBottomNav();
        setupButtons();
    }

    // ── Top nav menu button ────────────────────────────────────────────────────
    private void setupTopNav() {
        findViewById(R.id.btn_menu).setOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.END));
    }

    // ── Custom drawer ──────────────────────────────────────────────────────────
    private void setupDrawer() {
        findViewById(R.id.btn_close_drawer).setOnClickListener(v ->
                drawerLayout.closeDrawer(GravityCompat.END));

        findViewById(R.id.drawer_home).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show();
        });
        findViewById(R.id.drawer_generator).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, GeneratorActivity.class));
        });
        findViewById(R.id.drawer_gallery).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, GalleryActivity.class));
        });
        findViewById(R.id.drawer_light_mode).setOnClickListener(v ->
                Toast.makeText(this, "Light mode coming soon! ☀️", Toast.LENGTH_SHORT).show());
        findViewById(R.id.drawer_login_btn).setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.END);
            startActivity(new Intent(this, LoginActivity.class));
        });
    }

    // ── Feature cards: pink glow on touch ─────────────────────────────────────
    private void setupFeatureCards() {
        int[] cardIds = {
                R.id.card_ai_master, R.id.card_viral,
                R.id.card_instant,   R.id.card_trend
        };
        for (int id : cardIds) {
            View card = findViewById(id);
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.setBackgroundResource(R.drawable.pink_glow_border);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        v.setBackgroundResource(R.drawable.card_normal_border);
                        break;
                }
                return false; // let click pass through
            });
            card.setOnClickListener(v ->
                    Toast.makeText(this, "Feature coming soon! 🚀", Toast.LENGTH_SHORT).show());
        }
    }

    // ── Fresh Memes grid with pink highlight + fire badge ─────────────────────
    private void setupMemesGrid() {
        GridLayout grid = findViewById(R.id.memes_grid_main);
        grid.removeAllViews();

        int cols = 2;
        float density = getResources().getDisplayMetrics().density;
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int margin = (int)(8 * density);
        int cellSize = (screenWidth - (int)(40 * density) - margin * 4) / cols;

        for (int i = 0; i < memeImages.length; i++) {
            final int index = i;

            // Outer FrameLayout (holds card + fire badge)
            FrameLayout frame = new FrameLayout(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width  = cellSize;
            params.height = cellSize + (int)(36 * density);
            params.setMargins(margin, margin, margin, margin);
            params.columnSpec = GridLayout.spec(i % cols, 1f);
            frame.setLayoutParams(params);

            // Card (meme image + caption)
            LinearLayout card = new LinearLayout(this);
            card.setOrientation(LinearLayout.VERTICAL);
            card.setBackgroundResource(R.drawable.meme_card_normal);
            card.setClipToOutline(true);
            FrameLayout.LayoutParams cardParams = new FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            card.setLayoutParams(cardParams);

            ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, cellSize - (int)(36 * density)));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(memeImages[i]);

            TextView caption = new TextView(this);
            caption.setText(memeCaptions[i]);
            caption.setTextColor(0xFFCCCCCC);
            caption.setTextSize(13f);
            caption.setPadding((int)(8 * density), (int)(6 * density), (int)(8 * density), (int)(6 * density));
            caption.setGravity(Gravity.CENTER);
            caption.setMaxLines(1);
            caption.setEllipsize(android.text.TextUtils.TruncateAt.END);

            card.addView(imageView);
            card.addView(caption);

            // Touch → pink highlight
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case android.view.MotionEvent.ACTION_DOWN:
                        v.setBackgroundResource(R.drawable.meme_card_selected);
                        break;
                    case android.view.MotionEvent.ACTION_UP:
                    case android.view.MotionEvent.ACTION_CANCEL:
                        if (selectedCard != v) {
                            v.setBackgroundResource(R.drawable.meme_card_normal);
                        }
                        break;
                }
                return false;
            });

            card.setOnClickListener(v -> {
                // Reset previous selection
                if (selectedCard != null && selectedCard != v) {
                    selectedCard.setBackgroundResource(R.drawable.meme_card_normal);
                }
                selectedCard = v;
                v.setBackgroundResource(R.drawable.meme_card_selected);
                Toast.makeText(this, memeCaptions[index], Toast.LENGTH_SHORT).show();
            });

            frame.addView(card);

            // 🔥 Fire badge for hot memes
            if (isHot[i]) {
                TextView badge = new TextView(this);
                badge.setText("🔥 10k");
                badge.setTextSize(12f);
                badge.setTextColor(0xFFFFFFFF);
                badge.setBackgroundResource(R.drawable.demo_badge_bg);
                badge.setPadding((int)(8 * density), (int)(4 * density),
                        (int)(8 * density), (int)(4 * density));

                FrameLayout.LayoutParams badgeParams = new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.WRAP_CONTENT,
                        FrameLayout.LayoutParams.WRAP_CONTENT);
                badgeParams.gravity = Gravity.BOTTOM | Gravity.START;
                badgeParams.leftMargin  = (int)(8 * density);
                badgeParams.bottomMargin = (int)(36 * density) + (int)(6 * density);
                badge.setLayoutParams(badgeParams);
                frame.addView(badge);
            }

            grid.addView(frame);
        }
    }

    // ── Bottom navigation bar ──────────────────────────────────────────────────
    private void setupBottomNav() {
        findViewById(R.id.nav_home_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already on Home", Toast.LENGTH_SHORT).show());

        findViewById(R.id.nav_generator_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GeneratorActivity.class)));

        findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));

        findViewById(R.id.nav_login_btn).setOnClickListener(v ->
                startActivity(new Intent(this, LoginActivity.class)));
    }

    // ── Other buttons ──────────────────────────────────────────────────────────
    private void setupButtons() {
        findViewById(R.id.btn_start_creating).setOnClickListener(v ->
                startActivity(new Intent(this, GeneratorActivity.class)));

        findViewById(R.id.btn_check_viral).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));

        findViewById(R.id.btn_see_more_memes).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
    }

    // ── Back press ────────────────────────────────────────────────────────────
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END);
        } else {
            super.onBackPressed();
        }
    }
}