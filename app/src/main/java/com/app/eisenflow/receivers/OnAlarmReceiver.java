package com.app.eisenflow.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.eisenflow.Task;
import com.app.eisenflow.helpers.NotificationHelper;
import com.app.eisenflow.helpers.TaskReminderHelper;
import com.app.eisenflow.services.WakefulIntentService;

import java.util.Calendar;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.Constants.DAILY_TIP;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.utils.Constants.WEEKLY_OLD_TASKS_TIP;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendar;

/**
 * Created on 1/28/18.
 */

public class OnAlarmReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "OnAlarmReceiver: onReceive()");

        //Acquire a partial WakeLock.
        WakefulIntentService.acquireStaticLock(context);

        Log.v(TAG, "OnAlarmReceiver: send needed notifications");
        //Send notification, bundle intent with taskID
        NotificationHelper notification = new NotificationHelper();

        long taskId = intent.getExtras().getLong(KEY_ROW_ID);
        boolean isWeeklyTip = intent.getBooleanExtra(WEEKLY_OLD_TASKS_TIP, false);
        boolean isDailyTip = intent.getBooleanExtra(DAILY_TIP, false);

        if(taskId > 0) {
            Log.v(TAG, "OnAlarmReceiver: Show Reminder Notifications (Single and Repeating)");
            Uri uri = buildFlavorsUri(taskId);
            Cursor cursor = context.getContentResolver().query(
                    uri,
                    null,
                    null,
                    null,
                    null);
            if (cursor.moveToFirst()) {
                Task task = cursorToTask(cursor);
                notification.sendNotification(task, intent);
            }
        }
        else {
            if(isWeeklyTip) {
                Log.v(TAG, "OnAlarmReceiver: Show Weekly Tip Notification");
                new StartCheckingForOldTasks(notification).execute();
            }
            else if(isDailyTip) {
                Log.v(TAG, "OnAlarmReceiver: Show Daily Tip Notification");
                notification.showEveningTipNotifications();
            }
        }

        Log.v(TAG, "OnAlarmReceiver: unregister OnAlarm Broadcast Receiver");
        // Unregister the broadcast after Notifications are shown.
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }

    private class StartCheckingForOldTasks extends AsyncTask<Void, Void, Boolean> {
        private NotificationHelper notificationHelper;
        public StartCheckingForOldTasks(NotificationHelper notificationHelper) {
            this.notificationHelper = notificationHelper;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            Cursor cursor = getCursor();
            cursor.moveToFirst();

            boolean oldTaskExists = false;
            while(cursor != null && cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
                Calendar calDate = getCalendar(date, time);
                Calendar now = Calendar.getInstance();

                if(calDate.before(now)){
                    oldTaskExists = true;
                    return oldTaskExists;
                }
            }
            return oldTaskExists;
        }

        @Override
        protected void onPostExecute(Boolean oldTaskExists) {
            if (oldTaskExists) {
                notificationHelper.showWeeklyOldTaskNotification();
            } else {
                TaskReminderHelper.setWeeklyTipAlarms();
            }
        }
    }
}
