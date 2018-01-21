package com.app.eisenflow.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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

    public static Object createAlertMessage(Activity activity, View parentView, String messageToShow, int colorMsg) {
        if(Build.VERSION.SDK_INT >= NEEDED_API_LEVEL) {
            return showAlertSnackbar(activity, parentView, messageToShow, colorMsg);
        }
        else {
            return showAlertDialog(activity, messageToShow, colorMsg);
        }
    }

    private static Snackbar showAlertSnackbar(Activity activity, View parentView, String messageToShow, int colorMsg) {
        Snackbar snackbar = Snackbar.make(parentView, messageToShow, Snackbar.LENGTH_INDEFINITE)
                .setActionTextColor(Color.WHITE)
                .setAction(activity.getString(android.R.string.ok), new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                    }
                });

        View snackbarView = snackbar.getView();
        TextView text = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
        text.setTextColor(activity.getResources().getColor(colorMsg));
        return snackbar;
    }

    private static AlertDialog.Builder showAlertDialog(Activity activity, String messageToShow, int colorMsg) {
        int theme;
        if(colorMsg == R.color.date || colorMsg == R.color.white) {
            theme = R.style.MyTipDialogStyle;
        }
        else {
            theme =  R.style.MyAlertDialogStyle;
        }

        AlertDialog.Builder builder =
                new AlertDialog.Builder(activity, theme);
        if(colorMsg != R.color.date && colorMsg != R.color.white) {
            builder.setTitle(activity.getResources().getString(R.string.add_task_alert_title));
        }
        builder.setMessage(messageToShow);
        builder.setPositiveButton(activity.getResources().getString(android.R.string.ok), null);
        builder.setCancelable(true);
        return builder;
    }

    public static void showAlertMessage(Object alertMessage) {
        if (alertMessage != null) {
            if (alertMessage instanceof Snackbar) {
                ((Snackbar)alertMessage).show();
            }
            if (alertMessage instanceof AlertDialog.Builder) {
                ((AlertDialog.Builder)alertMessage).show();
            }
        }
    }

    public static void hideAlertMessage(Object alertMessage) {
        if (alertMessage != null) {
            if (alertMessage instanceof Snackbar) {
                ((Snackbar)alertMessage).dismiss();
            }
        }
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

    public static Uri getNotificationCompletedSoundUri() {
        return Uri.parse("android.resource://" +
                ApplicationEisenFlow.getAppContext().getPackageName() +
                "/" + com.app.eisenflow.R.raw.task_complete);
    }

    public static void forceEditTextToLooseFocus(Activity activity, MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            View v = activity.getCurrentFocus();
            if ( v instanceof EditText) {
                Rect outRect = new Rect();
                v.getGlobalVisibleRect(outRect);
                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
                    v.clearFocus();
                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                }
            }
        }
    }
}
