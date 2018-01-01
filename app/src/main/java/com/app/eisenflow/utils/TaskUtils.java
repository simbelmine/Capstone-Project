package com.app.eisenflow.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;

/**
 * Created on 12/31/17.
 */

public class TaskUtils {
    public static void deleteTask(Context context, int taskId) {
        Uri uri = buildFlavorsUri(taskId);
        context.getContentResolver().delete(uri, null, null);
    }

    public static void shareTask(Context context, Cursor cursor, int position) {
        if (cursor != null && cursor.moveToPosition(position)) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getMessageToShare(context, cursor));
            context.startActivity(Intent.createChooser(
                    sharingIntent,
                    context.getResources().getString(R.string.share_task_title)
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    private static String getMessageToShare(Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
        String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        String note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));

        return String.format(context.getResources().getString(R.string.share_task_message),
                name, date, time, note);
    }
}
