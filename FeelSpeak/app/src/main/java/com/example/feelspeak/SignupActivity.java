package com.example.feelspeak;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class SignupActivity extends AppCompatActivity {

    private EditText etUserName, etEmail, etPassword, etConfirmPassword;
    private TextView tvGoToLogin, tvInfo;
    private Button btnSignup;

    private FirebaseAuth auth;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        auth = FirebaseAuth.getInstance();
        prefs = NavigationUtils.getPrefs(this);

        etUserName        = findViewById(R.id.etSignupUserName);
        etEmail           = findViewById(R.id.etSignupEmail);
        etPassword        = findViewById(R.id.etSignupPassword);
        etConfirmPassword = findViewById(R.id.etSignupConfirmPassword);
        btnSignup         = findViewById(R.id.btnSignup);
        tvGoToLogin       = findViewById(R.id.tvGoToLogin);
        tvInfo            = findViewById(R.id.tvSignupInfo);

        // Load saved username if returning user
        String savedUsername = prefs.getString("profile_username", "");
        etUserName.setText(savedUsername);

        btnSignup.setOnClickListener(v -> signupWithEmail());

        tvGoToLogin.setOnClickListener(v ->
                startActivity(new Intent(SignupActivity.this, LoginActivity.class)));
    }

    private void signupWithEmail() {
        String username = etUserName.getText().toString().trim();
        String email    = etEmail.getText().toString().trim();
        String pass     = etPassword.getText().toString().trim();
        String conf     = etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUserName.setError("Enter user name");
            return;
        }
        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }
        if (!pass.equals(conf)) {
            etConfirmPassword.setError("Passwords do not match");
            return;
        }

        tvInfo.setText("Creating account...");
        auth.createUserWithEmailAndPassword(email, pass)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            // Save username and email to SharedPreferences (same keys as My Profile uses)
                            prefs.edit()
                                    .putString(NavigationUtils.KEY_USER_NAME, username)
                                    .putString("profile_username", username)
                                    .putString("profile_email", email)
                                    .apply();

                            user.sendEmailVerification()
                                    .addOnCompleteListener(t -> {
                                        if (t.isSuccessful()) {
                                            tvInfo.setText("Verification email sent. Please check inbox.");
                                            Toast.makeText(SignupActivity.this,
                                                    "Check your email for verification link",
                                                    Toast.LENGTH_LONG).show();

                                            // After signup → go to user type selection
                                            Intent intent = new Intent(SignupActivity.this,
                                                    UserTypeSelectionActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else {
                                            tvInfo.setText("Failed to send verification email.");
                                        }
                                    });
                        }
                    } else {
                        tvInfo.setText("Signup failed: " +
                                (task.getException() != null
                                        ? task.getException().getMessage()
                                        : ""));
                    }
                });
    }
}