package com.app.eisenflow.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;

/**
 * Created on 12/31/17.
 */

public class TaskUtils {
    public static void deleteTask(Context context, int taskId) {
        Log.v("eisen", "DELETE task with Id: " + taskId);

        Uri uri = buildFlavorsUri(taskId);
        context.getContentResolver().delete(uri, null, null);
    }
}
