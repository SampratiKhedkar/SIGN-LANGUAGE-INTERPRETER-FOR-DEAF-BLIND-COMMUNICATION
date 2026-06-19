package com.example.feelspeak;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;

import com.google.mediapipe.framework.image.BitmapImageBuilder;
import com.google.mediapipe.framework.image.MPImage;
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark;
import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class KeypointExtractor {

    private static final String TAG = "KeypointExtractor";
    private static final int MAX_FRAMES = 60;
    private static final int FEATURES = 126;
    private static final int LANDMARKS_PER_HAND = 21;

    // Simple helper; if you already have a better one, you can replace this.
    private static String getRealPathFromUri(Context context, Uri uri) {
        return uri.getPath();
    }

    public static float[][] extractFromVideo(Context context, Uri videoUri) {
        float[][] output = new float[MAX_FRAMES][FEATURES];
        List<float[]> buffer = new ArrayList<>();
        int framesWithHandsCount = 0;

        HandLandmarker.HandLandmarkerOptions options =
                HandLandmarker.HandLandmarkerOptions.builder()
                        .setBaseOptions(BaseOptions.builder()
                                .setModelAssetPath("hand_landmarker.task")
                                .build())
                        .setRunningMode(RunningMode.VIDEO)
                        .setMinHandDetectionConfidence(0.3f)
                        .setNumHands(2)
                        .build();

        HandLandmarker handLandmarker = null;
        VideoCapture cap = null;

        try {
            handLandmarker = HandLandmarker.createFromOptions(context, options);

            // 1) Open video with OpenCV
            String videoPath = copyUriToTempFile(context, videoUri);
            Log.d(TAG, "Video path (OpenCV temp) = " + videoPath);

            if (videoPath == null) {
                Log.e(TAG, "Failed to copy Uri to temp file");
                return output; // all zeros
            }

            cap = new VideoCapture(videoPath);
            if (!cap.isOpened()) {
                Log.e(TAG, "Cannot open video with OpenCV: " + videoPath);
                return output; // all zeros
            }

            int frameCount = (int) cap.get(Videoio.CAP_PROP_FRAME_COUNT);
            double fps = cap.get(Videoio.CAP_PROP_FPS);
            Log.d(TAG, "OpenCV frameCount=" + frameCount + " fps=" + fps);

            int targetSteps = MAX_FRAMES;
            List<Mat> sampledFrames = new ArrayList<>();

            // 2) Sample frames like Python (uniform indices)
            if (frameCount <= 0) {
                Mat frame = new Mat();
                int readCount = 0;
                while (readCount < targetSteps && cap.read(frame)) {
                    if (!frame.empty()) {
                        sampledFrames.add(frame.clone());
                        readCount++;
                    }
                }
            } else {
                for (int i = 0; i < targetSteps; i++) {
                    int idx = (int) Math.round(
                            (i / (double) (targetSteps - 1)) * (frameCount - 1));
                    cap.set(Videoio.CAP_PROP_POS_FRAMES, idx);
                    Mat frame = new Mat();
                    boolean ok = cap.read(frame);
                    if (!ok || frame.empty()) {
                        Log.w(TAG, "Empty frame at idx " + idx);
                        continue;
                    }
                    sampledFrames.add(frame.clone());
                }
            }

            if (cap != null) {
                cap.release();
            }

            Log.d(TAG, "Sampled frames count = " + sampledFrames.size());

            // 3) Convert Mat -> Bitmap and run HandLandmarker
            long stepMs = 33; // pseudo timestamp for RunningMode.VIDEO
            List<Bitmap> framesBitmap = new ArrayList<>();

            for (Mat m : sampledFrames) {
                Imgproc.cvtColor(m, m, Imgproc.COLOR_BGR2RGBA);
                Bitmap bmp = Bitmap.createBitmap(m.cols(), m.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(m, bmp);

                // If you still need rotation fixes, apply rotateBitmap here, but usually not needed
                framesBitmap.add(bmp);
            }

            for (int i = 0; i < framesBitmap.size(); i++) {
                MPImage mpImage = new BitmapImageBuilder(framesBitmap.get(i)).build();
                long timestampMs = i * stepMs;
                HandLandmarkerResult result =
                        handLandmarker.detectForVideo(mpImage, timestampMs);

                if (result != null && result.landmarks() != null && !result.landmarks().isEmpty()) {
                    framesWithHandsCount++;
                }

                buffer.add(extractHandFeatures(result));
            }

            Log.d("ValueCheck", "ANDROID DETECTION RESULT: Found hands in "
                    + framesWithHandsCount + " / " + framesBitmap.size() + " frames");

        } catch (Exception e) {
            Log.e(TAG, "Extraction failed", e);
        } finally {
            if (cap != null) {
                try {
                    cap.release();
                } catch (Exception ignored) {}
            }
            if (handLandmarker != null) {
                handLandmarker.close();
            }
        }

        // 4) Align frames to 60
        List<float[]> frames60 = sampleAndPadFrames(buffer, MAX_FRAMES);
        for (int i = 0; i < MAX_FRAMES; i++) {
            output[i] = frames60.get(i);
        }

        // 5) Debug logs
        logMiddleFrame(output[30]);

        saveFeaturesToCSV(context, output, "android_features.csv");
        return output;
    }

    private static String copyUriToTempFile(Context context, Uri uri) {
        File tempFile = null;
        try {
            tempFile = File.createTempFile("video_input_", ".mp4", context.getCacheDir());
            try (java.io.InputStream in = context.getContentResolver().openInputStream(uri);
                 java.io.OutputStream out = new java.io.FileOutputStream(tempFile)) {

                if (in == null) return null;
                byte[] buffer = new byte[8192];
                int len;
                while ((len = in.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                }
                out.flush();
                return tempFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "copyUriToTempFile failed", e);
            if (tempFile != null) {
                // cleanup
                //noinspection ResultOfMethodCallIgnored
                tempFile.delete();
            }
            return null;
        }
    }

    public static float[][][] makeModelInput(List<float[]> frames) {
        int numFrames = frames.size();
        int numFeatures = (numFrames > 0) ? frames.get(0).length : FEATURES;
        float[][][] input = new float[1][numFrames][numFeatures];
        for (int i = 0; i < numFrames; i++) {
            System.arraycopy(frames.get(i), 0, input[0][i], 0, numFeatures);
        }
        return input;
    }

    private static Bitmap rotateBitmap(Bitmap source, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(
                source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
    }

    private static void logMiddleFrame(float[] frame) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(String.format("%.6f", frame[i])).append(", ");
        }
        Log.d("ValueCheck", "ANDROID MIDDLE FRAME (30) first 10 values: " + sb.toString());
    }

    private static float[] extractHandFeatures(HandLandmarkerResult result) {
        float[] features = new float[FEATURES];
        if (result == null || result.landmarks() == null || result.landmarks().isEmpty()) {
            return features;
        }

        float[] lh = new float[63];
        float[] rh = new float[63];

        for (int handIndex = 0; handIndex < result.landmarks().size(); handIndex++) {
            String label = result.handedness().get(handIndex).get(0).categoryName();
            float[] target = "Left".equals(label) ? lh : rh;

            List<NormalizedLandmark> landmarks = result.landmarks().get(handIndex);
            for (int i = 0; i < LANDMARKS_PER_HAND && i < landmarks.size(); i++) {
                target[i * 3] = landmarks.get(i).x();
                target[i * 3 + 1] = landmarks.get(i).y();
                target[i * 3 + 2] = landmarks.get(i).z();
            }
        }

        System.arraycopy(lh, 0, features, 0, 63);
        System.arraycopy(rh, 0, features, 63, 63);
        return features;
    }

    public static List<float[]> sampleAndPadFrames(List<float[]> buffer, int numFrames) {
        List<float[]> output = new ArrayList<>();
        int size = buffer.size();
        if (size == 0) {
            for (int i = 0; i < numFrames; i++) output.add(new float[FEATURES]);
            return output;
        }
        for (int i = 0; i < numFrames; i++) {
            int idx = (size >= numFrames)
                    ? (int) Math.round(i * (size - 1) / (double) (numFrames - 1))
                    : Math.min(i, size - 1);
            output.add(buffer.get(idx).clone());
        }
        return output;
    }

    public static void saveFeaturesToCSV(Context context, float[][] features, String filename) {
        try {
            File file = new File(context.getExternalFilesDir(null), filename);
            FileWriter writer = new FileWriter(file);
            for (float[] frame : features) {
                for (int i = 0; i < frame.length; i++) {
                    writer.append(String.valueOf(frame[i]));
                    if (i < frame.length - 1) writer.append(",");
                }
                writer.append("\n");
            }
            writer.close();
        } catch (IOException ignored) {}
    }
}