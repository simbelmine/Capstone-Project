package com.app.eisenflow.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.eisenflow.helpers.TaskReminderHelper;

import static com.app.eisenflow.utils.Constants.TAG;

/**
 * Created on 1/28/18.
 */

public class OnBootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.v(TAG, "OnBootReceive: onReceive()");

        //Acquire a partial WakeLock.
        WakefulIntentService.acquireStaticLock(context);

        Log.v(TAG, "OnBootReceive: set evening daily alarm and weekly Sunday alarm");
        // Set the evening daily alarm and the weekly Sunday alarm.
        TaskReminderHelper taskReminderAlarm = new TaskReminderHelper();
        taskReminderAlarm.createDailyEveningTip();
        taskReminderAlarm.createWeeklyOldTasksTip();

        Log.v(TAG, "OnBootReceive: start alarm rescheduling service");
        // Start the alarm rescheduling service.
        context.startService(new Intent(context, TaskReminderService.class));

        Log.v(TAG, "OnBootReceive: unregister the Boot Broadcast receiver");
        // Unregister the broadcast after Notifications are shown.
        LocalBroadcastManager.getInstance(context).unregisterReceiver(this);
    }
}

