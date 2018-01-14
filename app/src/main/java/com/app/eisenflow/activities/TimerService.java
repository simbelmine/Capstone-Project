package com.app.eisenflow.activities;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.app.eisenflow.R;
import com.app.eisenflow.utils.Utils;

import java.util.concurrent.TimeUnit;

import static com.app.eisenflow.activities.MainActivity.TAG;
import static com.app.eisenflow.utils.DateTimeUtils.getCorrectTimerTimeValue;

/**
 * Created on 1/4/18.
 */

public class TimerService extends Service {
    private static final int NOTIFICATION_ID = 101;
    private static final int NOTIFICATION_RESULT_CODE = 0;
    private static final int NOTIFICATION_ACTION_CODE = 301;
    public static final String ACTION_PLAY = "onPlayAction";
    public static final String ACTION_PAUSE = "onPauseAction";
    public static final String ACTION_NOTIFICATION_PLAY = "onNotificationPlayAction";
    public static final String ACTION_NOTIFICATION_PAUSE = "onNotificationPauseAction";
    public static final String ACTION_NOTIFICATION_DISMISS = "onNotificationDismissAction";
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

    public NotificationCompat.Builder mNotificationBuilder;
    private CountDownTimer mCountDownTimer;
    private boolean isActivityToBackground;
    private long mTotalTimeInMilliseconds = 0;
    private long mPreviousMinute = 0;
    private long mTimeLeft = 0;
    private long mPreviousSecond = 0;
    private long mCurrentHour = 0;
    private long mCurrentMinutes = 0;
    private long mCurrentSeconds = 0;

