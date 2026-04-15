package com.example.memefy;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;

    private EditText etEmail, etPassword;
    private TextView tabLogin, tabSignup;
    private boolean isLoginMode = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();

        // If already logged in, go straight to ProfileActivity
        if (mAuth.getCurrentUser() != null) {
            goToProfile();
            return;
        }

        setupGoogleSignIn();
        initViews();
        setupListeners();
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void initViews() {
        etEmail    = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        tabLogin   = findViewById(R.id.tab_login);
        tabSignup  = findViewById(R.id.tab_signup);
    }

    private void setupListeners() {
        // Tab switching
        tabLogin.setOnClickListener(v -> {
            isLoginMode = true;
            tabLogin.setTextColor(0xFFFF6B9D);
            tabLogin.setBackgroundResource(R.drawable.card_normal_border);
            tabSignup.setTextColor(0xFF8A8FA8);
            tabSignup.setBackgroundResource(0);
            findViewById(R.id.btn_login_now).setEnabled(true);
            ((android.widget.Button) findViewById(R.id.btn_login_now)).setText("Login Now 👋");
        });

        tabSignup.setOnClickListener(v -> {
            isLoginMode = false;
            tabSignup.setTextColor(0xFFFF6B9D);
            tabSignup.setBackgroundResource(R.drawable.card_normal_border);
            tabLogin.setTextColor(0xFF8A8FA8);
            tabLogin.setBackgroundResource(0);
            ((android.widget.Button) findViewById(R.id.btn_login_now)).setText("Sign Up 🚀");
        });

        // Email/Password login or signup
        findViewById(R.id.btn_login_now).setOnClickListener(v -> {
            String email    = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                etEmail.setError("Email required");
                return;
            }
            if (TextUtils.isEmpty(password) || password.length() < 6) {
                etPassword.setError("Password must be 6+ characters");
                return;
            }

            if (isLoginMode) {
                signInWithEmail(email, password);
            } else {
                signUpWithEmail(email, password);
            }
        });

        // Google Sign-In
        findViewById(R.id.btn_google).setOnClickListener(v -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });

        // Bottom nav
        findViewById(R.id.nav_home_btn).setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
        findViewById(R.id.nav_create_btn).setOnClickListener(v ->
                startActivity(new Intent(this, DemoActivity.class)));
        findViewById(R.id.nav_gallery_btn).setOnClickListener(v ->
                startActivity(new Intent(this, GalleryActivity.class)));
        findViewById(R.id.nav_login_btn).setOnClickListener(v ->
                Toast.makeText(this, "Already on Login!", Toast.LENGTH_SHORT).show());
    }

    // ── Email/Password ─────────────────────────────────────────────────────────
    private void signInWithEmail(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Welcome back! 🎉", Toast.LENGTH_SHORT).show();
                        goToProfile();
                    } else {
                        Toast.makeText(this, "Login failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void signUpWithEmail(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Account created! Welcome 🚀", Toast.LENGTH_SHORT).show();
                        goToProfile();
                    } else {
                        Toast.makeText(this, "Sign up failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    // ── Google Sign-In ─────────────────────────────────────────────────────────
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign-in failed: " + e.getMessage(),
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Signed in with Google! 🎉", Toast.LENGTH_SHORT).show();
                        goToProfile();
                    } else {
                        Toast.makeText(this, "Firebase auth failed: " +
                                task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void goToProfile() {
        startActivity(new Intent(this, ProfileActivity.class));
        finish();
    }
}