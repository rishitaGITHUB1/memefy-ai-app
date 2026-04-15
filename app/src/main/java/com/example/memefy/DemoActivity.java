package com.example.memefy;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

public class DemoActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private ImageView btnMenu;
    private RecyclerView memesGrid;
    private Button btnLoadMore;
    private TextView tvShowingCount;
    private ProgressBar progressBar;

    private View sectionTemplates, sectionCustomize;
    private TextView tabTemplates, tabCustomize;

    private EditText etTopText, etBottomText;
    private Spinner spinnerFont, spinnerSize, spinnerAlign;
    private SeekBar seekHorizontal, seekVertical;
    private TextView tvHPercent, tvVPercent;
    private Button btnEffectShadow, btnEffectOutline, btnEffectGlow, btnEffectNeon;
    private Button btnGenerateMeme;
    private ImageView ivLivePreview;

    private int selectedMemeRes   = -1;
    private int selectedMemeIndex = 0;
    private String currentEffect  = "shadow";
    private int textColor   = Color.WHITE;
    private int hPercent    = 50;
    private int vTopPercent = 10;
    private int vBotPercent = 90;

    private final int[] allMemeImages = {
            R.drawable.meme1, R.drawable.meme2, R.drawable.meme3,
            R.drawable.meme4, R.drawable.meme5, R.drawable.meme1,
            R.drawable.meme2, R.drawable.meme3, R.drawable.meme4, R.drawable.meme5
    };
    private final String[] allMemeTitles = {
            "Drake Pointing", "Big Red Button", "Distracted Boyfriend",
            "Bernie Sanders", "UNO Draw 25", "Left Exit 12",
            "Anakin Padme", "Always Has Been", "This Is Fine", "Spongebob Mock"
    };

    private final List<Meme> displayedMemes = new ArrayList<>();
    private MemesAdapter adapter;
    private int currentCount = 0;
    private static final int LOAD_BATCH = 5;
    private static final int TOTAL = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        initViews();
        setupDrawer();
        setupGrid();
        setupTabs();
        setupCustomizePanel();
        setupListeners();
        setupBottomNav();
        loadMoreMemes();
    }

    private void initViews() {
        drawerLayout     = findViewById(R.id.drawer_layout);
        navigationView   = findViewById(R.id.nav_view);
        btnMenu          = findViewById(R.id.btn_menu);
        memesGrid        = findViewById(R.id.memes_grid);
        btnLoadMore      = findViewById(R.id.btn_load_more);
        tvShowingCount   = findViewById(R.id.tv_showing_count);
        progressBar      = findViewById(R.id.progress_bar);
        sectionTemplates = findViewById(R.id.section_templates);
        sectionCustomize = findViewById(R.id.section_customize);
        tabTemplates     = findViewById(R.id.tab_templates);
        tabCustomize     = findViewById(R.id.tab_customize);
        etTopText        = findViewById(R.id.et_top_text);
        etBottomText     = findViewById(R.id.et_bottom_text);
        spinnerFont      = findViewById(R.id.spinner_font);
        spinnerSize      = findViewById(R.id.spinner_size);
        spinnerAlign     = findViewById(R.id.spinner_align);
        seekHorizontal   = findViewById(R.id.seek_horizontal);
        seekVertical     = findViewById(R.id.seek_vertical);
        tvHPercent       = findViewById(R.id.tv_h_percent);
        tvVPercent       = findViewById(R.id.tv_v_percent);
        btnEffectShadow  = findViewById(R.id.btn_effect_shadow);
        btnEffectOutline = findViewById(R.id.btn_effect_outline);
        btnEffectGlow    = findViewById(R.id.btn_effect_glow);
        btnEffectNeon    = findViewById(R.id.btn_effect_neon);
        btnGenerateMeme  = findViewById(R.id.btn_generate_meme);
        ivLivePreview    = findViewById(R.id.iv_live_preview);
    }

    private void setupDrawer() {
        navigationView.setNavigationItemSelectedListener(this);
        View headerView = navigationView.getHeaderView(0);
        ImageView close = headerView.findViewById(R.id.btn_close_drawer);
        if (close != null) close.setOnClickListener(v -> drawerLayout.closeDrawer(GravityCompat.END));
    }

    private void setupGrid() {
        memesGrid.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new MemesAdapter(this, displayedMemes, (memeRes, index) -> {
            selectedMemeRes   = memeRes;
            selectedMemeIndex = index % allMemeTitles.length;
            switchToCustomize();
        });
        memesGrid.setAdapter(adapter);
    }

    private void setupTabs() {
        tabTemplates.setOnClickListener(v -> switchToTemplates());
        tabCustomize.setOnClickListener(v -> {
            if (selectedMemeRes == -1) Toast.makeText(this, "Pick a template first!", Toast.LENGTH_SHORT).show();
            else switchToCustomize();
        });
        findViewById(R.id.tab_ai).setOnClickListener(v ->
                Toast.makeText(this, "AI Generator coming soon! 🤖", Toast.LENGTH_SHORT).show());
        findViewById(R.id.tab_effects).setOnClickListener(v ->
                Toast.makeText(this, "Effects coming soon! ✨", Toast.LENGTH_SHORT).show());
    }

    private void switchToTemplates() {
        sectionTemplates.setVisibility(View.VISIBLE);
        sectionCustomize.setVisibility(View.GONE);
        tabTemplates.setBackgroundResource(R.drawable.gradient_button);
        tabCustomize.setBackgroundResource(R.drawable.outline_button);
    }

    private void switchToCustomize() {
        sectionTemplates.setVisibility(View.GONE);
        sectionCustomize.setVisibility(View.VISIBLE);
        tabTemplates.setBackgroundResource(R.drawable.outline_button);
        tabCustomize.setBackgroundResource(R.drawable.gradient_button);
        renderPreview();
    }

    private void setupCustomizePanel() {
        ArrayAdapter<String> fa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Impact (Classic)", "Sans Serif", "Serif", "Monospace"});
        fa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFont.setAdapter(fa);

        ArrayAdapter<String> sa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Small", "Medium", "Large", "X-Large"});
        sa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(sa);
        spinnerSize.setSelection(1);

        ArrayAdapter<String> aa = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item,
                new String[]{"Left", "Center", "Right"});
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerAlign.setAdapter(aa);
        spinnerAlign.setSelection(1);

        seekHorizontal.setMax(100); seekHorizontal.setProgress(50);
        seekVertical.setMax(100);   seekVertical.setProgress(10);
        tvHPercent.setText("50%");  tvVPercent.setText("10%");

        TextWatcher tw = new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            public void onTextChanged(CharSequence s, int i, int b, int c) { renderPreview(); }
            public void afterTextChanged(Editable s) {}
        };
        etTopText.addTextChangedListener(tw);
        etBottomText.addTextChangedListener(tw);

        AdapterView.OnItemSelectedListener sl = new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> p, View v, int pos, long id) { renderPreview(); }
            public void onNothingSelected(AdapterView<?> p) {}
        };
        spinnerFont.setOnItemSelectedListener(sl);
        spinnerSize.setOnItemSelectedListener(sl);
        spinnerAlign.setOnItemSelectedListener(sl);

        SeekBar.OnSeekBarChangeListener skl = new SeekBar.OnSeekBarChangeListener() {
            public void onProgressChanged(SeekBar s, int p, boolean u) {
                if (s.getId() == R.id.seek_horizontal) { hPercent = p; tvHPercent.setText(p + "%"); }
                else { vTopPercent = p; vBotPercent = 100 - p; tvVPercent.setText(p + "%"); }
                renderPreview();
            }
            public void onStartTrackingTouch(SeekBar s) {}
            public void onStopTrackingTouch(SeekBar s) {}
        };
        seekHorizontal.setOnSeekBarChangeListener(skl);
        seekVertical.setOnSeekBarChangeListener(skl);

        btnEffectShadow.setOnClickListener(v  -> { currentEffect = "shadow";  updateEffectButtons(); renderPreview(); });
        btnEffectOutline.setOnClickListener(v -> { currentEffect = "outline"; updateEffectButtons(); renderPreview(); });
        btnEffectGlow.setOnClickListener(v    -> { currentEffect = "glow";    updateEffectButtons(); renderPreview(); });
        btnEffectNeon.setOnClickListener(v    -> { currentEffect = "neon";    updateEffectButtons(); renderPreview(); });
        updateEffectButtons();

        btnGenerateMeme.setOnClickListener(v -> {
            if (selectedMemeRes == -1) { Toast.makeText(this, "Pick a template first!", Toast.LENGTH_SHORT).show(); return; }
            Bitmap bmp = buildMemeBitmap();
            String title = etTopText.getText().toString().trim();
            if (title.isEmpty()) title = allMemeTitles[selectedMemeIndex];
            SaveMemeDialog.show(this, bmp, title);
        });
    }

    private void setupBottomNav() {
        boolean loggedIn = MemeSessionManager.isLoggedIn();
        View bottomNav = findViewById(R.id.bottom_nav_demo);
        if (bottomNav == null) return;

        bottomNav.findViewById(R.id.nav_home_btn).setOnClickListener(v ->
                startActivity(new Intent(this, MainActivity.class)));
        bottomNav.findViewById(R.id.nav_create_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already creating! ✨", Toast.LENGTH_SHORT).show());
        bottomNav.findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));

        View statsBtn   = bottomNav.findViewById(R.id.nav_stats_btn);
        View profileBtn = bottomNav.findViewById(R.id.nav_profile_btn);
        View loginBtn   = bottomNav.findViewById(R.id.nav_login_btn);

        if (loggedIn) {
            if (statsBtn   != null) { statsBtn.setVisibility(View.VISIBLE); statsBtn.setOnClickListener(v -> startActivity(new Intent(this, DashboardActivity.class))); }
            if (profileBtn != null) { profileBtn.setVisibility(View.VISIBLE); profileBtn.setOnClickListener(v -> startActivity(new Intent(this, ProfileActivity.class))); }
            if (loginBtn   != null) loginBtn.setVisibility(View.GONE);
        } else {
            if (statsBtn   != null) statsBtn.setVisibility(View.GONE);
            if (profileBtn != null) profileBtn.setVisibility(View.GONE);
            if (loginBtn   != null) { loginBtn.setVisibility(View.VISIBLE); loginBtn.setOnClickListener(v -> startActivity(new Intent(this, LoginActivity.class))); }
        }
    }

    private void renderPreview() {
        if (selectedMemeRes == -1) return;
        ivLivePreview.setImageBitmap(buildMemeBitmap());
    }

    private Bitmap buildMemeBitmap() {
        Bitmap original = BitmapFactory.decodeResource(getResources(), selectedMemeRes);
        Bitmap mutable  = original.copy(Bitmap.Config.ARGB_8888, true);
        Canvas canvas   = new Canvas(mutable);
        int w = mutable.getWidth(), h = mutable.getHeight();

        Typeface tf;
        switch (spinnerFont.getSelectedItemPosition()) {
            case 1: tf = Typeface.SANS_SERIF; break;
            case 2: tf = Typeface.SERIF;      break;
            case 3: tf = Typeface.MONOSPACE;  break;
            default: tf = Typeface.create("Impact", Typeface.BOLD); break;
        }
        float baseSize;
        switch (spinnerSize.getSelectedItemPosition()) {
            case 0: baseSize = w * 0.06f; break;
            case 2: baseSize = w * 0.10f; break;
            case 3: baseSize = w * 0.13f; break;
            default: baseSize = w * 0.08f; break;
        }
        Paint.Align align; float xPos;
        switch (spinnerAlign.getSelectedItemPosition()) {
            case 0: align = Paint.Align.LEFT;  xPos = w * 0.05f; break;
            case 2: align = Paint.Align.RIGHT; xPos = w * 0.95f; break;
            default: align = Paint.Align.CENTER; xPos = w * (hPercent / 100f); break;
        }
        drawText(canvas, etTopText.getText().toString().trim().toUpperCase(), xPos, h * (vTopPercent / 100f), tf, baseSize, align);
        drawText(canvas, etBottomText.getText().toString().trim().toUpperCase(), xPos, h * (vBotPercent / 100f), tf, baseSize, align);
        return mutable;
    }

    private void drawText(Canvas canvas, String text, float x, float y, Typeface tf, float size, Paint.Align align) {
        if (text.isEmpty()) return;
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setTypeface(tf); p.setTextSize(size); p.setTextAlign(align); p.setColor(textColor);
        switch (currentEffect) {
            case "shadow":  p.setShadowLayer(6f, 3f, 3f, Color.BLACK); break;
            case "outline":
                Paint s = new Paint(p); s.setStyle(Paint.Style.STROKE); s.setStrokeWidth(size * 0.08f); s.setColor(Color.BLACK);
                canvas.drawText(text, x, y, s); break;
            case "glow":    p.setShadowLayer(18f, 0, 0, Color.argb(200, 100, 200, 255)); break;
            case "neon":    p.setShadowLayer(24f, 0, 0, Color.argb(220, 255, 50, 255)); p.setColor(Color.argb(255, 255, 180, 255)); break;
        }
        canvas.drawText(text, x, y, p);
    }

    private void updateEffectButtons() {
        btnEffectShadow.setBackgroundResource(currentEffect.equals("shadow")  ? R.drawable.gradient_button : R.drawable.outline_button);
        btnEffectOutline.setBackgroundResource(currentEffect.equals("outline") ? R.drawable.gradient_button : R.drawable.outline_button);
        btnEffectGlow.setBackgroundResource(currentEffect.equals("glow")    ? R.drawable.gradient_button : R.drawable.outline_button);
        btnEffectNeon.setBackgroundResource(currentEffect.equals("neon")    ? R.drawable.gradient_button : R.drawable.outline_button);
    }

    private void setupListeners() {
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(GravityCompat.END));
        btnLoadMore.setOnClickListener(v -> loadMoreMemes());
        int[] ids = {R.id.filter_all,R.id.filter_popular,R.id.filter_trending,R.id.filter_classic,R.id.filter_gaming,R.id.filter_reaction};
        String[] names = {"All","Popular","Trending","Classic","Gaming","Reaction"};
        for (int i = 0; i < ids.length; i++) { final String n = names[i]; findViewById(ids[i]).setOnClickListener(v -> Toast.makeText(this, n + "!", Toast.LENGTH_SHORT).show()); }
    }

    private void loadMoreMemes() {
        int remaining = TOTAL - currentCount;
        if (remaining <= 0) { btnLoadMore.setEnabled(false); return; }
        int toLoad = Math.min(LOAD_BATCH, allMemeImages.length);
        for (int i = 0; i < toLoad; i++) { int idx = (currentCount + i) % allMemeImages.length; displayedMemes.add(new Meme(allMemeImages[idx], allMemeTitles[idx])); }
        currentCount = Math.min(currentCount + toLoad, TOTAL);
        adapter.notifyDataSetChanged();
        int still = TOTAL - currentCount;
        tvShowingCount.setText("Showing " + currentCount + " of " + TOTAL + " templates");
        progressBar.setProgress((currentCount * 100) / TOTAL);
        if (still > 0) btnLoadMore.setText("🚀  Load " + Math.min(20, still) + " More (" + still + " remaining)");
        else { btnLoadMore.setEnabled(false); btnLoadMore.setText("All loaded! 🎉"); }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.nav_home)           startActivity(new Intent(this, MainActivity.class));
        else if (id == R.id.nav_dashboard) startActivity(new Intent(this, DashboardActivity.class));
        else if (id == R.id.nav_gallery)   startActivity(new Intent(this, GalleryActivity.class));
        else if (id == R.id.nav_profile)   startActivity(new Intent(this, ProfileActivity.class));
        else if (id == R.id.nav_logout) { com.google.firebase.auth.FirebaseAuth.getInstance().signOut(); startActivity(new Intent(this, MainActivity.class)); }
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) drawerLayout.closeDrawer(GravityCompat.END);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) drawerLayout.closeDrawer(GravityCompat.END);
        else if (sectionCustomize != null && sectionCustomize.getVisibility() == View.VISIBLE) switchToTemplates();
        else super.onBackPressed();
    }
}