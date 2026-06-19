package com.example.feelspeak;
// replace with your package name

import org.tensorflow.lite.Interpreter;
import android.content.Context;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

public class ModelUtils {

    // Load model from assets
    public static MappedByteBuffer loadModelFile(Context context, String filename) throws IOException {
        FileInputStream fis = new FileInputStream(context.getAssets().openFd(filename).getFileDescriptor());
        FileChannel fileChannel = fis.getChannel();
        long startOffset = context.getAssets().openFd(filename).getStartOffset();
        long declaredLength = context.getAssets().openFd(filename).getLength();
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength);
    }

    // Create TFLite Interpreter
    public static Interpreter getInterpreter(Context context, String filename) throws IOException {
        return new Interpreter(loadModelFile(context, filename));
    }
}