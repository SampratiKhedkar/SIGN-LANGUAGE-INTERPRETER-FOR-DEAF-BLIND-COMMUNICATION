package com.example.feelspeak;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private TextView tvInfo;
    private MaterialButton btnSendReset;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        auth = FirebaseAuth.getInstance();

        etEmail      = findViewById(R.id.etForgotEmail);
        tvInfo       = findViewById(R.id.tvForgotInfo);
        btnSendReset = findViewById(R.id.btnSendReset);

        btnSendReset.setOnClickListener(v -> sendResetEmail());
    }

    private void sendResetEmail() {
        String email = etEmail.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Enter email");
            return;
        }

        tvInfo.setText("Sending reset email...");
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        tvInfo.setText("Reset email sent. Check your inbox.");
                        Toast.makeText(this,
                                "Reset link sent to your email",
                                Toast.LENGTH_LONG).show();
                    } else {
                        tvInfo.setText("Failed: " +
                                (task.getException() != null
                                        ? task.getException().getMessage() : ""));
                    }
                });
    }
}