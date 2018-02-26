package com.app.eisenflow.helpers;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.activities.MainActivity;
import com.app.eisenflow.activities.SingleTaskActivity;
import com.app.eisenflow.receivers.OnAlarmReceiver;
import com.app.eisenflow.receivers.OnNotificationActionReceiver;
import com.app.eisenflow.utils.DataUtils;

import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.utils.Constants.NOTFICATION_ACTION_DONE;
import static com.app.eisenflow.utils.Constants.NOTIFICATION_REMINDER_ACTION_CODE;
import static com.app.eisenflow.utils.Constants.NOTIFICATION_REMINDER_CHANEL;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendar;
import static com.app.eisenflow.utils.TaskUtils.generateRandomId;
import static com.app.eisenflow.utils.Utils.NEEDED_API_LEVEL;
import static com.app.eisenflow.utils.Utils.getNotificationSoundUri;

/**
 * Created on 1/28/18.
 */

public class NotificationHelper {
    public void sendNotification(Task task, Intent intent) {
        Context context = ApplicationEisenFlow.getAppContext();

        Intent taskIntent = new Intent(context, SingleTaskActivity.class);
        taskIntent.putExtras(intent);
        PendingIntent pendingIntentOpenTask = PendingIntent.getActivity(
                context,
                0,
                taskIntent,
                PendingIntent.FLAG_ONE_SHOT);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, NOTIFICATION_REMINDER_CHANEL)
                .setSmallIcon(com.app.eisenflow.R.mipmap.ic_stat_fish_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(task.getTitle())
                .setContentInfo(task.getDate() + " @ " + task.getTime())
                .setSound(getNotificationSoundUri())
                .setAutoCancel(true)
                .setLights(Color.CYAN, 500, 500)
                .setContentIntent(pendingIntentOpenTask)
                ;

        DataUtils.Priority priority = DataUtils.Priority.valueOf(task.getPriority());
        Calendar taskCalendarDate = getCalendar(task.getDate(), task.getTime());
        Calendar now = Calendar.getInstance();

        if(priority == TWO && taskCalendarDate.getTimeInMillis() < now.getTimeInMillis()) {
            notificationBuilder.addAction(getNotificationActionAddProgress(intent, (int)task.getId()));
        }
        else {
            notificationBuilder.addAction(getNotificationActionDone(intent));
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify((int)task.getId(), notificationBuilder.build());
    }

    private NotificationCompat.Action getNotificationActionAddProgress(Intent intent, int taskId) {
        Context context = ApplicationEisenFlow.getAppContext();
        Intent actionAddProgress = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, taskId)
                .setAction("ACTION_NOTIFICATION_ADD_PROGRESS");
        PendingIntent actionAddProgressPendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REMINDER_ACTION_CODE,
                actionAddProgress,
                PendingIntent.FLAG_ONE_SHOT
        );
        return new NotificationCompat.Action.Builder(
                R.mipmap.plus,
                context.getString(R.string.notification_add_progress),
                actionAddProgressPendingIntent).build();
    }

    private NotificationCompat.Action getNotificationActionDone(Intent intent) {
        Context context = ApplicationEisenFlow.getAppContext();
        Intent actionDone = new Intent(context, OnNotificationActionReceiver.class)
                .setAction(NOTFICATION_ACTION_DONE);
        actionDone.putExtras(intent);
        PendingIntent actionDonePendingIntent = PendingIntent.getBroadcast(
                context,
                NOTIFICATION_REMINDER_ACTION_CODE,
                actionDone,
                PendingIntent.FLAG_ONE_SHOT
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.check_done,
                context.getString(R.string.notification_done),
                actionDonePendingIntent).build();
    }

    public void showEveningTipNotifications() {
        Context context = ApplicationEisenFlow.getAppContext();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context, NOTIFICATION_REMINDER_CHANEL)
                .setSmallIcon(com.app.eisenflow.R.mipmap.ic_stat_fish_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.daily_evening_tip_notification))
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(context.getString(R.string.daily_evening_tip_notification)))
                .setSound(getNotificationSoundUri())
                .setAutoCancel(true)
                .setLights(Color.CYAN, 500, 500)
                .setContentIntent(getOpenAppPendingIntent())
                ;

        if(Build.VERSION.SDK_INT >= NEEDED_API_LEVEL) {
            NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
            notificationBuilder.extend(wearableExtender);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(generateRandomId(), notificationBuilder.build());
    }

    public void showWeeklyOldTaskNotification() {
        Context context = ApplicationEisenFlow.getAppContext();
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(
                context, NOTIFICATION_REMINDER_CHANEL)
                .setSmallIcon(R.mipmap.ic_stat_fish_icon)
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(context.getString(R.string.weekly_tip_notification))
                .setSound(getNotificationSoundUri())
                .setAutoCancel(true)
                .setLights(Color.CYAN, 500, 500)
                .setContentIntent(getOpenAppPendingIntent())
                ;

        if(Build.VERSION.SDK_INT >= NEEDED_API_LEVEL) {
            NotificationCompat.WearableExtender wearableExtender = new NotificationCompat.WearableExtender();
            notificationBuilder.extend(wearableExtender);
        }

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(generateRandomId(), notificationBuilder.build());
    }

    private PendingIntent getOpenAppPendingIntent() {
        Context context = ApplicationEisenFlow.getAppContext();
        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.putExtra(KEY_ROW_ID, -1);

        return PendingIntent.getActivity(
                context,
                0,
                appIntent,
                PendingIntent.FLAG_ONE_SHOT);
    }
}
