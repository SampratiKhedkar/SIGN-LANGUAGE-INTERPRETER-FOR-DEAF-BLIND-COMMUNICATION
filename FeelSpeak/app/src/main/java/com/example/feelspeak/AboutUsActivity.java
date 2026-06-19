package com.example.feelspeak;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.material.appbar.MaterialToolbar;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        MaterialToolbar topAppBar = findViewById(R.id.topAppBarAbout);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());
    }
}