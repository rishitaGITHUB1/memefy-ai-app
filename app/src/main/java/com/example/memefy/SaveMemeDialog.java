package com.example.memefy;

import android.app.Dialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

public class SaveMemeDialog {

    public static void show(Context context, Bitmap memeBitmap, String templateName) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_save_meme);
        dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dialog.getWindow().setLayout(
                (int)(context.getResources().getDisplayMetrics().widthPixels * 0.92f),
                android.view.ViewGroup.LayoutParams.WRAP_CONTENT);

        ImageView ivPreview    = dialog.findViewById(R.id.iv_meme_preview);
        Button    btnDownload  = dialog.findViewById(R.id.btn_download);
        Button    btnSaveProfile = dialog.findViewById(R.id.btn_save_profile);
        Button    btnClose     = dialog.findViewById(R.id.btn_close_save);

        ivPreview.setImageBitmap(memeBitmap);

        // Download to gallery
        btnDownload.setOnClickListener(v -> {
            saveBitmapToGallery(context, memeBitmap, templateName);
            dialog.dismiss();
        });

        // Save to Firebase profile
        btnSaveProfile.setOnClickListener(v -> {
            if (!MemeSessionManager.isLoggedIn()) {
                Toast.makeText(context, "Login first to save to profile! 🔐", Toast.LENGTH_SHORT).show();
                context.startActivity(new Intent(context, LoginActivity.class));
                return;
            }
            saveMemeToProfile(context, memeBitmap, templateName);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private static void saveBitmapToGallery(Context context, Bitmap bitmap, String name) {
        try {
            String filename = "memefy_" + System.currentTimeMillis() + ".png";
            OutputStream fos;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/MemefyAI");
                Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                fos = context.getContentResolver().openOutputStream(uri);
            } else {
                java.io.File dir = new java.io.File(
                        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MemefyAI");
                dir.mkdirs();
                java.io.File file = new java.io.File(dir, filename);
                fos = new java.io.FileOutputStream(file);
            }

            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Toast.makeText(context, "Saved to gallery! 📸", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private static void saveMemeToProfile(Context context, Bitmap bitmap, String title) {
        // Convert bitmap to base64
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        // Scale down to save Firestore space
        Bitmap scaled = Bitmap.createScaledBitmap(bitmap, 600,
                (int)(600f * bitmap.getHeight() / bitmap.getWidth()), true);
        scaled.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        String base64Image = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);

        Map<String, Object> memeData = new HashMap<>();
        memeData.put("title",     title.isEmpty() ? "My Meme" : title);
        memeData.put("imageData", base64Image);
        memeData.put("views",     0);
        memeData.put("shares",    0);
        memeData.put("timestamp", com.google.firebase.Timestamp.now());

        String uid = MemeSessionManager.getUserId();
        FirebaseFirestore.getInstance()
                .collection("users").document(uid).collection("memes")
                .add(memeData)
                .addOnSuccessListener(ref -> {
                    Toast.makeText(context, "Saved to your profile! 🎨", Toast.LENGTH_SHORT).show();
                    // Navigate to profile to see it
                    context.startActivity(new Intent(context, ProfileActivity.class));
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Save failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}