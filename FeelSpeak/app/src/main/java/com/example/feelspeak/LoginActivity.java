package com.example.feelspeak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.android.material.button.MaterialButton;


public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView tvForgotPassword, tvGoToSignup, tvInfo;
    private MaterialButton btnLogin, btnLoginWithGoogle;
    private CheckBox cbRememberMe;

    private SharedPreferences prefs;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Firebase Auth
        auth = FirebaseAuth.getInstance();

        // SharedPreferences
        prefs = getSharedPreferences("FeelSpeakPrefs", MODE_PRIVATE);

        // Find views
        etEmail          = findViewById(R.id.etLoginEmail);
        etPassword       = findViewById(R.id.etLoginPassword);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvGoToSignup     = findViewById(R.id.tvGoToSignup);
        tvInfo           = findViewById(R.id.tvLoginInfo);
        btnLogin         = findViewById(R.id.btnLogin);
        btnLoginWithGoogle = findViewById(R.id.btnLoginWithGoogle);
        cbRememberMe     = findViewById(R.id.cbRememberMe);

        // If Remember Me was checked earlier, skip login and go straight to home / type select
        boolean remember = prefs.getBoolean("remember_me", false);
        if (remember) {
            goDirectToNextScreen();
            return;
        }

        // Login button
        btnLogin.setOnClickListener(v -> loginWithEmail());

        // "Don't have an account? Signup"
        tvGoToSignup.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, SignupActivity.class)));

        // "Forgot password?"
        tvForgotPassword.setOnClickListener(v ->
                startActivity(new Intent(LoginActivity.this, ForgotPasswordActivity.class)));

        // Google login placeholder
        btnLoginWithGoogle.setOnClickListener(v ->
                Toast.makeText(this, "Google login wired later", Toast.LENGTH_SHORT).show());
    }

    private void loginWithEmail() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Enter password");
            return;
        }

        tvInfo.setText("Signing in...");
        auth.signInWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            user.reload().addOnCompleteListener(t -> {
                                if (user.isEmailVerified()) {
                                    tvInfo.setText("Login successful");

                                    // Save Remember Me flag based on checkbox
                                    prefs.edit()
                                            .putBoolean("remember_me", cbRememberMe.isChecked())
                                            .apply();

                                    goDirectToNextScreen();
                                } else {
                                    tvInfo.setText("Please verify your email first.");
                                }
                            });
                        }
                    } else {
                        tvInfo.setText("Login failed: " +
                                (task.getException() != null ? task.getException().getMessage() : ""));
                    }
                });
    }

    /**
     * Decide where to go after successful login or when Remember Me is true.
     * Uses stored user_type: BLIND / DEAF / NORMAL.
     */
    private void goDirectToNextScreen() {
        String userType = prefs.getString("user_type", null);

        Intent i;
        if (userType == null) {
            // No type chosen yet → ask on UserTypeSelectionActivity
            i = new Intent(LoginActivity.this, UserTypeSelectionActivity.class);
        } else {
            switch (userType) {
                case "BLIND":
                    i = new Intent(LoginActivity.this, BlindHomeActivity.class);
                    break;
                case "DEAF":
                    i = new Intent(LoginActivity.this, DeafHomeActivity.class);
                    break;
                default:
                    i = new Intent(LoginActivity.this, NormalHomeActivity.class);
                    break;
            }
        }

        startActivity(i);
        finish();
    }
}