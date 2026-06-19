package com.example.feelspeak;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class NormalUploadVideoActivity extends AppCompatActivity {
    private ActivityResultLauncher<Intent> videoPickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_upload_video);

        // Toolbar (no drawer here)
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarUploadVideo);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Back arrow to go back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        videoPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Uri videoUri = result.getData().getData();
                        if (videoUri != null) {
                            Intent intent = new Intent(this, VideoPreviewActivity.class);
                            intent.putExtra("VIDEO_URI", videoUri.toString());
                            startActivity(intent);
                        }
                    }
                });

        Button btnSelectVideo = findViewById(R.id.btnSelectVideo);
        btnSelectVideo.setOnClickListener(v -> pickVideo());
    }

    private void pickVideo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("video/*");
        videoPickerLauncher.launch(intent);
    }
}