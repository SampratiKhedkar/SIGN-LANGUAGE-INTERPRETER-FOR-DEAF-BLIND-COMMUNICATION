package com.example.feelspeak;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AlertDialog;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

public class SettingsActivity extends AppCompatActivity {

    private RadioGroup radioGroupUserType;
    private RadioButton radioNormal, radioDeaf, radioBlind;
    private Button buttonSaveUserType, btnDeleteAccount, btnChangePassword;

    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        prefs = NavigationUtils.getPrefs(this);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarSettings);
        setSupportActionBar(topAppBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Optional custom title
        TextView toolbarTitle = topAppBar.findViewById(R.id.toolbarTitleSettings);

        // User Type Selection Views
        radioGroupUserType = findViewById(R.id.radio_group_user_type);
        radioNormal = findViewById(R.id.radio_normal);
        radioDeaf = findViewById(R.id.radio_deaf);
        radioBlind = findViewById(R.id.radio_blind);
        buttonSaveUserType = findViewById(R.id.button_save_user_type);

        // Account Settings Views
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnDeleteAccount = findViewById(R.id.btnDeleteAccount);

        // Load current user type
        loadCurrentUserType();

        // Save user type click listener
        buttonSaveUserType.setOnClickListener(v -> saveUserType());

        // Account settings click listeners
        btnChangePassword.setOnClickListener(v -> handleChangePassword());
        btnDeleteAccount.setOnClickListener(v -> showDeleteConfirmationDialog());
    }

    private void loadCurrentUserType() {
        String userType = prefs.getString(NavigationUtils.KEY_USER_TYPE, "NORMAL");

        if ("BLIND".equalsIgnoreCase(userType)) {
            radioBlind.setChecked(true);
        } else if ("DEAF".equalsIgnoreCase(userType)) {
            radioDeaf.setChecked(true);
        } else {
            radioNormal.setChecked(true);
        }
    }

    private void saveUserType() {
        String newType = "NORMAL";
        int checkedId = radioGroupUserType.getCheckedRadioButtonId();
        if (checkedId == R.id.radio_blind) {
            newType = "BLIND";
        } else if (checkedId == R.id.radio_deaf) {
            newType = "DEAF";
        }

        prefs.edit().putString(NavigationUtils.KEY_USER_TYPE, newType).apply();
        Toast.makeText(this, "Preference saved", Toast.LENGTH_SHORT).show();
        NavigationUtils.goToCurrentHome(this);
        finish();
    }

    private void handleChangePassword() {
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            auth.sendPasswordResetEmail(user.getEmail())
                    .addOnSuccessListener(aVoid ->
                            Toast.makeText(this,
                                    "Password reset email sent to " + user.getEmail(),
                                    Toast.LENGTH_LONG).show()
                    )
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to send reset email: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show()
                    );
        } else {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Account")
                .setMessage("Are you sure you want to delete your account? This action cannot be undone. All your data will be permanently deleted.")
                .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void deleteAccount() {
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(this, "No user logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        String userId = user.getUid();

        Toast.makeText(this, "Deleting account...", Toast.LENGTH_SHORT).show();

        db.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    user.delete()
                            .addOnSuccessListener(aVoid2 -> {
                                clearAllUserData();
                                Toast.makeText(SettingsActivity.this,
                                        "Account deleted successfully",
                                        Toast.LENGTH_SHORT).show();

                                Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                                finish();
                            })
                            .addOnFailureListener(e -> Toast.makeText(
                                    SettingsActivity.this,
                                    "Failed to delete account: " + e.getMessage(),
                                    Toast.LENGTH_SHORT
                            ).show());
                })
                .addOnFailureListener(e -> Toast.makeText(
                        SettingsActivity.this,
                        "Failed to delete user data: " + e.getMessage(),
                        Toast.LENGTH_SHORT
                ).show());
    }

    private void clearAllUserData() {
        SharedPreferences.Editor editor = prefs.edit();

        editor.remove("profile_first_name");
        editor.remove("profile_last_name");
        editor.remove("profile_username");
        editor.remove("profile_email");
        editor.remove("profile_phone");
        editor.remove("profile_photo_path");
        editor.remove("profile_photo_uri");

        editor.remove(NavigationUtils.KEY_USER_NAME);
        editor.remove(NavigationUtils.KEY_REMEMBER_ME);
        editor.remove(NavigationUtils.KEY_USER_TYPE);

        editor.remove("theme_mode");

        editor.clear();
        editor.apply();
    }
}
