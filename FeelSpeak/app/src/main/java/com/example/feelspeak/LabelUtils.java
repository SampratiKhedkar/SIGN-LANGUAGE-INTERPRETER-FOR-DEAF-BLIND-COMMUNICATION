package com.example.feelspeak;

import android.content.Context;
import android.content.res.AssetManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LabelUtils {

    /**
     * Loads a list of labels from a file in the assets directory.
     *
     * @param context   The application context to access assets.
     * @param assetPath The path to the label file within the assets folder.
     * @return A list of strings, where each string is a label.
     * @throws IOException If the file cannot be read.
     */
    public static List<String> loadLabels(Context context, String assetPath) throws IOException {
        List<String> labels = new ArrayList<>();
        AssetManager assetManager = context.getAssets();

        // Use a try-with-resources statement to automatically close the reader
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(assetManager.open(assetPath)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                labels.add(line);
            }
        }

        return labels;
    }
}