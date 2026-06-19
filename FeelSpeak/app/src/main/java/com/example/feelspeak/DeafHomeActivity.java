package com.example.feelspeak;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

public class DeafHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView textUsername;
    private ImageView profileImage;
    private MaterialButton menuHome, menuProfile, menuNotifications,
            menuAbout, menuHelp, menuSettings, menuLogout;
    private Chip buttonThemeLight, buttonThemeDark;

    private static final String PREFS_NAME = "FeelSpeakPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearBadPhotoOnce();
        setContentView(R.layout.activity_deaf_home);

        drawerLayout = findViewById(R.id.drawer_layout_deaf);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarDeaf);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Navigation icon opens drawer
        topAppBar.setNavigationOnClickListener(v ->
                drawerLayout.openDrawer(GravityCompat.START));

        // Drawer views
        textUsername = findViewById(R.id.text_username);
        profileImage = findViewById(R.id.profile_image);
        menuHome = findViewById(R.id.menu_home);
        menuProfile = findViewById(R.id.menu_profile);
        menuNotifications = findViewById(R.id.menu_notifications);
        menuAbout = findViewById(R.id.menu_about);
        menuHelp = findViewById(R.id.menu_help);
        menuSettings = findViewById(R.id.menu_settings);
        menuLogout = findViewById(R.id.menu_logout);
        buttonThemeLight = findViewById(R.id.button_theme_light);
        buttonThemeDark = findViewById(R.id.button_theme_dark);

        setupUserName();
        setupMenuClicks();
        setupThemeButtons();

        // Deaf-specific buttons
        Button btnRecordVideo = findViewById(R.id.btnRecordVideo);
        Button btnUploadVideo = findViewById(R.id.btnUploadVideo);

        btnRecordVideo.setOnClickListener(v ->
                startActivity(new Intent(this, NormalRecordVideoActivity.class)));

        btnUploadVideo.setOnClickListener(v ->
                startActivity(new Intent(this, NormalUploadVideoActivity.class)));

        // Back press handling with drawer
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    finish();
                }
            }
        });
    }

    private void setupUserName() {
        SharedPreferences prefs = NavigationUtils.getPrefs(this);
        String name = prefs.getString(NavigationUtils.KEY_USER_NAME, "Guest");
        textUsername.setText(name);

        String photoPath = prefs.getString("profile_photo_path", null);
        if (profileImage != null && photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            } else {
                // profileImage.setImageResource(R.drawable.ic_profile);
            }
        }
    }

    private void clearBadPhotoOnce() {
        SharedPreferences prefs = NavigationUtils.getPrefs(this);
        prefs.edit().remove("profile_photo_uri").apply();
    }

    private void setupThemeButtons() {
        buttonThemeLight.setOnClickListener(v -> saveThemeMode("light"));
        buttonThemeDark.setOnClickListener(v -> saveThemeMode("dark"));
    }

    private void saveThemeMode(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString("theme_mode", mode).apply();
    }

    private void setupMenuClicks() {
        menuHome.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            NavigationUtils.goToCurrentHome(DeafHomeActivity.this);
            finish();
        });

        menuSettings.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, SettingsActivity.class));
        });

        menuLogout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            handleLogout();
        });

        menuProfile.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, MyProfileActivity.class));
        });

        menuNotifications.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, NotificationsActivity.class));
        });

        menuAbout.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, AboutUsActivity.class));
        });

        menuHelp.setOnClickListener(v -> {
            drawerLayout.closeDrawer(GravityCompat.START);
            startActivity(new Intent(this, HelpActivity.class));
        });
    }

    private void handleLogout() {
        FirebaseAuth.getInstance().signOut();
        SharedPreferences prefs = NavigationUtils.getPrefs(this);
        prefs.edit()
                .remove(NavigationUtils.KEY_REMEMBER_ME)
                .remove(NavigationUtils.KEY_USER_TYPE)
                .apply();

        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUserName();
    }

}