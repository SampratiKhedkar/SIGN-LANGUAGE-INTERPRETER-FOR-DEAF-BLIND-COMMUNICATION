package com.example.feelspeak;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences prefs;

    private ImageView imgMicCheck, imgCameraCheck, imgStorageCheck;

    private String[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_permission);

        prefs = getSharedPreferences("FeelSpeakPrefs", MODE_PRIVATE);
        Button btnGrant = findViewById(R.id.btnGrantPermissions);

        imgMicCheck = findViewById(R.id.imgMicCheck);
        imgCameraCheck = findViewById(R.id.imgCameraCheck);
        imgStorageCheck = findViewById(R.id.imgStorageCheck);

        // Build permissions array depending on Android version
        if (Build.VERSION.SDK_INT >= 33) {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_MEDIA_VIDEO
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.CAMERA,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            };
        }

        // Button: check + request permissions
        btnGrant.setOnClickListener(v -> checkAndRequestPermissions());

        // When screen opens, update ticks for already-granted permissions
        updateTicks();
    }

    private void checkAndRequestPermissions() {
        boolean allGranted = true;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                allGranted = false;
                break;
            }
        }

        if (allGranted) {
            prefs.edit().putBoolean("permissions_granted", true).apply();
            goNextAfterPermissions();
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    private void updateTicks() {
        // Mic
        imgMicCheck.setVisibility(
                ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                        == PackageManager.PERMISSION_GRANTED ? View.VISIBLE : View.GONE);

        // Camera
        imgCameraCheck.setVisibility(
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        == PackageManager.PERMISSION_GRANTED ? View.VISIBLE : View.GONE);

        // Storage / media
        if (Build.VERSION.SDK_INT >= 33) {
            imgStorageCheck.setVisibility(
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_VIDEO)
                            == PackageManager.PERMISSION_GRANTED ? View.VISIBLE : View.GONE);
        } else {
            imgStorageCheck.setVisibility(
                    ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            == PackageManager.PERMISSION_GRANTED ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean allGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }

            updateTicks();

            if (allGranted) {
                prefs.edit().putBoolean("permissions_granted", true).apply();
                goNextAfterPermissions();
            } else {
                Toast.makeText(this,
                        "All permissions needed for app to work!",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    private void goNextAfterPermissions() {
        // If user is already logged in and has user_type, go to correct home;
        // otherwise fall back to SignupActivity or LoginActivity as you prefer.
        String userType = prefs.getString(NavigationUtils.KEY_USER_TYPE, null);
        boolean remember = prefs.getBoolean(NavigationUtils.KEY_REMEMBER_ME, false);

        if (remember && userType != null) {
            NavigationUtils.goToCurrentHome(this);
        } else {
            startActivity(new Intent(this, SignupActivity.class));
        }
        finish();
    }
}