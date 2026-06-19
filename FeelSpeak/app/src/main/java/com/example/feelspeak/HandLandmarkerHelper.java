package com.example.feelspeak;

import android.content.Context;

import com.google.mediapipe.tasks.core.BaseOptions;
import com.google.mediapipe.tasks.vision.core.RunningMode;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker;
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions;

public class HandLandmarkerHelper {

    public static HandLandmarker getHandLandmarker(Context context) {

        BaseOptions baseOptions =
                BaseOptions.builder()
                        .setModelAssetPath("hand_landmarker.task")
                        .build();

        HandLandmarkerOptions options =
                HandLandmarkerOptions.builder()
                        .setBaseOptions(baseOptions)
                        .setRunningMode(RunningMode.VIDEO)
                        .setNumHands(1)
                        .setMinHandDetectionConfidence(0.3f)
                        .setMinHandPresenceConfidence(0.3f)
                        .setMinTrackingConfidence(0.3f)
                        .build();

        return HandLandmarker.createFromOptions(context, options);
    }
}