    @Override
    public void onCreate() {
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Creating service");
        }
        background();
        mNotificationBuilder = createNotification();
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
                setPauseAction();
            } else if (ACTION_NOTIFICATION_PAUSE.equals(action)) {
                pause();
                setPlayAction();
            } else if (ACTION_NOTIFICATION_DISMISS.equals(action)) {
                stopSelf();
            }
        }

        return Service.START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Log.isLoggable(TAG, Log.VERBOSE)) {
            Log.v(TAG, "Destroying service");
        }
        mCountDownTimer.cancel();
        cancelNotification();
        unregisterReceivers();
    }

    /**
     * Place the service into the foreground
     */
    public void foreground() {
        startForeground(NOTIFICATION_ID, mNotificationBuilder.build());
    }

    /**
     * Return the service to the background
     */
    public void background() {
        stopForeground(true);
    }

    public void updateNotification(int progress, String elapsedTime) {
        mNotificationBuilder.setContentText(getString(R.string.timer_notification_content, elapsedTime));
        mNotificationBuilder.setProgress((int)mTotalTimeInMilliseconds, progress, false);
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
                .setSmallIcon(R.mipmap.ic_stat_fish_icon)
                .setAutoCancel(true)
                .setShowWhen(false);

        builder.addAction(getNotificationActionPause());

        builder.setContentIntent(getContentIntent(TimerActivity.class));
        builder.setProgress(MAX_PROGRESS, MIN_PROGRESS, false);

        return builder;
    }

    private PendingIntent getContentIntent(Class<?> intentClass)  {
        Intent notificationIntent = new Intent(this, intentClass);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);

        return PendingIntent.getActivity(
                this,
                NOTIFICATION_RESULT_CODE,
                notificationIntent,
                0);
    }

    private NotificationCompat.Action getNotificationActionPlay() {
        Intent actionPlay = new Intent(this, TimerService.class);
        actionPlay.setAction(ACTION_NOTIFICATION_PLAY);
        PendingIntent actionPlayPendingIntent = PendingIntent.getService(
                this, NOTIFICATION_ACTION_CODE,
                actionPlay,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.play,
                getString(R.string.timer_notification_play),
                actionPlayPendingIntent).build();
    }

    private NotificationCompat.Action getNotificationActionPause() {
        Intent actionPause = new Intent(this, TimerService.class);
        actionPause.setAction(ACTION_NOTIFICATION_PAUSE);
        PendingIntent actionPausePendingIntent = PendingIntent.getService(
                this,
                NOTIFICATION_ACTION_CODE,
                actionPause,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.pause,
                getString(R.string.timer_notification_pause),
                actionPausePendingIntent).build();
    }

    private NotificationCompat.Action getNotificationActionDismiss() {
        Intent actionPause = new Intent(this, TimerService.class);
        actionPause.setAction(ACTION_NOTIFICATION_DISMISS);
        actionPause.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent actionPausePendingIntent = PendingIntent.getService(
                this,
                NOTIFICATION_ACTION_CODE,
                actionPause,
                0
        );
        return new NotificationCompat.Action.Builder(
                R.drawable.close_vector,
                getString(R.string.timer_notification_dismiss),
                actionPausePendingIntent).build();
    }

    private void setPlayAction() {
        clearNotificationActions();
        mNotificationBuilder.addAction(getNotificationActionPlay());
        foreground();

    }

    private void setPauseAction() {
        clearNotificationActions();
        mNotificationBuilder.addAction(getNotificationActionPause());
        foreground();

    }

    private void initCountDownTimer(long startTime) {
        mCountDownTimer = new CountDownTimer(startTime, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                // Now Timer is Running.
                mTimeLeft = leftTimeInMilliseconds;

                long calculatedTimeLeft = mTotalTimeInMilliseconds - leftTimeInMilliseconds;
                mCurrentHour = TimeUnit.MILLISECONDS.toHours(calculatedTimeLeft);
                mCurrentMinutes = TimeUnit.MILLISECONDS.toMinutes(calculatedTimeLeft) - TimeUnit.HOURS.toMinutes(mCurrentHour);
                mCurrentSeconds = TimeUnit.MILLISECONDS.toSeconds(calculatedTimeLeft) - TimeUnit.MINUTES.toSeconds(mCurrentMinutes);
                long progress = (mTotalTimeInMilliseconds - leftTimeInMilliseconds);

                if (mPreviousSecond != mCurrentSeconds ) {
                    Log.v(TAG, "" + mCurrentSeconds);
                }

                if (!isActivityToBackground && mCurrentSeconds != mPreviousSecond) {
                    Log.v(TAG, "Send Broadcast: Tick to Activity");
                    sendBroadcastTick(leftTimeInMilliseconds);
                }

                // Check if TimerService is Running, if yes update the Notification.
                // If TimerService is Running it means the app is in the background.
                if (isActivityToBackground && mPreviousMinute != mCurrentMinutes) {
                    Log.e(TAG, "Send Broadcast: Tick to Notification");
                    updateNotification((int)progress, getNotificationTimeString(mCurrentHour, mCurrentMinutes));
                    mPreviousMinute = mCurrentMinutes;
                }
                mPreviousSecond = mCurrentSeconds;
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "Send Broadcast: Finished");
                sendBroadcastFinished();
                if (isActivityToBackground) {
                    Log.v(TAG, "Finished: update notification");
                    if (mCurrentMinutes == 0 && mCurrentSeconds == 59) {
                        mCurrentMinutes = 1;
                    }

                    // Add Dismiss action.
                    clearNotificationActions();
                    mNotificationBuilder.addAction(getNotificationActionDismiss());
                    // Open MainActivity on notification click when CountdownTimer finished it's work.
                    mNotificationBuilder.setContentIntent(getContentIntent(MainActivity.class));
                    // Alert CountdownTimer finished by sound.
                    mNotificationBuilder.setSound(Utils.getNotificationCompletedSoundUri());
                    // Update the notification.
                    updateNotification((int)mTotalTimeInMilliseconds, getNotificationTimeString(mCurrentHour, mCurrentMinutes));
                }
            }
        };
    }

    private String getNotificationTimeString(long hours, long minutes) {
        StringBuilder sb = new StringBuilder();
        sb.append(getCorrectTimerTimeValue(hours) + "h:");
        sb.append(getCorrectTimerTimeValue(minutes) + "m");

        return sb.toString();
    }

    private void clearNotificationActions() {
        mNotificationBuilder.mActions.clear();
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
            setPauseAction();
        }
    };

    private BroadcastReceiver onPause = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "Service Received Action: Pause");
            pause();
            setPlayAction();
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
        initCountDownTimer(startTime);
        startCountDownTimer();
    }

    private void pause() {
        if (mCountDownTimer != null) {
            mCountDownTimer.cancel();
        }
    }
}
