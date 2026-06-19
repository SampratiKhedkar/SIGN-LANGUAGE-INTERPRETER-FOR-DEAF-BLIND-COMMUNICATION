package com.example.feelspeak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

public class MyProfileActivity extends AppCompatActivity {

    private static final int REQ_PICK_IMAGE = 101;

    private EditText etFirstName, etLastName, etUsername, etEmail, etPhone;
    private TextView tvInfo;
    private ImageView imgProfilePhoto;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // Toolbar setup
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarProfile);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        prefs = NavigationUtils.getPrefs(this);

        // Views
        imgProfilePhoto = findViewById(R.id.imgProfilePhoto);
        ImageButton btnEditPhoto = findViewById(R.id.btnEditPhoto);
        etFirstName = findViewById(R.id.etFirstName);
        etLastName  = findViewById(R.id.etLastName);
        etUsername  = findViewById(R.id.etUsername);
        etEmail     = findViewById(R.id.etEmail);
        etPhone     = findViewById(R.id.etPhone);
        Button btnSave = findViewById(R.id.btnSaveProfile);
        tvInfo      = findViewById(R.id.tvProfileInfo);

        btnSave.setOnClickListener(v -> saveProfile());

        // Load saved text fields
        etFirstName.setText(prefs.getString("profile_first_name", ""));
        etLastName.setText(prefs.getString("profile_last_name", ""));
        etUsername.setText(prefs.getString("profile_username", ""));
        etEmail.setText(prefs.getString("profile_email", ""));
        etPhone.setText(prefs.getString("profile_phone", ""));

        // Clear any old URI-based key once (optional)
        prefs.edit().remove("profile_photo_uri").apply();

        // Load saved photo from internal storage path
        String photoPath = prefs.getString("profile_photo_path", null);
        if (photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                imgProfilePhoto.setImageBitmap(bitmap);
            }
        }

        btnEditPhoto.setOnClickListener(v -> openGallery());
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, REQ_PICK_IMAGE);
    }

    // Copy selected image into internal storage and return its absolute path
    private String copyImageToInternalStorage(Uri sourceUri) throws IOException {
        InputStream in = getContentResolver().openInputStream(sourceUri);
        File dir = new File(getFilesDir(), "profile");
        if (!dir.exists()) dir.mkdirs();
        File outFile = new File(dir, "profile.jpg");
        OutputStream out = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
        return outFile.getAbsolutePath();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            if (imageUri != null) {
                // 1) Show preview immediately
                imgProfilePhoto.setImageURI(imageUri);

                // 2) Copy to internal storage and save path
                try {
                    String path = copyImageToInternalStorage(imageUri);
                    prefs.edit()
                            .putString("profile_photo_path", path)
                            .remove("profile_photo_uri")
                            .apply();
                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void saveProfile() {
        String first = etFirstName.getText().toString().trim();
        String last  = etLastName.getText().toString().trim();
        String user  = etUsername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        SharedPreferences.Editor ed = prefs.edit();
        ed.putString("profile_first_name", first);
        ed.putString("profile_last_name", last);
        ed.putString("profile_username", user);
        ed.putString("profile_email", email);
        ed.putString("profile_phone", phone);

        String fullName = (first + " " + last).trim();
        ed.putString(NavigationUtils.KEY_USER_NAME, fullName);
        ed.apply();

        tvInfo.setText("Profile saved");
        Toast.makeText(this, "Profile saved", Toast.LENGTH_SHORT).show();
    }
}