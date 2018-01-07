package com.app.eisenflow.activities;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.app.eisenflow.R;

import static com.app.eisenflow.activities.MainActivity.TAG;

/**
 * Created on 1/4/18.
 */

public class TimerService extends Service {
    public NotificationCompat.Builder notificationBuilder;

    // Foreground notification id
    private static final int NOTIFICATION_ID = 1;

    // Service binder
    private final IBinder serviceBinder = new RunServiceBinder();

    public class RunServiceBinder extends Binder {
        TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Creating service");
        }
        notificationBuilder = createNotification();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting service");
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Binding service");
        }
        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Destroying service");
        }
    }

    /**
     * Place the service into the foreground
     */
    public void foreground() {
        startForeground(NOTIFICATION_ID, notificationBuilder.build());
    }

    /**
     * Return the service to the background
     */
    public void background() {
        stopForeground(true);
    }

    public void updateNotification(String elapsedTime) {
        notificationBuilder.setContentTitle(elapsedTime);
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */
    private NotificationCompat.Builder createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "M_CH_ID")
                .setContentTitle("Timer Active")
                .setContentText("Tap to return to the timer")
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent resultIntent = new Intent(this, TimerActivity.class);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent,
                        0);
        builder.setContentIntent(resultPendingIntent);

        return builder;
    }
}
