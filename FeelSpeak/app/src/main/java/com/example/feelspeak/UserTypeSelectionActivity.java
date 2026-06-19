package com.example.feelspeak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.appbar.MaterialToolbar;

public class UserTypeSelectionActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_type_selection);

        prefs = NavigationUtils.getPrefs(this);

        // Toolbar (no back button)
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarUserType);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        Button btnBlind  = findViewById(R.id.btnBlind);
        Button btnNormal = findViewById(R.id.btnNormal);
        Button btnDeaf   = findViewById(R.id.btnDeaf);

        btnBlind.setOnClickListener(v -> selectUserType("BLIND"));
        btnNormal.setOnClickListener(v -> selectUserType("NORMAL"));
        btnDeaf.setOnClickListener(v -> selectUserType("DEAF"));
    }

    private void selectUserType(String type) {
        // Store user type with shared key used everywhere else
        prefs.edit().putString(NavigationUtils.KEY_USER_TYPE, type).apply();

        Intent intent;
        switch (type) {
            case "BLIND":
                intent = new Intent(this, BlindHomeActivity.class);
                break;
            case "DEAF":
                intent = new Intent(this, DeafHomeActivity.class);
                break;
            default:
                intent = new Intent(this, NormalHomeActivity.class);
                break;
        }
        startActivity(intent);
        finish();
    }
}