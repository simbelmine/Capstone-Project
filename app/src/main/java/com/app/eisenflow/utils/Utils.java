package com.app.eisenflow.utils;

import android.content.Context;
import android.os.Build;

/**
 * General util class.
 *
 * Created on 12/16/17.
 */

public class Utils {

    /**
     * Calculates the height of the status bar
     * for Android Lollipop and above.
     *
     * @return status bar height.
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = context.getResources().getDimensionPixelSize(resourceId);
            }
        }
        return result;
    }
}