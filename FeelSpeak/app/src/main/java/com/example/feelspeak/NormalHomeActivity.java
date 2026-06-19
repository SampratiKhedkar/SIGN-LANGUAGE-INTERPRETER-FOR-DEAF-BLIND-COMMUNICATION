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
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class NormalHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView textUsername;
    private ImageView profileImage;
    private MaterialButton menuHome, menuProfile, menuNotifications,
            menuAbout, menuHelp, menuSettings, menuLogout;
    private Chip buttonThemeLight, buttonThemeDark;

    // Main buttons
    private Button btnRecordAudio, btnRecordVideo, btnUploadVideo;

    private MaterialToolbar topAppBar;
    private static final String PREFS_NAME = "FeelSpeakPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearBadPhotoOnce();
        setContentView(R.layout.activity_normal_home);

        drawerLayout = findViewById(R.id.drawer_layout);
        topAppBar = findViewById(R.id.topAppBar);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        initDrawerViews();
        initMainButtons();
        setupToolbar();
        setupUserName();
        setupThemeButtons();
        setupMenuClicks();

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

    private void initDrawerViews() {
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
    }

    // main buttons in the center content
    private void initMainButtons() {
        btnRecordAudio = findViewById(R.id.btnRecordAudio);
        btnRecordVideo = findViewById(R.id.btnRecordVideo);
        btnUploadVideo = findViewById(R.id.btnUploadVideo);

        Button btnRecordAudio = findViewById(R.id.btnRecordAudio);
        btnRecordAudio.setOnClickListener(v -> {
            Intent intent = new Intent(this, NormalRecordAudioActivity.class);
            startActivity(intent);
        });

        if (btnRecordVideo != null) {
            btnRecordVideo.setOnClickListener(v -> {
                Intent intent = new Intent(this, NormalRecordVideoActivity.class);
                startActivity(intent);
            });
        }

        if (btnUploadVideo != null) {
            btnUploadVideo.setOnClickListener(v -> {
                Intent intent = new Intent(this, NormalUploadVideoActivity.class);
                startActivity(intent);
            });
        }
    }

    private void setupToolbar() {
        // Navigation (hamburger) icon opens the drawer
        topAppBar.setNavigationOnClickListener(v -> {
            drawerLayout.openDrawer(GravityCompat.START);
        });
    }

    private void setupUserName() {
        SharedPreferences prefs = NavigationUtils.getPrefs(this);

        String name = prefs.getString(NavigationUtils.KEY_USER_NAME, "Guest");
        if (textUsername != null) {
            textUsername.setText(name);
        }

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
        if (buttonThemeLight == null || buttonThemeDark == null) return;

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String currentTheme = prefs.getString("theme_mode", "light");

        buttonThemeLight.setChecked("light".equals(currentTheme));
        buttonThemeDark.setChecked("dark".equals(currentTheme));

        buttonThemeLight.setOnClickListener(v -> {
            saveThemeMode("light");
            buttonThemeLight.setChecked(true);
            buttonThemeDark.setChecked(false);
            Toast.makeText(this, "Light theme selected", Toast.LENGTH_SHORT).show();
        });

        buttonThemeDark.setOnClickListener(v -> {
            saveThemeMode("dark");
            buttonThemeDark.setChecked(true);
            buttonThemeLight.setChecked(false);
            Toast.makeText(this, "Dark theme selected", Toast.LENGTH_SHORT).show();
        });
    }

    private void saveThemeMode(String mode) {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        prefs.edit().putString("theme_mode", mode).apply();
    }

    private void setupMenuClicks() {
        if (menuHome != null) {
            menuHome.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                NavigationUtils.goToCurrentHome(NormalHomeActivity.this);
                finish();
            });
        }

        if (menuSettings != null) {
            menuSettings.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, SettingsActivity.class));
            });
        }

        if (menuLogout != null) {
            menuLogout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                handleLogout();
            });
        }

        if (menuProfile != null) {
            menuProfile.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, MyProfileActivity.class));
            });
        }

        if (menuNotifications != null) {
            menuNotifications.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, NotificationsActivity.class));
            });
        }

        if (menuAbout != null) {
            menuAbout.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, AboutUsActivity.class));
            });
        }

        if (menuHelp != null) {
            menuHelp.setOnClickListener(v -> {
                drawerLayout.closeDrawer(GravityCompat.START);
                startActivity(new Intent(this, HelpActivity.class));
            });
        }
    }

    private void goToCurrentHome() {
        // Always route via NavigationUtils and close this activity
        NavigationUtils.goToCurrentHome(this);
        finish();
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