package com.example.feelspeak;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;


public class TFLiteModelLoader {
    /**
     * Load a .tflite model stored in the assets folder and return a MappedByteBuffer.
     * Usage: MappedByteBuffer mb = TFLiteModelLoader.loadModelFile(context, "isl_model.tflite");
     */
    public static MappedByteBuffer loadModelFile(Context context, String modelFilename) throws IOException {
        AssetFileDescriptor fileDescriptor = context.getAssets().openFd(modelFilename);
        FileInputStream inputStream = new FileInputStream(fileDescriptor.getFileDescriptor());
        FileChannel fileChannel = inputStream.getChannel();
        long startOffset = fileDescriptor.getStartOffset();
        long declaredLength = fileDescriptor.getDeclaredLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }
}