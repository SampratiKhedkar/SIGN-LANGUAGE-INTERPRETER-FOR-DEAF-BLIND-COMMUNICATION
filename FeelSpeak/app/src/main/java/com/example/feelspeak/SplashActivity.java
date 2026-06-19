package com.example.feelspeak;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private static final long HOLD_TIME = 1750L;     // 1.75 seconds before zoom
    private static final long ZOOM_DURATION = 900L;  // zoom-out duration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Fullscreen splash
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.activity_splash);

        ImageView logo = findViewById(R.id.imgLogo);

        logo.setScaleX(1f);
        logo.setScaleY(1f);
        logo.setAlpha(1f);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            logo.animate()
                    .scaleX(10.0f)
                    .scaleY(10.0f)
                    .alpha(0f)
                    .setDuration(ZOOM_DURATION)
                    .setInterpolator(new AccelerateInterpolator(1.5f))
                    .withEndAction(() -> {
                        logo.setVisibility(View.GONE);
                        navigateToCorrectScreen();
                    })
                    .start();
        }, HOLD_TIME);
    }

    private void navigateToCorrectScreen() {
        SharedPreferences prefs = NavigationUtils.getPrefs(this);

        boolean permissionsGranted = prefs.getBoolean("permissions_granted", false);
        boolean rememberMe = prefs.getBoolean(NavigationUtils.KEY_REMEMBER_ME, false);
        String userType = prefs.getString(NavigationUtils.KEY_USER_TYPE, null);

        if (!permissionsGranted) {
            startActivity(new Intent(this, PermissionActivity.class));
        } else if (rememberMe && userType != null) {
            NavigationUtils.goToCurrentHome(this);
        } else {
            startActivity(new Intent(this, LoginActivity.class));
        }

        finish();
    }
}