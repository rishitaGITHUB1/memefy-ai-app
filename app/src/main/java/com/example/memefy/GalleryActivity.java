package com.example.memefy;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class GalleryActivity extends AppCompatActivity {

    // ── Meme data model ────────────────────────────────────────────────────────
    static class GalleryMeme {
        int imageRes;
        String title, author, date, fire, views;
        String[] tags;
        boolean liked;

        GalleryMeme(int imageRes, String title, String date, String fire, String views, String... tags) {
            this.imageRes = imageRes;
            this.title    = title;
            this.author   = "@Anonymous";
            this.date     = date;
            this.fire     = fire;
            this.views    = views;
            this.tags     = tags;
            this.liked    = false;
        }
    }

    // ── Data ───────────────────────────────────────────────────────────────────
    private final List<GalleryMeme> allMemes = new ArrayList<>();
    private List<GalleryMeme> filteredMemes  = new ArrayList<>();
    private String activeFilter = "all";
    private String searchQuery  = "";

    private LinearLayout memeFeed;
    private EditText etSearch;
    private float density;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        density  = getResources().getDisplayMetrics().density;
        memeFeed = findViewById(R.id.meme_feed);
        etSearch = findViewById(R.id.et_search);

        populateMemes();
        setupSearch();
        setupFilterChips();
        setupBottomNav();
        renderFeed(allMemes);
    }

    // ── Meme data ──────────────────────────────────────────────────────────────
    private void populateMemes() {
        allMemes.add(new GalleryMeme(R.drawable.meme1,
                "When the deadline is tomorrow",
                "2024-01-15", "2.3k", "15.6k",
                "#relatable", "#work", "#procrastination"));

        allMemes.add(new GalleryMeme(R.drawable.meme2,
                "POV: You're the main character",
                "2024-01-07", "3.0k", "20.8k",
                "#confidence", "#main character", "#energy"));

        allMemes.add(new GalleryMeme(R.drawable.meme3,
                "When your code works on the first try",
                "2024-01-08", "3.9k", "25.4k",
                "#coding", "#success", "#developer"));

        allMemes.add(new GalleryMeme(R.drawable.meme4,
                "My bank account after one grocery trip",
                "2024-01-11", "2.8k", "19.2k",
                "#money", "#adulting", "#reality"));

        allMemes.add(new GalleryMeme(R.drawable.meme2,
                "POV: You forgot you had homework",
                "2024-01-14", "1.9k", "12.4k",
                "#school", "#panic", "#student"));

        allMemes.add(new GalleryMeme(R.drawable.meme5,
                "When you finally understand the assignment",
                "2024-01-12", "4.2k", "28.7k",
                "#school", "#success", "#enlightenment"));

        allMemes.add(new GalleryMeme(R.drawable.meme4,
                "Me explaining why I need another streaming service",
                "2024-01-13", "3.1k", "21.0k",
                "#money", "#streaming", "#priorities"));

        allMemes.add(new GalleryMeme(R.drawable.meme3,
                "POV: You're the main character",
                "2024-01-07", "3.0k", "20.8k",
                "#confidence", "#energy", "#relatable"));

        allMemes.add(new GalleryMeme(R.drawable.meme1,
                "Me at 3am instead of sleeping",
                "2024-01-10", "5.1k", "32.1k",
                "#relatable", "#adulting", "#school"));
    }

    // ── Search ─────────────────────────────────────────────────────────────────
    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            public void onTextChanged(CharSequence s, int i, int b, int c) {
                searchQuery = s.toString().trim().toLowerCase();
                applyFilters();
            }
            public void afterTextChanged(Editable s) {}
        });

        findViewById(R.id.btn_search).setOnClickListener(v -> applyFilters());
    }

    // ── Filter chips ───────────────────────────────────────────────────────────
    private void setupFilterChips() {
        int[] chipIds = {R.id.chip_all, R.id.chip_relatable, R.id.chip_work,
                R.id.chip_school, R.id.chip_money, R.id.chip_coding, R.id.chip_adulting};
        String[] filters = {"all", "relatable", "work", "school", "money", "coding", "adulting"};

        for (int i = 0; i < chipIds.length; i++) {
            final String filter = filters[i];
            final TextView chip = findViewById(chipIds[i]);
            chip.setOnClickListener(v -> {
                activeFilter = filter;
                // Reset all chips
                for (int id : chipIds) {
                    findViewById(id).setBackgroundResource(R.drawable.card_normal_border);
                    ((TextView) findViewById(id)).setTextColor(0xFFCCCCCC);
                }
                chip.setBackgroundResource(R.drawable.gradient_button);
                chip.setTextColor(0xFFFFFFFF);
                applyFilters();
            });
        }
    }

    private void applyFilters() {
        filteredMemes = new ArrayList<>();
        for (GalleryMeme meme : allMemes) {
            boolean matchesFilter = activeFilter.equals("all");
            if (!matchesFilter) {
                for (String tag : meme.tags) {
                    if (tag.toLowerCase().contains(activeFilter)) { matchesFilter = true; break; }
                }
            }
            boolean matchesSearch = searchQuery.isEmpty() || meme.title.toLowerCase().contains(searchQuery);
            if (matchesFilter && matchesSearch) filteredMemes.add(meme);
        }
        renderFeed(filteredMemes);
    }

    // ── Render meme feed ───────────────────────────────────────────────────────
    private void renderFeed(List<GalleryMeme> memes) {
        memeFeed.removeAllViews();
        for (GalleryMeme meme : memes) {
            memeFeed.addView(buildMemeCard(meme));
        }
        if (memes.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No memes found 😢");
            empty.setTextColor(0xFF8A8FA8);
            empty.setTextSize(16f);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, (int)(48 * density), 0, (int)(48 * density));
            memeFeed.addView(empty);
        }
    }

    // ── Build single meme card ─────────────────────────────────────────────────
    private View buildMemeCard(GalleryMeme meme) {
        // Outer card
        CardView card = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, (int)(20 * density));
        card.setLayoutParams(cardParams);
        card.setRadius(16 * density);
        card.setCardElevation(4 * density);
        card.setCardBackgroundColor(0xFF1e2235);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Image with overlay
        FrameLayout imageFrame = new FrameLayout(this);
        imageFrame.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, (int)(280 * density)));

        ImageView imageView = new ImageView(this);
        imageView.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageResource(meme.imageRes);

        // "View Meme" overlay (hidden by default)
        LinearLayout overlay = new LinearLayout(this);
        overlay.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        overlay.setOrientation(LinearLayout.HORIZONTAL);
        overlay.setGravity(Gravity.CENTER);
        overlay.setBackgroundColor(0x66000000);
        overlay.setVisibility(View.GONE);

        TextView viewMemeBtn = new TextView(this);
        viewMemeBtn.setText("👁  View Meme");
        viewMemeBtn.setTextColor(0xFFFFFFFF);
        viewMemeBtn.setTextSize(16f);
        viewMemeBtn.setTypeface(null, android.graphics.Typeface.BOLD);
        viewMemeBtn.setBackground(getResources().getDrawable(R.drawable.card_normal_border));
        int vp = (int)(10 * density); int hp = (int)(20 * density);
        viewMemeBtn.setPadding(hp, vp, hp, vp);
        overlay.addView(viewMemeBtn);

        imageFrame.addView(imageView);
        imageFrame.addView(overlay);

        // Touch → pink glow + show overlay
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    card.setCardBackgroundColor(0xFF2a1535);

                    overlay.setVisibility(View.VISIBLE);
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    overlay.setVisibility(View.GONE);
                    card.setCardBackgroundColor(0xFF1e2235);
                    break;
            }
            return false;
        });

        // Tap → fullscreen dialog
        card.setOnClickListener(v -> showMemeDialog(meme));
        viewMemeBtn.setOnClickListener(v -> showMemeDialog(meme));

        // Info section
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        int pad = (int)(14 * density);
        info.setPadding(pad, pad, pad, pad);

        // Title
        TextView title = new TextView(this);
        title.setText(meme.title);
        title.setTextColor(0xFFFFFFFF);
        title.setTextSize(16f);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = (int)(6 * density);
        title.setLayoutParams(titleParams);

        // Author + date row
        LinearLayout metaRow = new LinearLayout(this);
        metaRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams metaParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        metaParams.bottomMargin = (int)(10 * density);
        metaRow.setLayoutParams(metaParams);

        TextView author = new TextView(this);
        author.setText(meme.author);
        author.setTextColor(0xFF4ECDC4);
        author.setTextSize(13f);
        author.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView date = new TextView(this);
        date.setText(meme.date);
        date.setTextColor(0xFF8A8FA8);
        date.setTextSize(13f);

        metaRow.addView(author);
        metaRow.addView(date);

        // Tags row
        LinearLayout tagsRow = new LinearLayout(this);
        tagsRow.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams tagsParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tagsParams.bottomMargin = (int)(12 * density);
        tagsRow.setLayoutParams(tagsParams);

        for (String tag : meme.tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextColor(0xFFCCCCCC);
            tagView.setTextSize(12f);
            tagView.setBackground(getResources().getDrawable(R.drawable.card_normal_border));
            tagView.setPadding((int)(10 * density), (int)(4 * density),
                    (int)(10 * density), (int)(4 * density));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.rightMargin = (int)(6 * density);
            tagView.setLayoutParams(tp);
            tagsRow.addView(tagView);
        }

        // Stats + like/share row
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setGravity(Gravity.CENTER_VERTICAL);

        TextView fireCount = new TextView(this);
        fireCount.setText("🔥 " + meme.fire);
        fireCount.setTextColor(0xFFCCCCCC);
        fireCount.setTextSize(14f);
        fireCount.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams) fireCount.getLayoutParams()).rightMargin = (int)(14 * density);

        TextView viewCount = new TextView(this);
        viewCount.setText("👁 " + meme.views);
        viewCount.setTextColor(0xFFCCCCCC);
        viewCount.setTextSize(14f);
        viewCount.setLayoutParams(new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        // Like button
        TextView likeBtn = new TextView(this);
        likeBtn.setText(meme.liked ? "❤" : "🤍");
        likeBtn.setTextSize(22f);
        likeBtn.setGravity(Gravity.CENTER);
        likeBtn.setLayoutParams(new LinearLayout.LayoutParams(
                (int)(44 * density), (int)(44 * density)));
        likeBtn.setBackground(meme.liked ?
                getResources().getDrawable(R.drawable.gradient_button) :
                getResources().getDrawable(R.drawable.card_normal_border));
        ((LinearLayout.LayoutParams) likeBtn.getLayoutParams()).rightMargin = (int)(8 * density);
        likeBtn.setOnClickListener(v -> {
            meme.liked = !meme.liked;
            likeBtn.setText(meme.liked ? "❤" : "🤍");
            likeBtn.setBackground(meme.liked ?
                    getResources().getDrawable(R.drawable.gradient_button) :
                    getResources().getDrawable(R.drawable.card_normal_border));
            Toast.makeText(this, meme.liked ? "Liked! ❤" : "Unliked", Toast.LENGTH_SHORT).show();
        });

        // Share button
        TextView shareBtn = new TextView(this);
        shareBtn.setText("📤");
        shareBtn.setTextSize(18f);
        shareBtn.setGravity(Gravity.CENTER);
        shareBtn.setBackground(getResources().getDrawable(R.drawable.card_normal_border));
        shareBtn.setLayoutParams(new LinearLayout.LayoutParams(
                (int)(44 * density), (int)(44 * density)));
        shareBtn.setOnClickListener(v -> shareMeme(meme));

        statsRow.addView(fireCount);
        statsRow.addView(viewCount);
        statsRow.addView(likeBtn);
        statsRow.addView(shareBtn);

        info.addView(title);
        info.addView(metaRow);
        info.addView(tagsRow);
        info.addView(statsRow);

        inner.addView(imageFrame);
        inner.addView(info);
        card.addView(inner);
        return card;
    }

    // ── Fullscreen meme dialog ─────────────────────────────────────────────────
    private void showMemeDialog(GalleryMeme meme) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_meme_view);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int)(getResources().getDisplayMetrics().widthPixels * 0.95f),
                ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView ivMeme       = dialog.findViewById(R.id.iv_fullscreen_meme);
        TextView  tvTitle      = dialog.findViewById(R.id.tv_view_title);
        TextView  tvAuthor     = dialog.findViewById(R.id.tv_view_author);
        TextView  tvDate       = dialog.findViewById(R.id.tv_view_date);
        LinearLayout tagsContainer = dialog.findViewById(R.id.view_tags_container);
        TextView  tvFire       = dialog.findViewById(R.id.tv_view_fire);
        TextView  tvViews      = dialog.findViewById(R.id.tv_view_views);
        TextView  btnLike      = dialog.findViewById(R.id.btn_view_like);
        TextView  btnShare     = dialog.findViewById(R.id.btn_view_share);
        TextView  btnDownload  = dialog.findViewById(R.id.btn_view_download);
        TextView  btnClose     = dialog.findViewById(R.id.btn_close_view);

        ivMeme.setImageResource(meme.imageRes);
        tvTitle.setText(meme.title);
        tvAuthor.setText(meme.author);
        tvDate.setText(meme.date);
        tvFire.setText("🔥 " + meme.fire);
        tvViews.setText("👁 " + meme.views);

        // Tags
        for (String tag : meme.tags) {
            TextView tagView = new TextView(this);
            tagView.setText(tag);
            tagView.setTextColor(0xFFCCCCCC);
            tagView.setTextSize(12f);
            tagView.setBackground(getResources().getDrawable(R.drawable.card_normal_border));
            tagView.setPadding((int)(10*density),(int)(4*density),(int)(10*density),(int)(4*density));
            LinearLayout.LayoutParams tp = new LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            tp.rightMargin = (int)(6 * density);
            tagView.setLayoutParams(tp);
            tagsContainer.addView(tagView);
        }

        btnLike.setText(meme.liked ? "❤" : "🤍");
        btnLike.setOnClickListener(v -> {
            meme.liked = !meme.liked;
            btnLike.setText(meme.liked ? "❤" : "🤍");
            Toast.makeText(this, meme.liked ? "Liked! ❤" : "Unliked", Toast.LENGTH_SHORT).show();
            renderFeed(filteredMemes.isEmpty() ? allMemes : filteredMemes);
        });

        btnShare.setOnClickListener(v -> { shareMeme(meme); });

        btnDownload.setOnClickListener(v -> {
            ivMeme.setDrawingCacheEnabled(true);
            Bitmap bmp = ivMeme.getDrawingCache();
            if (bmp != null) downloadBitmap(bmp, meme.title);
            else Toast.makeText(this, "Download failed", Toast.LENGTH_SHORT).show();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // ── Share ──────────────────────────────────────────────────────────────────
    private void shareMeme(GalleryMeme meme) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, meme.title + " 🔥\n\nShared from MEMEFY-AI");
        startActivity(Intent.createChooser(shareIntent, "Share meme via"));
    }

    // ── Download ───────────────────────────────────────────────────────────────
    private void downloadBitmap(Bitmap bitmap, String name) {
        try {
            String filename = "memefy_" + System.currentTimeMillis() + ".png";
            OutputStream fos;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH,
                        Environment.DIRECTORY_PICTURES + "/MemefyAI");
                Uri uri = getContentResolver().insert(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = getContentResolver().openOutputStream(uri);
            } else {
                java.io.File dir = new java.io.File(
                        Environment.getExternalStoragePublicDirectory(
                                Environment.DIRECTORY_PICTURES), "MemefyAI");
                dir.mkdirs();
                fos = new java.io.FileOutputStream(new java.io.File(dir, filename));
            }
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(this, "Saved to gallery! 📸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    // ── Bottom nav ─────────────────────────────────────────────────────────────
    private void setupBottomNav() {
        boolean loggedIn = MemeSessionManager.isLoggedIn();

        findViewById(R.id.nav_home_btn).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_create_btn).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already in Gallery!", Toast.LENGTH_SHORT).show());

        if (loggedIn) {
            findViewById(R.id.nav_stats_btn).setOnClickListener(v ->
                    startActivity(new Intent(this, DashboardActivity.class)));
            findViewById(R.id.nav_profile_btn).setOnClickListener(v ->
                    startActivity(new Intent(this, ProfileActivity.class)));
        } else {
            // Show login icon instead of profile when not logged in
            ImageView profileIcon = (ImageView) ((LinearLayout)findViewById(R.id.nav_profile_btn))
                    .getChildAt(0);
            profileIcon.setImageResource(R.drawable.ic_logout);
            findViewById(R.id.nav_profile_btn).setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
            findViewById(R.id.nav_stats_btn).setOnClickListener(v ->
                    startActivity(new Intent(this, LoginActivity.class)));
        }
    }
}