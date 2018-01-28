package com.app.eisenflow.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.eisenflow.R;
import com.app.eisenflow.services.TimerService;
import com.app.eisenflow.utils.Utils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.utils.Statics.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Statics.TAG;
import static com.app.eisenflow.services.TimerService.ACTION_ACTIVITY_TO_BACKGROUND;
import static com.app.eisenflow.services.TimerService.ACTION_ACTIVITY_TO_FOREGROUND;
import static com.app.eisenflow.services.TimerService.ACTION_FINISHED;
import static com.app.eisenflow.services.TimerService.ACTION_PAUSE;
import static com.app.eisenflow.services.TimerService.ACTION_PLAY;
import static com.app.eisenflow.services.TimerService.ACTION_TICK;
import static com.app.eisenflow.services.TimerService.LEFT_TIME_MILLIS;
import static com.app.eisenflow.services.TimerService.START_TIME;
import static com.app.eisenflow.services.TimerService.TOTAL_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.DateTimeUtils.getCorrectTimerTimeValue;
import static com.app.eisenflow.utils.DateTimeUtils.getTimerTimeInMillis;
import static com.app.eisenflow.utils.DateTimeUtils.isTimerTimeValid;
import static com.app.eisenflow.utils.Utils.hideAlertMessage;
import static com.app.eisenflow.utils.Utils.hideKeyboard;
import static com.app.eisenflow.utils.Utils.isServiceRunning;
import static com.app.eisenflow.utils.Utils.createAlertMessage;
import static com.app.eisenflow.utils.Utils.showAlertMessage;

/**
 * Created on 1/1/18.
 */

public class TimerActivity extends AppCompatActivity {
    @BindView(R.id. close_timer_btn) LinearLayout mCloseButton;
    @BindView(R.id.play_btn) ImageView mPlayButton;
    @BindView(R.id.pause_btn) ImageView mPauseButton;
    @BindView(R.id.big_play_btn) View mBigPLayButton;
    @BindView(R.id.timer_task_title) TextView mTaskTitle;
    @BindView(R.id.timer_progress_bar) ProgressBar mCircularProgressBar;
    @BindView(R.id.timer_hour) EditText mTimerHour;
    @BindView(R.id.timer_minutes) EditText mTimerMinutes;
    @BindView(R.id.seconds_layout) LinearLayout mTimerSecondsHolder;
    @BindView(R.id.timer_seconds) EditText mTimerSeconds;

    private int mHourValue = 0;
    private int mMinutesValue = 0;
    private long mTimeStart = 0;
    private long mTimeLeft = 0;
    private boolean isPaused;
    private boolean isTimerSet;
    private boolean isTimerRunning;

