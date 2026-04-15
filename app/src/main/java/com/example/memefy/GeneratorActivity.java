package com.example.memefy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialog;

public class GeneratorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showGeneratorDialog();
    }

    private void showGeneratorDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);

        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_generator_login, null);
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.setCancelable(true);

        ImageView btnClose       = dialogView.findViewById(R.id.btn_close_dialog);
        CardView  btnGoogleLogin = dialogView.findViewById(R.id.btn_google_login);
        Button    btnDemoMode    = dialogView.findViewById(R.id.btn_demo_mode);

        // Close → dismiss and go back to MainActivity
        btnClose.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            finish();
        });

        // Google Login (placeholder)
        btnGoogleLogin.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            Toast.makeText(this, "Google Sign-In coming soon! 🔐", Toast.LENGTH_SHORT).show();
            finish();
        });

        // Demo Mode → open full Meme Factory screen
        btnDemoMode.setOnClickListener(v -> {
            bottomSheetDialog.dismiss();
            startActivity(new Intent(GeneratorActivity.this, DemoActivity.class));
            finish();
        });

        // Dismissed by back button or outside tap → go back
        bottomSheetDialog.setOnDismissListener(dialog -> {
            if (!isFinishing()) finish();
        });

        bottomSheetDialog.show();
    }
}