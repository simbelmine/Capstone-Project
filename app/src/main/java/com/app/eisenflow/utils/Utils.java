package com.app.eisenflow.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import static com.app.eisenflow.activities.LaunchActivity.PREF_FIRST_TIME_USER;

/**
 * Created on 12/19/17.
 */

public class Utils {
    private static final String PREFERENCES_FILE = "eisenflow_settings";

    public static void saveSharedBooleanSetting(Context context, String settingName, boolean settingValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(settingName, settingValue);
        editor.apply();
    }

    public static boolean readSharedBooleanSetting(Context context, String settingName, boolean defaultValue) {
        SharedPreferences sharedPref = context.getSharedPreferences(PREFERENCES_FILE, Context.MODE_PRIVATE);
        return sharedPref.getBoolean(settingName, defaultValue);
    }

    public static boolean isFirstTimeUser(Context context) {
        return readSharedBooleanSetting(context, PREF_FIRST_TIME_USER, true);
    }

    public static int blendColors(int from, int to, float ratio) {
        final float inverseRatio = 1.0f - ratio;

        final float r = Color.red(to) * ratio + Color.red(from) * inverseRatio;
        final float g = Color.green(to) * ratio + Color.green(from) * inverseRatio;
        final float b = Color.blue(to) * ratio + Color.blue(from) * inverseRatio;

        return Color.rgb((int) r, (int) g, (int) b);
    }
}
