package com.app.eisenflow.activities;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.annotation.Nullable;
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
import com.app.eisenflow.utils.Utils;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.helpers.RecyclerItemSwipeDetector.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.DateTimeUtils.getCorrectTimerTimeValue;
import static com.app.eisenflow.utils.DateTimeUtils.getTimerTimeInMillis;
import static com.app.eisenflow.utils.DateTimeUtils.isTimerTimeValid;
import static com.app.eisenflow.utils.Utils.hideKeyboard;

import static com.app.eisenflow.activities.MainActivity.TAG;

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

    private static final long COUNTDOWN_INTERVAL = 50;
    private int mHourValue = 0;
    private int mMinutesValue = 0;
    private long mTimeStart = 0;
    private long mTimeLeft = 0;
    private boolean isPaused;
    private boolean isTimerSet;
    private boolean isTimerRunning;
    private CountDownTimer mCountDownTimer;
    private boolean isFirstStarted;
    private long mTotalTimeInMilliseconds = 0;

    private boolean serviceBound;
    private TimerService timerService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        ButterKnife.bind(this);
        isPaused = true;

        addTextChangedListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        stopService();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTaskName();
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (isTimerRunning) {
            startService();
        } else {
            stopService();
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

        if (isPaused) {
            if (isTimerTimeValid(mHourValue, mMinutesValue)) {
                isPaused = false;
                setTimerTime();
                startTimer();
                hideKeyboard(this);
            } else {
                Utils.showAlertMessage(findViewById(R.id.timer_holder), "To start, enter time!", R.color.date);
            }
        }
        else {
            mCountDownTimer.cancel();
            mPlayButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.INVISIBLE);
            isPaused = true;
            isTimerRunning = false;
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

        mPlayButton.setVisibility(View.INVISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);

        mTimerSecondsHolder.setVisibility(View.VISIBLE);
        mTimerHour.setEnabled(false);
        mTimerMinutes.setEnabled(false);
    }

    private void startTimer() {
        initCountDownTimer();
        startCountDownTimer();
    }

    private void initCountDownTimer() {
        mCountDownTimer = new CountDownTimer(mTimeStart, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                // Now Timer is Running.
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
                mCircularProgressBar.setProgress((int)progress);

                // Check if TimerService is Running, if yes update the Notification.
                // If TimerService is Running it means the app is in the background.
                if(serviceBound && timerService != null) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(getCorrectTimerTimeValue(hours) + ":");
                    sb.append(getCorrectTimerTimeValue(minutes) + ":");
                    sb.append(getCorrectTimerTimeValue(seconds));
                    timerService.updateNotification(sb.toString());
                    timerService.foreground();
                }
            }

            @Override
            public void onFinish() {
                Log.v(TAG, "CountDownTimer Finished.");
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
    }

    private void startCountDownTimer() {
        mCountDownTimer.start();
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
        bindService(startIntent, mConnection, 0);
    }

    private void stopService() {
        if (serviceBound) {
            stopService(new Intent(this, TimerService.class));
            // Unbind the service
            unbindService(mConnection);
            serviceBound = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service bound");
            }
            TimerService.RunServiceBinder binder = (TimerService.RunServiceBinder) service;
            timerService = binder.getService();
            serviceBound = true;
            // Ensure the service is not in the foreground when bound
            timerService.background();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            serviceBound = false;
        }
    };
}
