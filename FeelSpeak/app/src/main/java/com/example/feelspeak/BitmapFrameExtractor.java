package com.example.feelspeak;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
//import android.widget.VideoView;
import android.content.Context;

public class BitmapFrameExtractor {
    public interface FrameCallback {
        void onFrame(Bitmap bitmap, long timestampMs);
    }

    // Extract bitmap from video at specified timestamp (in ms)
    public static void extractFrameAt(
            Context context, Uri videoUri, long timestampMs, FrameCallback callback) {
        try {
            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
            retriever.setDataSource(context, videoUri);
            Bitmap bitmap = retriever.getFrameAtTime(timestampMs * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
            retriever.release();
            callback.onFrame(bitmap, timestampMs);
        } catch (Exception e) {
            e.printStackTrace();
            callback.onFrame(null, -1);
        }
    }
}