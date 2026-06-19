package com.example.feelspeak;

import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.material.appbar.MaterialToolbar;

import org.tensorflow.lite.Interpreter;
import org.tensorflow.lite.Tensor;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

// OpenCV init
import org.opencv.android.OpenCVLoader;

public class VideoPreviewActivity extends AppCompatActivity {

    // Static block to load OpenCV
    static {
        if (!OpenCVLoader.initDebug()) {
            android.util.Log.e("OpenCV", "OpenCV init failed");
        } else {
            android.util.Log.d("OpenCV", "OpenCV init success");
        }
    }

    private VideoView videoView;
    private Button btnPredict;
    private TextView txtResult;
    private View layoutAudioAnimations;
    private ImageView imgSoundMic, imgAudioWaves;

    private Interpreter tflite = null;
    private List<String> labels = null;
    private Uri videoUri;
    private TextToSpeech tts;
    private String lastPredictionSentence = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_preview);

        // Toolbar
        MaterialToolbar topAppBar = findViewById(R.id.topAppBarVideoPreview);
        setSupportActionBar(topAppBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        topAppBar.setNavigationOnClickListener(v -> onBackPressed());

        // Views
        videoView = findViewById(R.id.videoView);
        btnPredict = findViewById(R.id.btnPredict);
        txtResult = findViewById(R.id.txtResult);
        layoutAudioAnimations = findViewById(R.id.layoutAudioAnimations);
        imgSoundMic = findViewById(R.id.imgSoundMic);
        imgAudioWaves = findViewById(R.id.imgAudioWaves);

        layoutAudioAnimations.setVisibility(View.GONE);

        // TextToSpeech
        tts = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int res = tts.setLanguage(Locale.US);
                if (res == TextToSpeech.LANG_MISSING_DATA
                        || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                    txtResult.setText("TTS language not supported");
                } else {
                    tts.setPitch(1.0f);
                    tts.setSpeechRate(0.75f);
                }
            } else {
                txtResult.setText("TTS init failed");
            }
        });

        // Get video URI
        String videoUriStr = getIntent().getStringExtra("VIDEO_URI");
        if (videoUriStr == null) {
            txtResult.setText("No video URI received!");
            return;
        }
        videoUri = Uri.parse(videoUriStr);

        // VideoView setup
        videoView.setMediaController(null);
        videoView.setVideoURI(videoUri);
        videoView.setOnPreparedListener(mp -> {
            mp.setLooping(false);
            videoView.start();
        });
        videoView.setOnCompletionListener(mp -> {
            // stop at last frame
        });
        videoView.setOnClickListener(v -> {
            videoView.setVideoURI(videoUri);
            videoView.start();
        });

        // Load TFLite model and labels
        try {
            java.nio.MappedByteBuffer modelBuffer =
                    TFLiteModelLoader.loadModelFile(this, "sign_language_lstm_model2_sol.tflite");

            Interpreter.Options options = new Interpreter.Options();
            options.setNumThreads(4);
            tflite = new Interpreter(modelBuffer, options);

            labels = LabelUtils.loadLabels(this, "label_encoder2_sol.txt");

            android.util.Log.d("ModelDebug",
                    "Loaded model: sign_language_lstm_model2_sol.tflite");
            android.util.Log.d("ModelDebug",
                    "Loaded labels: label_encoder2_sol.txt, size = " +
                            (labels == null ? -1 : labels.size()));

        } catch (IOException e) {
            e.printStackTrace();
            txtResult.setText("Failed to load model/labels.");
        }

        // Predict button
        btnPredict.setOnClickListener(v -> runPredictionInBackground());

        // Tap on text to re-speak
        txtResult.setOnClickListener(v -> {
            if (lastPredictionSentence != null && tts != null) {
                startAudioAnimations();
                speakPrediction(lastPredictionSentence);
            }
        });
    }

    private void runPredictionInBackground() {
        layoutAudioAnimations.setVisibility(View.GONE);

        new Thread(() -> {
            final String sentence = predictVideoInternal();

            runOnUiThread(() -> {
                if (sentence != null && !sentence.isEmpty()) {
                    lastPredictionSentence = sentence;
                    txtResult.setText(sentence);
                    startAudioAnimations();
                    speakPrediction(sentence);
                } else {
                    txtResult.setText("Prediction failed!");
                    stopAudioAnimations();
                }
            });
        }).start();
    }

    private String predictVideoInternal() {
        if (tflite == null) {
            return "Model not loaded!";
        }

        float[][] frames = KeypointExtractor.extractFromVideo(this, videoUri);
        if (frames == null || frames.length == 0) {
            return "No keypoints extracted!";
        }

        android.util.Log.d("VideoPreview",
                "Frames shape = " + frames.length + " x " + frames[0].length);

        List<float[]> frameList = Arrays.asList(frames);
        float[][][] modelInput = KeypointExtractor.makeModelInput(frameList);
        if (modelInput == null || modelInput.length == 0) {
            return "Model input is empty!";
        }

        tflite.allocateTensors();
        Tensor inputTensor = tflite.getInputTensor(0);
        Tensor outTensor = tflite.getOutputTensor(0);

        int[] inShape = inputTensor.shape();   // expect [1, 60, 126]
        int[] outShape = outTensor.shape();    // expect [1, N]

        android.util.Log.d("TFLiteShape",
                "Input shape = " + Arrays.toString(inShape) +
                        " Output shape = " + Arrays.toString(outShape));

        android.util.Log.d("TFLiteShape",
                "labels.size() = " + (labels == null ? -1 : labels.size()));

        int numLabels = outShape[outShape.length - 1];
        float[][] output = new float[1][numLabels];

        try {
            tflite.run(modelInput, output);

            int maxIndex = 0;
            float maxProb = output[0][0];
            for (int i = 1; i < numLabels; i++) {
                if (output[0][i] > maxProb) {
                    maxProb = output[0][i];
                    maxIndex = i;
                }
            }

            String predictedGesture = (labels != null && labels.size() > maxIndex)
                    ? labels.get(maxIndex)
                    : String.valueOf(maxIndex);

            return " ' " + predictedGesture + " ' ";
        } catch (Exception e) {
            e.printStackTrace();
            return "Prediction error!";
        }
    }

    private void startAudioAnimations() {
        layoutAudioAnimations.setVisibility(View.VISIBLE);

        Glide.with(this)
                .asGif()
                .load(R.drawable.sound_mic)
                .into(imgSoundMic);

        Glide.with(this)
                .asGif()
                .load(R.drawable.audio_waves)
                .into(imgAudioWaves);
    }

    private void stopAudioAnimations() {
        layoutAudioAnimations.setVisibility(View.GONE);

        Glide.with(this).clear(imgSoundMic);
        Glide.with(this).clear(imgAudioWaves);
        imgSoundMic.setImageDrawable(null);
        imgAudioWaves.setImageDrawable(null);
    }

    private void speakPrediction(String sentence) {
        if (tts == null) return;

        startAudioAnimations();
        tts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null, "PREDICTION_UTTERANCE");

        int approxMs = Math.max(1500, sentence.length() * 80);
        txtResult.postDelayed(this::stopAudioAnimations, approxMs);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (tts != null && tts.isSpeaking()) {
            tts.stop();
        }
        stopAudioAnimations();
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}