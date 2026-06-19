package com.example.feelspeak;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class NormalRecordVideoActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> videoCaptureLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_record_video);

        // Toolbar (no drawer here)
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarRecordVideo);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Back arrow to return to previous screen
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        Button btnRecordVideo = findViewById(R.id.btnRecordVideo);

        // Launcher for camera video capture
        videoCaptureLauncher = registerForActivityResult(
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

        // When user taps "Record Video" → open camera
        btnRecordVideo.setOnClickListener(v -> openCameraForVideo());
    }

    private void openCameraForVideo() {
        Intent intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        // Optionally:
        // intent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
        // intent.putExtra(MediaStore.EXTRA_DURATION_LIMIT, 10);
        videoCaptureLauncher.launch(intent);
    }
}