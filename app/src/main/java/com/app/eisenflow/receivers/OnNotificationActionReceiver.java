package com.app.eisenflow.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.utils.Constants.NOTFICATION_ACTION_DONE;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.TaskUtils.updateTaskDoneState;

/**
 * Created on 2/25/18.
 */

public class OnNotificationActionReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        long taskId = intent.getExtras().getLong(KEY_ROW_ID);
        String action = intent.getAction();
        Uri uri = buildFlavorsUri(taskId);
        Cursor cursor = context.getContentResolver().query(
                uri,
                null,
                null,
                null,
                null);
        if (cursor != null && cursor.moveToFirst()) {
            switch (action) {
                case NOTFICATION_ACTION_DONE:
                    int isDoneValue = cursor.getInt(cursor.getColumnIndex(KEY_IS_DONE));
                    boolean isDone = getBooleanState(isDoneValue);
                    updateTaskDoneState(context, cursor, !isDone);

                    closeNotification(context, (int)taskId);
                    break;
            }
        } else {
            Log.e(TAG, "Couldn't move to first. Was searching for task with id: " + taskId);
        }
    }

    private void closeNotification(Context context, int id) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.cancel(id);
    }
}
