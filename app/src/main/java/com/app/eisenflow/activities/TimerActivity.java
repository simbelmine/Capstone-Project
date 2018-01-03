package com.app.eisenflow.activities;

import android.database.Cursor;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.app.eisenflow.R;
import com.app.eisenflow.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.helpers.RecyclerItemSwipeDetector.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.DateTimeUtils.getCorrectTimerTimeValue;
import static com.app.eisenflow.utils.DateTimeUtils.getTimerHourInMillis;
import static com.app.eisenflow.utils.DateTimeUtils.getTimerMinutesInMillis;
import static com.app.eisenflow.utils.DateTimeUtils.getTimerTimeInMillis;
import static com.app.eisenflow.utils.DateTimeUtils.isTimerTimeValid;
import static com.app.eisenflow.utils.Utils.hideKeyboard;

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

    private static final int MINUTES_IN_HOUR = 60;
    private static final int SECONDS_IN_MINUTE = 60;
    private static final long COUNTDOWN_INTERVAL = 50;
    private int mHourValue = 0;
    private int mMinutesValue = 0;
    private long mTotalTimeLeft = 0;
    private boolean isPaused;
    private boolean isTimerSet;
    private CountDownTimer mCountDownTimer;
    private int mOldSeconds = 0;
    private int secondsProgress = 1;
    private int minutesProgress = 0;
    private int hoursProgress = 0;
    private boolean isAdditionalMinsReady;
    private boolean isFirstStarted;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timer);
        ButterKnife.bind(this);

        addTextChangedListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setTaskName();
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
        if (!isPaused) {
            if (isTimerTimeValid(mHourValue, mMinutesValue)) {
                setTimerTime();
                startTimer();
            } else {
                Utils.showAlertMessage(findViewById(R.id.timer_holder), "To start, enter time!", R.color.date);
            }
            hideKeyboard(this);
        } else {
            isPaused = false;
            mCountDownTimer.cancel();
            mPlayButton.setVisibility(View.VISIBLE);
            mPauseButton.setVisibility(View.INVISIBLE);
        }
    }

    private void setTimerTime() {
        isFirstStarted = true;
        isPaused = true;

        long totalTimeCountInMilliseconds = 0;
        if(!isTimerSet) {
            totalTimeCountInMilliseconds = getTimerTimeInMillis(mHourValue, mMinutesValue);
            mCircularProgressBar.setMax((int) (totalTimeCountInMilliseconds));
            isTimerSet = true;
        }

        if(mTotalTimeLeft == 0) {
            mTotalTimeLeft = totalTimeCountInMilliseconds;
        }

        mPlayButton.setVisibility(View.INVISIBLE);
        mPauseButton.setVisibility(View.VISIBLE);

        mTimerSecondsHolder.setVisibility(View.VISIBLE);
        mTimerHour.setInputType(InputType.TYPE_NULL);
        mTimerMinutes.setInputType(InputType.TYPE_NULL);

        mTimerHour.setText(getCorrectTimerTimeValue(0));
        mTimerMinutes.setText(getCorrectTimerTimeValue(0));
    }

    private void startTimer() {
        initCountDownTimer();
        startCountDownTimer();
    }

    private void initCountDownTimer() {
        mCountDownTimer = new CountDownTimer(mTotalTimeLeft, COUNTDOWN_INTERVAL) {
            @Override
            public void onTick(long leftTimeInMilliseconds) {
                mTotalTimeLeft = leftTimeInMilliseconds;

                long progress = (getTimerTimeInMillis(mHourValue, mMinutesValue) - leftTimeInMilliseconds);
                mCircularProgressBar.setProgress(Math.abs((int)progress));

                long progressInSeconds = progress/1000;

                if(mOldSeconds != progressInSeconds) {

                    if(secondsProgress == SECONDS_IN_MINUTE-1) {
                        if(getTimerHourInMillis(mHourValue) == 0) {
                            isAdditionalMinsReady = true;
                        }

                        // check if there are some minutes to go before counting the hour
                        if (!isAdditionalMinsReady && minutesProgress < MINUTES_IN_HOUR) {
                            minutesProgress++;
                            mTimerMinutes.setText(getCorrectTimerTimeValue(minutesProgress));
                        }
                        else if (!isAdditionalMinsReady) {
                            if (hoursProgress < getTimerHourInMillis(mHourValue) / 1000) {
                                hoursProgress++;
                                mTimerHour.setText(getCorrectTimerTimeValue(hoursProgress));
                            }
                            else {
                                isAdditionalMinsReady = true;
                                hoursProgress = 0;
                            }
                            minutesProgress = 0;
                        }

                        if (isAdditionalMinsReady && minutesProgress < (getTimerMinutesInMillis(mMinutesValue) / 1000)) {
                            minutesProgress++;
                            mTimerMinutes.setText(getCorrectTimerTimeValue(minutesProgress));
                        }

                        secondsProgress = 1;
                        mTimerSeconds.setText(getCorrectTimerTimeValue(0));
                    }
                    else {
                        mTimerSeconds.setText(getCorrectTimerTimeValue(secondsProgress));
                        secondsProgress++;
                    }
                }

                mOldSeconds = (int)progressInSeconds;
            }

            @Override
            public void onFinish() {
                mCircularProgressBar.setProgress(0);
                mTimerSecondsHolder.setVisibility(View.GONE);
                mTimerHour.setCursorVisible(true);
                mTimerMinutes.setCursorVisible(true);
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
}
