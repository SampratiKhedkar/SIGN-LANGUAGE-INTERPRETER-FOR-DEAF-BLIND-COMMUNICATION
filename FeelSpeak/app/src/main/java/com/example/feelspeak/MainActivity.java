package com.example.feelspeak;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_CODE = 100;
    private static final int PICK_VIDEO = 101;
    private static final int OPEN_CAMERA = 102;

    private Button btnCamera, btnUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarMain);
        setSupportActionBar(topAppBar);

        btnCamera = findViewById(R.id.btnOpenCamera);
        btnUpload = findViewById(R.id.btnPickVideo);

        requestPermissions();

        btnUpload.setOnClickListener(v -> pickVideo());
        btnCamera.setOnClickListener(v -> openCamera());
    }

    private void requestPermissions() {
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                (android.os.Build.VERSION.SDK_INT >= 33 &&
                        checkSelfPermission(Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED) ||
                (android.os.Build.VERSION.SDK_INT < 33 &&
                        checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED)) {

            if (android.os.Build.VERSION.SDK_INT >= 33) {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_MEDIA_VIDEO
                }, PERMISSION_CODE);
            } else {
                requestPermissions(new String[]{
                        Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, PERMISSION_CODE);
            }
        }
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_VIDEO);
    }

    private void openCamera() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        startActivityForResult(intent, OPEN_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && data != null) {
            Uri videoUri = data.getData();
            if (videoUri != null) {
                Intent intent = new Intent(MainActivity.this, VideoPreviewActivity.class);
                intent.putExtra("VIDEO_URI", videoUri.toString());
                startActivity(intent);
            } else {
                Toast.makeText(this, "Video URI is null!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}