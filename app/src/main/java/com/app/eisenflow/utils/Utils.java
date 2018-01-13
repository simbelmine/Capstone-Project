package com.app.eisenflow.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;

import static com.app.eisenflow.activities.LaunchActivity.PREF_FIRST_TIME_USER;

/**
 * Created on 12/19/17.
 */

public class Utils {
    private static final String PREFERENCES_FILE = "eisenflow_settings";
    public static final int NEEDED_API_LEVEL = 22;

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

    public static void showAlertMessage(View parentView, String messageToShow, int colorMsg) {
        if(Build.VERSION.SDK_INT >= NEEDED_API_LEVEL) {
            showAlertSnackbar(parentView, messageToShow, colorMsg);
        }
        else {
            showAlertDialog(messageToShow, colorMsg);
        }
    }
    private static void showAlertSnackbar(View parentView, String messageToShow, int colorMsg) {
        Snackbar snackbar = Snackbar.make(parentView, messageToShow, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.WHITE)
                .setAction(ApplicationEisenFlow.getAppContext().getString(android.R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

        View snackbarView = snackbar.getView();
        TextView text = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        text.setTextColor(ApplicationEisenFlow.getAppContext().getResources().getColor(colorMsg));
        snackbar.show();
    }

    private static void showAlertDialog(String messageToShow, int colorMsg) {
        int theme;
        if(colorMsg == R.color.date || colorMsg == R.color.white) {
            theme = R.style.MyTipDialogStyle;
        }
        else {
            theme =  R.style.MyAlertDialogStyle;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(ApplicationEisenFlow.getAppContext(), theme);
        if(colorMsg != R.color.date && colorMsg != R.color.white) {
            builder.setTitle(ApplicationEisenFlow.getAppContext().getResources().getString(R.string.add_task_alert_title));
        }
        builder.setMessage(messageToShow);
        builder.setPositiveButton(ApplicationEisenFlow.getAppContext().getResources().getString(android.R.string.ok), null);
        builder.show();
    }

    public static float convertDpToPixel(Context context, int dp){
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float)metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    public static void hideKeyboard(Activity activity) {
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    public static boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) ApplicationEisenFlow.getAppContext().
                getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