    private boolean isFirstStarted;
    private long mTotalTimeInMilliseconds = 0;
    private Object alertMessage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        ButterKnife.bind(this);
        addTextChangedListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!isServiceRunning(TimerService.class)) {
            startService();
        }
        sendMessageActivityToForeground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isPaused = true;
        updatePlayPauseButton(true);
        setTaskName();
        registerReceivers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceivers();
        if (isTimerRunning) {
            sendMessageActivityToBackground();
        }
    }

    private void setTaskName() {
        Cursor cursor = getCursor();
        int taskPosition = getTaskPosition();
        if(taskPosition != -1 && cursor != null && cursor.moveToPosition(taskPosition)) {
            mTaskTitle.setText(cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
        }
    }

    private int getTaskPosition() {
        Bundle extras = getIntent().getExtras();
        return extras != null? extras.getInt(EXTRA_TASK_POSITION, -1): -1;
    }

    private void addTextChangedListeners() {
        mTimerHour.addTextChangedListener(mHourTextWatcher);
        mTimerMinutes.addTextChangedListener(mMinutesTextWatcher);
    }

    @OnClick (R.id.close_timer_btn)
    public void onCloseButtonClick() {
        finish();
    }

    @OnClick (R.id.big_play_btn)
    public void onBigPlayButton() {
        hideKeyboard(this);
        if (isPaused) {
            if (isTimerTimeValid(mHourValue, mMinutesValue)) {
                play();
                hideAlertMessage(alertMessage);
            } else {
                alertMessage = createAlertMessage(this, findViewById(R.id.timer_holder), getString(R.string.timer_alert_message), R.color.date);
                showAlertMessage(alertMessage);
            }
        }
        else {
            pause();
        }
    }

    private void play() {
        isPaused = false;
        updatePlayPauseButton(isPaused);
        setTimerTime();
        sendBroadcastPlay();
    }

    private void pause() {
        sendBroadcastPause();
        isPaused = true;
        isTimerRunning = false;
        updatePlayPauseButton(isPaused);
    }

    private void updatePlayPauseButton(boolean isPaused) {
        if (isPaused) {
            mPlayButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.INVISIBLE);
        } else {
            mPlayButton.setVisibility(View.INVISIBLE);
            mPauseButton.setVisibility(View.VISIBLE);
        }
    }

    private void setTimerTime() {
        isFirstStarted = true;

        if(!isTimerSet && mTotalTimeInMilliseconds == 0) {
            mTotalTimeInMilliseconds = getTimerTimeInMillis(mHourValue, mMinutesValue);
            mCircularProgressBar.setMax((int) (mTotalTimeInMilliseconds));
            isTimerSet = true;
        }

        if(mTimeStart == 0) {
            mTimeStart = mTotalTimeInMilliseconds;
        } else {
            mTimeStart = mTimeLeft;
        }
    }

    private void updatePlayViews() {
        mPlayButton.setVisibility(View.INVISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);

        mTimerSecondsHolder.setVisibility(View.VISIBLE);
        mTimerHour.setEnabled(false);
        mTimerMinutes.setEnabled(false);
    }

    private TextWatcher mHourTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!TextUtils.isEmpty(charSequence) && !isFirstStarted) {
                mHourValue = Integer.parseInt(charSequence.toString());
            }
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };

    private TextWatcher mMinutesTextWatcher = new TextWatcher() {
        @Override
        public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
            if (!TextUtils.isEmpty(charSequence) && !isFirstStarted) {
                mMinutesValue = Integer.parseInt(charSequence.toString());
            }
        }
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }
        @Override
        public void afterTextChanged(Editable editable) {
        }
    };


    private void startService() {
        Intent startIntent = new Intent(this, TimerService.class);
        startService(startIntent);
    }

    private void stopService() {
        if (Utils.isServiceRunning(TimerService.class)) {
            stopService(new Intent(this, TimerService.class));
        }
    }

    private BroadcastReceiver onTick = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "TimerActivity Received Action: Tick");
            updatePlayViews();

            if (intent != null) {
                Long leftTimeInMilliseconds = intent.getLongExtra(LEFT_TIME_MILLIS, 0);
                isPaused = false;
                isTimerRunning = true;
                // Save time for future use.
                mTimeLeft = leftTimeInMilliseconds;

                // Calculate hour, minutes, seconds by extracting
                // the current left time from the total time to go.
                long calculatedTimeLeft = mTotalTimeInMilliseconds - leftTimeInMilliseconds;
                long hours = TimeUnit.MILLISECONDS.toHours(calculatedTimeLeft);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(calculatedTimeLeft) - TimeUnit.HOURS.toMinutes(hours);
                long seconds = TimeUnit.MILLISECONDS.toSeconds(calculatedTimeLeft) - TimeUnit.MINUTES.toSeconds(minutes);

                // Add counter values.
                mTimerHour.setText(getCorrectTimerTimeValue(hours));
                mTimerMinutes.setText(getCorrectTimerTimeValue(minutes));
                mTimerSeconds.setText(getCorrectTimerTimeValue(seconds));

                long progress = (mTotalTimeInMilliseconds - leftTimeInMilliseconds);
                mCircularProgressBar.setProgress((int) progress);
            }
        }
    };

    private BroadcastReceiver onFinished = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.v(TAG, "TimerActivity Received Action: Finished");

            isTimerRunning = false;
            mCircularProgressBar.setProgress(0);
            mTimerHour.setEnabled(true);
            mTimerMinutes.setEnabled(true);
            mTimerHour.getText().clear();
            mTimerMinutes.getText().clear();
            mTimerSecondsHolder.setVisibility(View.GONE);
            mPauseButton.setVisibility(View.INVISIBLE);
            mPlayButton.setVisibility(View.VISIBLE);
        }
    };

    private void registerReceivers() {
        IntentFilter intentFilterTick = new IntentFilter(ACTION_TICK);
        LocalBroadcastManager.getInstance(this).registerReceiver(onTick, intentFilterTick);

        IntentFilter intentFilterFinished = new IntentFilter(ACTION_FINISHED);
        LocalBroadcastManager.getInstance(this).registerReceiver(onFinished, intentFilterFinished);
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onTick);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(onFinished);
    }

    private void sendBroadcastPlay() {
        Intent intentPlay = new Intent(ACTION_PLAY);
        intentPlay.putExtra(START_TIME, mTimeStart);
        intentPlay.putExtra(TOTAL_TIME, mTotalTimeInMilliseconds);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPlay);
    }

    private void sendBroadcastPause() {
        Intent intentPause = new Intent(ACTION_PAUSE);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentPause);
    }

    private void sendMessageActivityToBackground() {
        Intent intentActivityToBackground = new Intent(ACTION_ACTIVITY_TO_BACKGROUND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentActivityToBackground);
    }

    private void sendMessageActivityToForeground() {
        Intent intentActivityToForeground = new Intent(ACTION_ACTIVITY_TO_FOREGROUND);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intentActivityToForeground);
    }

    @Override
    public void onBackPressed() {
        Log.v(TAG, "onBackPressed() ...");
        stopService();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        Log.v(TAG, "onDestroy() ...");
        stopService();
        super.onDestroy();
    }
}
