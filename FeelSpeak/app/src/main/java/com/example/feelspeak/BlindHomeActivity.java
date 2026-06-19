package com.example.feelspeak;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.firebase.auth.FirebaseAuth;

import java.util.ArrayList;

public class BlindHomeActivity extends AppCompatActivity {

    private DrawerLayout drawerLayout;
    private TextView textUsername;

    private MaterialButton menuHome, menuProfile, menuNotifications,
            menuAbout, menuHelp, menuSettings, menuLogout;
    private Chip buttonThemeLight, buttonThemeDark;

    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private TextView tvResult;
    private boolean isRecording = false;
    private static final int PERMISSION_REQUEST_CODE = 200;
    private ImageView profileImage;
    private static final String PREFS_NAME = "FeelSpeakPrefs";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        clearBadPhotoOnce();
        setContentView(R.layout.activity_blind_home);

        drawerLayout = findViewById(R.id.drawer_layout_blind);
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarBlind);
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

        // Speech recognizer setup
        tvResult = findViewById(R.id.tvResult);
        initSpeechRecognizer();

        View root = findViewById(R.id.rootBlindLayout);
        root.setOnClickListener(v -> toggleRecording());

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

        // Load from internal-storage path
        String photoPath = prefs.getString("profile_photo_path", null);
        if (profileImage != null && photoPath != null) {
            Bitmap bitmap = BitmapFactory.decodeFile(photoPath);
            if (bitmap != null) {
                profileImage.setImageBitmap(bitmap);
            } else {
                // optional default
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
            NavigationUtils.goToCurrentHome(BlindHomeActivity.this);
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

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    tvResult.setText(matches.get(0));
                } else {
                    tvResult.setText("No speech recognized. Tap again.");
                }
                isRecording = false;
            }

            @Override
            public void onError(int error) {
                isRecording = false;
                tvResult.setText("Error. Tap again to retry.");
            }

            @Override public void onReadyForSpeech(Bundle params) {}
            @Override public void onBeginningOfSpeech() {}
            @Override public void onRmsChanged(float rmsdB) {}
            @Override public void onBufferReceived(byte[] buffer) {}
            @Override public void onEndOfSpeech() {}
            @Override public void onPartialResults(Bundle partialResults) {}
            @Override public void onEvent(int eventType, Bundle params) {}
        });
    }

    private void toggleRecording() {
        if (!hasMicPermission()) {
            requestMicPermission();
            return;
        }

        if (!isRecording) {
            startRecording();
        } else {
            stopRecording();
        }
    }

    private void startRecording() {
        try {
            speechRecognizer.startListening(speechRecognizerIntent);
            isRecording = true;
            tvResult.setText("🔴 Recording... Tap anywhere to stop.");
        } catch (Exception e) {
            Toast.makeText(this, "Speech recognition not available", Toast.LENGTH_SHORT).show();
        }
    }

    private void stopRecording() {
        if (speechRecognizer != null) {
            speechRecognizer.stopListening();
        }
    }

    private boolean hasMicPermission() {
        return ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMicPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupUserName();
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}