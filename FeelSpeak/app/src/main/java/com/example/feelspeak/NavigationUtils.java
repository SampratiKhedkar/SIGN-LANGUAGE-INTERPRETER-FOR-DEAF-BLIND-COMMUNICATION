package com.example.feelspeak;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class NavigationUtils {

    // One prefs file for the whole app
    private static final String PREFS_NAME = "FeelSpeakPrefs";

    // Keys used everywhere
    public static final String KEY_USER_TYPE   = "user_type";
    public static final String KEY_REMEMBER_ME = "remember_me";

    // Other keys you already use (optional, add as needed)
    public static final String KEY_USER_NAME   = "user_name";
    public static final String KEY_PERMISSIONS_GRANTED = "permissions_granted";

    // Get shared prefs
    public static SharedPreferences getPrefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // Convenience: read current user type (default NORMAL)
    public static String getUserType(Context context) {
        SharedPreferences prefs = getPrefs(context);
        return prefs.getString(KEY_USER_TYPE, "NORMAL");
    }

    // Convenience: set current user type
    public static void setUserType(Context context, String type) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit().putString(KEY_USER_TYPE, type).apply();
    }

    // Open the correct Home screen based on saved user type
    public static void goToCurrentHome(Context context) {
        String userType = getUserType(context);

        Intent intent;
        if ("BLIND".equalsIgnoreCase(userType)) {
            intent = new Intent(context, BlindHomeActivity.class);
        } else if ("DEAF".equalsIgnoreCase(userType)) {
            intent = new Intent(context, DeafHomeActivity.class);
        } else {
            intent = new Intent(context, NormalHomeActivity.class);
        }

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // Optional: helper to clear login info on logout
    public static void clearLoginAndUserType(Context context) {
        SharedPreferences prefs = getPrefs(context);
        prefs.edit()
                .remove(KEY_REMEMBER_ME)
                .remove(KEY_USER_TYPE)
                .apply();
    }
}