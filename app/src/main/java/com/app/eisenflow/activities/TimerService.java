package com.app.eisenflow.activities;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.eisenflow.R;

import java.util.concurrent.TimeUnit;

import static com.app.eisenflow.activities.MainActivity.TAG;
import static com.app.eisenflow.utils.DateTimeUtils.getCorrectTimerTimeValue;

/**
 * Created on 1/4/18.
 */

public class TimerService extends Service {
    public NotificationCompat.Builder notificationBuilder;

    private static final int NOTIFICATION_ID = 101;
    private static final int NOTIFICATION_RESULT_CODE = 0;
    private static final int NOTIFICATION_ACTION_CODE = 301;
    public static final String ACTION_PLAY = "onPlayAction";
    public static final String ACTION_PAUSE = "onPauseAction";
    public static final String ACTION_NOTIFICATION_PLAY = "onNotificationPlayAction";
    public static final String ACTION_NOTIFICATION_PAUSE = "onNotificationPauseAction";
    public static final String ACTION_TICK = "onTickAction";
    public static final String ACTION_FINISHED = "onFinishedAction";
    public static final String ACTION_ACTIVITY_TO_BACKGROUND = "onActivityToBackground";
    public static final String ACTION_ACTIVITY_TO_FOREGROUND = "onActivityToForeground";
    public static final String START_TIME = "startTime";
    public static final String TOTAL_TIME = "totalTime";
    private static final String NOTIFICATION_CHANEL = "M_CH_ID";
    public static final String LEFT_TIME_MILLIS = "LeftTimeMillis";
    private static final long COUNTDOWN_INTERVAL = 50;
    private static final String DEFAULT_TIME_PASSED = "00h:00m";
    private static final int MIN_PROGRESS = 1;
    private static final int MAX_PROGRESS = 100;

    // Service binder
    private final IBinder serviceBinder = new RunServiceBinder();

