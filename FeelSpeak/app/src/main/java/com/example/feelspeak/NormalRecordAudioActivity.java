package com.example.feelspeak;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;

public class NormalRecordAudioActivity extends AppCompatActivity {
    private SpeechRecognizer speechRecognizer;
    private Intent speechRecognizerIntent;
    private Button btnRecord;
    private TextView tvResult;
    private boolean isRecording = false;

    private static final int PERMISSION_REQUEST_CODE = 200;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_normal_record_audio);

        // Toolbar (no drawer here)
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarRecordAudio);
        setSupportActionBar(topAppBar);

        // Hide default action bar title
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Back arrow to go back
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        initSpeechRecognizer();
        btnRecord = findViewById(R.id.btnRecord);
        tvResult = findViewById(R.id.tvResult);

        btnRecord.setOnClickListener(v -> {
            if (!hasMicPermission()) {
                requestMicPermission();
                return;
            }

            if (!isRecording) {
                startRecording();
            } else {
                stopRecording();
            }
        });
    }

    private void initSpeechRecognizer() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        speechRecognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        speechRecognizerIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onResults(Bundle results) {
                ArrayList<String> matches =
                        results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && !matches.isEmpty()) {
                    tvResult.setText(matches.get(0));
                }
                isRecording = false;
                btnRecord.setText("🎤 Record");
            }

            @Override
            public void onError(int error) {
                Toast.makeText(NormalRecordAudioActivity.this,
                        "Error: " + error, Toast.LENGTH_SHORT).show();
                isRecording = false;
                btnRecord.setText("🎤 Record");
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

    private void startRecording() {
        try {
            speechRecognizer.startListening(speechRecognizerIntent);
            isRecording = true;
            btnRecord.setText("⏹️ Stop");
            tvResult.setText("Listening...");
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
                android.Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestMicPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startRecording();
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (speechRecognizer != null) {
            speechRecognizer.destroy();
        }
        super.onDestroy();
    }
}