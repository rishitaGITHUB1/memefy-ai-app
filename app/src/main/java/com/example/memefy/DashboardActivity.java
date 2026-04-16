package com.example.memefy;

import android.content.Intent;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
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

import java.util.Map;

public class DashboardActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseUser currentUser;

    private TextView tvWelcome, tvUsername;
    private TextView tvStatMemes, tvStatViews, tvStatShares;
    private TextView tvStatTrending, tvStatToxicity, tvStatSentiment;
    private LinearLayout recentActivityContainer;

    private final String[] tips = {
            "The best memes capture universal experiences in unexpected ways. Think about what made you laugh today!",
            "Relatable content always wins. Focus on everyday struggles everyone faces!",
            "Timing is everything — jump on trends within the first 24 hours!",
            "Keep it simple. The best memes are understood in under 3 seconds.",
            "Use Impact font for classic memes — it's iconic for a reason!"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        mAuth = FirebaseAuth.getInstance();
        db    = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();

        initViews();
        setupWelcome();
        setupQuickActions();
        setupStatCardTouchEffects();
        setupBottomNav();
        loadStatsFromFirebase();
        loadRecentActivity();
        setRandomTip();
    }

    private void initViews() {
        tvWelcome              = findViewById(R.id.tv_welcome);
        tvUsername             = findViewById(R.id.tv_username);
        tvStatMemes            = findViewById(R.id.tv_stat_memes);
        tvStatViews            = findViewById(R.id.tv_stat_views);
        tvStatShares           = findViewById(R.id.tv_stat_shares);
        tvStatTrending         = findViewById(R.id.tv_stat_trending);
        tvStatToxicity         = findViewById(R.id.tv_stat_toxicity);
        tvStatSentiment        = findViewById(R.id.tv_stat_sentiment);
        recentActivityContainer = findViewById(R.id.recent_activity_container);
    }

    // ── Welcome message ────────────────────────────────────────────────────────
    private void setupWelcome() {
        if (currentUser != null) {
            String name = currentUser.getDisplayName();
            if (name == null || name.isEmpty()) {
                String email = currentUser.getEmail();
                name = email != null ? email.split("@")[0] : "Meme Master";
            }
            // Capitalize first letter
            name = name.substring(0, 1).toUpperCase() + name.substring(1);
            tvUsername.setText(name + "!");
        }
    }

    // ── Pink border touch effect on all stat cards ─────────────────────────────
    private void setupStatCardTouchEffects() {
        int[] cardIds = {
                R.id.card_stat_memes, R.id.card_stat_views, R.id.card_stat_shares,
                R.id.card_stat_trending, R.id.card_stat_toxicity, R.id.card_stat_sentiment,
                R.id.card_create_meme, R.id.card_browse_gallery, R.id.card_view_profile
        };

        for (int id : cardIds) {
            CardView card = findViewById(id);
            card.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        card.setCardBackgroundColor(0xFF1a0a2a);

                        // Draw pink outline via foreground
                        v.setForeground(getResources().getDrawable(R.drawable.pink_glow_border));
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        card.setCardBackgroundColor(0xFF1e2235);
                        v.setForeground(null);
                        break;
                }
                return false;
            });
        }
    }

    // ── Quick actions ──────────────────────────────────────────────────────────
    private void setupQuickActions() {
        findViewById(R.id.card_create_meme).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));

        findViewById(R.id.card_browse_gallery).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));

        findViewById(R.id.card_view_profile).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    // ── Load stats from Firebase ───────────────────────────────────────────────
    private void loadStatsFromFirebase() {
        if (currentUser == null) return;

        db.collection("users").document(currentUser.getUid()).collection("memes")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int memeCount  = querySnapshot.size();
                    long totalViews = 0, totalShares = 0;
                    int safe = 0, neutral = 0, risky = 0;
                    double totalToxicity = 0;

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Map<String, Object> data = doc.getData();
                        if (data.containsKey("views"))
                            totalViews += (long) data.get("views");
                        if (data.containsKey("shares"))
                            totalShares += (long) data.get("shares");
                        if (data.containsKey("toxicity"))
                            totalToxicity += (double) data.get("toxicity");
                        // sentiment
                        String sentiment = (String) data.getOrDefault("sentiment", "neutral");
                        if ("safe".equals(sentiment)) safe++;
                        else if ("risky".equals(sentiment)) risky++;
                        else neutral++;
                    }

                    tvStatMemes.setText(String.valueOf(memeCount));
                    tvStatViews.setText(formatCount(totalViews));
                    tvStatShares.setText(String.valueOf(totalShares));
                    tvStatTrending.setText(String.valueOf(memeCount * 10)); // simple score
                    tvStatSentiment.setText(safe + "/" + neutral + "/" + risky);

                    if (memeCount > 0) {
                        double avgTox = totalToxicity / memeCount;
                        tvStatToxicity.setText(String.format("%.1f%%", avgTox));
                    } else {
                        tvStatToxicity.setText("0%");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load stats", Toast.LENGTH_SHORT).show());
    }

    // ── Load recent activity ───────────────────────────────────────────────────
    private void loadRecentActivity() {
        if (currentUser == null) {
            addDefaultActivity();
            return;
        }

        db.collection("users").document(currentUser.getUid()).collection("memes")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .limit(5)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    recentActivityContainer.removeAllViews();
                    if (querySnapshot.isEmpty()) {
                        addDefaultActivity();
                        return;
                    }
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String title = doc.contains("title") ? (String) doc.get("title") : "My Meme";
                        String sentiment = doc.contains("sentiment") ? (String) doc.get("sentiment") : "Neutral";
                        double toxicity = doc.contains("toxicity") ?
                                (double) doc.get("toxicity") : 0.0;
                        addActivityCard("Created new meme",
                                title + " • Just now",
                                "Sentiment: " + sentiment + " • Toxicity: " +
                                        String.format("%.1f%%", toxicity) + " • Trendy: N/A",
                                "🎨");
                    }
                })
                .addOnFailureListener(e -> addDefaultActivity());
    }

    private void addDefaultActivity() {
        recentActivityContainer.removeAllViews();
        addActivityCard("Welcome to MEMEFY AI!", "Start creating memes • Just now", null, "❓");
        addActivityCard("Explore templates", "Generator ready • Now", null, "🤖");
    }

    private void addActivityCard(String title, String subtitle, String detail, String emoji) {
        float density = getResources().getDisplayMetrics().density;

        CardView card = new CardView(this);
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = (int)(12 * density);
        card.setLayoutParams(p);
        card.setRadius(16 * density);
        card.setCardElevation(4 * density);
        card.setCardBackgroundColor(0xFF1e2235);

        // Pink touch effect
        card.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.setForeground(getResources().getDrawable(R.drawable.pink_glow_border));
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.setForeground(null);
                    break;
            }
            return false;
        });

        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int pad = (int)(16 * density);
        row.setPadding(pad, pad, pad, pad);

        TextView emojiView = new TextView(this);
        emojiView.setText(emoji);
        emojiView.setTextSize(24f);
        emojiView.setGravity(Gravity.CENTER);
        emojiView.setBackground(getResources().getDrawable(R.drawable.activity_icon_bg));
        int s = (int)(48 * density);
        emojiView.setLayoutParams(new LinearLayout.LayoutParams(s, s));

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams tcp = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        tcp.leftMargin = (int)(16 * density);
        textCol.setLayoutParams(tcp);

        TextView tvTitle = new TextView(this);
        tvTitle.setText(title);
        tvTitle.setTextColor(0xFFFFFFFF);
        tvTitle.setTextSize(15f);
        tvTitle.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView tvSub = new TextView(this);
        tvSub.setText(subtitle);
        tvSub.setTextColor(0xFF8A8FA8);
        tvSub.setTextSize(13f);

        textCol.addView(tvTitle);
        textCol.addView(tvSub);

        if (detail != null) {
            TextView tvDetail = new TextView(this);
            tvDetail.setText(detail);
            tvDetail.setTextColor(0xFF666A80);
            tvDetail.setTextSize(12f);
            textCol.addView(tvDetail);
        }

        row.addView(emojiView);
        row.addView(textCol);
        card.addView(row);
        recentActivityContainer.addView(card);
    }

    // ── Tip of the day ─────────────────────────────────────────────────────────
    private void setRandomTip() {
        TextView tvTip = findViewById(R.id.tv_tip);
        int index = (int)(System.currentTimeMillis() / 86400000) % tips.length;
        tvTip.setText(tips[index]);
    }

    // ── Bottom nav ─────────────────────────────────────────────────────────────
    private void setupBottomNav() {
        findViewById(R.id.nav_home_btn).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        findViewById(R.id.nav_create_btn).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
        findViewById(R.id.nav_stats_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already on Stats!", Toast.LENGTH_SHORT).show());
        findViewById(R.id.nav_profile_btn).setOnClickListener(v ->
                startActivity(new Intent(this, ProfileActivity.class)));
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String formatCount(long count) {
        if (count >= 1000) return String.format("%.1fk", count / 1000.0);
        return String.valueOf(count);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStatsFromFirebase();
        loadRecentActivity();
    }
}