    private CountDownTimer mCountDownTimer;
    private boolean isActivityToBackground;
    private boolean isPlaying;
    private long mTotalTimeInMilliseconds = 0;
    private long mPreviousMinute = 0;
    private long mTimeLeft = 0;
    private long mPreviousSecond = 0;

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
        background();
        notificationBuilder = createNotification();
        registerReceivers();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Starting service");
        }

        if (intent != null) {
            Log.v(TAG, "onStartCommand: " + intent.getAction());
            String action = intent.getAction();
            if (ACTION_NOTIFICATION_PLAY.equals(action)) {
                play(mTimeLeft);
                notificationBuilder.mActions.clear();
                notificationBuilder.addAction(getNotificationActionPause());
                foreground();
            } else if (ACTION_NOTIFICATION_PAUSE.equals(action)) {
                pause();
                notificationBuilder.mActions.clear();
                notificationBuilder.addAction(getNotificationActionPlay());
                foreground();
            }
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
        mCountDownTimer.cancel();
        stopSelf();
        cancelNotification();
        unregisterReceivers();
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

    public void updateNotification(int progress, String elapsedTime) {
        notificationBuilder.setContentText(getString(R.string.timer_notification_content, elapsedTime));
        notificationBuilder.setProgress((int)mTotalTimeInMilliseconds, progress, false);
        foreground();
    }

    public void cancelNotification() {
        Log.e(TAG, "Cancel service and notification");
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
    }

    /**
     * Creates a notification for placing the service into the foreground
     *
     * @return a notification for interacting with the service when in the foreground
     */
    private NotificationCompat.Builder createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANEL)
                .setContentText(getString(R.string.timer_notification_content, DEFAULT_TIME_PASSED))
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(true);

        builder.addAction(getNotificationActionPause());

        Intent notificationIntent = new Intent(this, TimerActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        PendingIntent intent = PendingIntent.getActivity(
                this,
                NOTIFICATION_RESULT_CODE,
                notificationIntent,
                0);
        builder.setContentIntent(intent);
        builder.setProgress(MAX_PROGRESS, MIN_PROGRESS, false);

        return builder;
    }

    private NotificationCompat.Action getNotificationActionPlay() {
        Intent actionPlay = new Intent(this, TimerService.class);
        actionPlay.setAction(ACTION_NOTIFICATION_PLAY);
        actionPlay.putExtra("isPlaying", true);
        PendingIntent actionPlayPendingIntent = PendingIntent.getService(
                this, NOTIFICATION_ACTION_CODE,
                actionPlay,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.play,
                "Play",
                actionPlayPendingIntent).build();
    }

    private NotificationCompat.Action getNotificationActionPause() {
        Intent actionPause = new Intent(this, TimerService.class);
        actionPause.setAction(ACTION_NOTIFICATION_PAUSE);
        actionPause.putExtra("isPlaying", false);
        PendingIntent actionPausePendingIntent = PendingIntent.getService(
                this,
                NOTIFICATION_ACTION_CODE,
                actionPause,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.pause,
                "Pause",
                actionPausePendingIntent).build();
    }

    private void initCountDownTimer(long startTime) {
        mCountDownTimer = new CountDownTimer(startTime, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                // Now Timer is Running.
                mTimeLeft = leftTimeInMilliseconds;

                long calculatedTimeLeft = mTotalTimeInMilliseconds - leftTimeInMilliseconds;
                long hours = TimeUnit.MILLISECONDS.toHours(calculatedTimeLeft);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(calculatedTimeLeft) - TimeUnit.HOURS.toMinutes(hours);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(calculatedTimeLeft) - TimeUnit.MINUTES.toSeconds(minutes);
                long progress = (mTotalTimeInMilliseconds - leftTimeInMilliseconds);

                if (!isActivityToBackground && seconds != mPreviousSecond) {
                    Log.v(TAG, "Send Broadcast: Tick to Activity");
                    sendBroadcastTick(leftTimeInMilliseconds);
                }

                // Check if TimerService is Running, if yes update the Notification.
                // If TimerService is Running it means the app is in the background.
                if (isActivityToBackground && mPreviousMinute != minutes) {
                    Log.e(TAG, "Send Broadcast: Tick to Notification");
                    StringBuilder sb = new StringBuilder();
                    sb.append(getCorrectTimerTimeValue(hours) + "h:");
                    sb.append(getCorrectTimerTimeValue(minutes) + "m");
                    updateNotification((int)progress, sb.toString());
                    mPreviousMinute = minutes;
                }
                mPreviousSecond = seconds;
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "Send Broadcast: Finished");
                sendBroadcastFinished();
            }
        };
    }

    private void startCountDownTimer() {
        if (mCountDownTimer != null) {
            mCountDownTimer.start();
        }
    }

    /**
     * Broadcast Receiver Methods
     */
    private void sendBroadcastTick(long leftTimeMillis) {
        Intent intentTick = new Intent(ACTION_TICK);
        intentTick.putExtra(LEFT_TIME_MILLIS, leftTimeMillis);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentTick);
    }

    private void sendBroadcastFinished() {
        Intent intentFinished = new Intent(ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentFinished);
    }

    private BroadcastReceiver onPlay = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Service Received Action: Play");
            long totalTime = intent.getLongExtra(TOTAL_TIME, 0);
            mTotalTimeInMilliseconds = totalTime;
            mTimeLeft = mTotalTimeInMilliseconds;

            long startTime = intent.getLongExtra(START_TIME, 0);
            play(startTime);
        }
    };

    private BroadcastReceiver onPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Service Received Action: Pause");
            pause();
        }
    };

    private BroadcastReceiver onActivityToBackground = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Service Received Action: ActivityToBackground");
            // Send the service to the foreground.
            foreground();
            isActivityToBackground = true;
        }
    };

    private BroadcastReceiver onActivityToForeground = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Service Received Action: ActivityToForeground");
            // Send the service to the background.
            background();
            isActivityToBackground = false;
        }
    };

    private void registerReceivers() {
        IntentFilter intentFilterPlay = new IntentFilter(ACTION_PLAY);
        LocalBroadcastManager.getInstance(this).registerReceiver(onPlay, intentFilterPlay);

        IntentFilter intentFilterPause = new IntentFilter(ACTION_PAUSE);
        LocalBroadcastManager.getInstance(this).registerReceiver(onPause, intentFilterPause);

        IntentFilter intentFilterActivityToBackground = new IntentFilter(ACTION_ACTIVITY_TO_BACKGROUND);
        LocalBroadcastManager.getInstance(this).registerReceiver(onActivityToBackground, intentFilterActivityToBackground);

        IntentFilter intentFilterActivityToForeground = new IntentFilter(ACTION_ACTIVITY_TO_FOREGROUND);
        LocalBroadcastManager.getInstance(this).registerReceiver(onActivityToForeground, intentFilterActivityToForeground);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPlay);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onPause);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onActivityToBackground);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onActivityToForeground);
    }

    private void play(long startTime) {
        isPlaying = true;
        initCountDownTimer(startTime);
        startCountDownTimer();
    }

    private void pause() {
        isPlaying = false;
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
