package com.app.eisenflow.activities;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.utils.DateTimeUtils;
import com.app.eisenflow.utils.Utils;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 12/21/17.
 */

public class SingleTaskActivity extends AppCompatActivity {
    @BindView(R.id.single_task_app_bar) AppBarLayout mAppBarLayout;
    @BindView(R.id.single_task_toolbar) Toolbar mToolbar;
    @BindView(R.id.title_holder) ImageView mTitleHolder;
    @BindView(R.id.task_name) EditText mTaskTitle;
    @BindView(R.id.do_it_holder) FrameLayout mDoItHolder;
    @BindView(R.id.decide_holder) FrameLayout mDecideHolder;
    @BindView(R.id.delegate_holder) FrameLayout mDelegateHolder;
    @BindView(R.id.dump_it_holder) FrameLayout mDumpItHolder;
    @BindView(R.id.date_holder) FrameLayout mDateHolder;
    @BindView(R.id.date_txt) TextView mDate;
    @BindView(R.id.time_holder) FrameLayout mTimeHolder;
    @BindView(R.id.time_txt) TextView mTime;
    @BindView(R.id.occurrence_holder) RadioGroup mOccurrenceHolder;
    @BindView(R.id.mon_cb) CheckBox mMonCheckBox;
    @BindView(R.id.tue_cb) CheckBox mTueCheckBox;
    @BindView(R.id.wed_cb) CheckBox mWedCheckBox;
    @BindView(R.id.thu_cb) CheckBox mThuCheckBox;
    @BindView(R.id.fri_cb) CheckBox mFriCheckBox;
    @BindView(R.id.sat_cb) CheckBox mSatCheckBox;
    @BindView(R.id.sun_cb) CheckBox mSunCheckBox;
    @BindView(R.id.vibration_switch) Switch mVibrationSwitch;
    @BindView(R.id.note_edit_text) EditText mNoteEditText;

    // Enum to set priority highest to lowest (1 to 4).
    private enum Priority {
        DEFAULT(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4);

        private static Map map = new HashMap<>();
        private int value;
        Priority(int value) {
            this.value = value;
        }

        static {
            for (Priority pageType : Priority.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static Priority valueOf(int pageType) {
            Priority priority = (Priority) map.get(pageType);
            if (priority == null) {
                return DEFAULT;
            }
            return priority ;
        }

        public int getValue() {
            return value;
        }
    }
    private Task mTask;
    private Priority mPriority;
    private Calendar today;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.close_small);

        init();
    }

    private void init() {
        mTask = new Task();
        mPriority = Priority.DEFAULT;
        today = Calendar.getInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDateTime();
    }

    private void initDateTime() {
        // Set Date.
        if (mTask.getDate() == null) {
            mDate.setText(DateTimeUtils.getDateString(today));
        } else {
            mDate.setText(mTask.getDate());
        }
        // Set Time.
        if (mTask.getTime() == null) {
            mTime.setText(DateTimeUtils.getTimeString(today));
        } else {
            mTime.setText(DateTimeUtils.getTimeString(getCalendarTime()));
        }
    }

    @OnClick (R.id.do_it_holder)
    public void onClickDoItHolder() {
        mPriority = Priority.ONE;
        setBgPriorityColor();
        mTask.setPriority(Priority.ONE.getValue());
    }

    @OnClick (R.id.decide_holder)
    public void onClickDecideHolder() {
        mPriority = Priority.TWO;
        setBgPriorityColor();
        mTask.setPriority(Priority.TWO.getValue());
    }

    @OnClick (R.id.delegate_holder)
    public void onClickDelegateHolder() {
        mPriority = Priority.THREE;
        setBgPriorityColor();
        mTask.setPriority(Priority.THREE.getValue());
    }

    @OnClick (R.id.dump_it_holder)
    public void onClickDumpItHolder() {
        mPriority = Priority.FOUR;
        setBgPriorityColor();
        mTask.setPriority(Priority.FOUR.getValue());
    }

    @OnClick (R.id.date_holder)
    public void onClickDateHolder() {
        openDatePickerDialog();
    }

    @OnClick (R.id.time_holder)
    public void onClickTimeHolder() {
        openTimePickerDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save_task:
                Toast.makeText(SingleTaskActivity.this, "TASK SAVED", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

    private void setBgPriorityColor() {
        switch (mPriority) {
            case ONE:
                setBackgroundWithAnimation();
                break;
            case TWO:
                setBackgroundWithAnimation();
                break;
            case THREE:
                setBackgroundWithAnimation();
                break;
            case FOUR:
                setBackgroundWithAnimation();
                break;
        }
    }

    private void setBackgroundWithAnimation() {
        final int priority = mTask.getPriority();
        final int fromColor = getBackgroundColorByPriority(Priority.valueOf(priority));
        final int toColor = getBackgroundColorByPriority(mPriority);

        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                float position = animation.getAnimatedFraction();
                int blended = Utils.blendColors(getResources().getColor(fromColor), getResources().getColor(toColor), position);

                // Apply blended color to the view.
                mTitleHolder.setBackgroundColor(blended);
            }
        });
        anim.start();
    }

    private int getBackgroundColorByPriority(Priority priority) {
        switch (priority) {
            case ONE:
                return R.color.firstQuadrant;
            case TWO:
                return R.color.secondQuadrant;
            case THREE:
                return R.color.thirdQuadrant;
            case FOUR:
                return R.color.fourthQuadrant;
            default:
                return R.color.default_title_holder;
        }
    }

    private void openDatePickerDialog() {
        Calendar dateToSet = getCalendarDate();

        DatePickerDialog mDatePickerDialog;
        mDatePickerDialog = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day_of_month) {
                setDateAfterPick(year, month, day_of_month);
            }
        }, dateToSet.get(Calendar.YEAR), dateToSet.get(Calendar.MONTH), dateToSet.get(Calendar.DAY_OF_MONTH));
        mDatePickerDialog.show();
    }

    private Calendar getCalendarDate() {
        Calendar calendar = Calendar.getInstance();
        String date = mTask.getDate();
        Date dateToReturn;
        if (date != null) {
            dateToReturn = DateTimeUtils.getDate(date);
            calendar.setTime(dateToReturn);
        }

        return calendar;
    }

    private void setDateAfterPick(int selectedYear, int selectedMonth, int selectedDayOfMonth) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, selectedYear);
        c.set(Calendar.MONTH, selectedMonth);
        c.set(Calendar.DAY_OF_MONTH, selectedDayOfMonth);
        String date = DateTimeUtils.getDateString(c);

        mTask.setDate(date);
        setDateText(date);
    }

    private void setDateText(String dateStr) {
        mDate.setText(dateStr);
    }

    private void openTimePickerDialog() {
        Calendar timeToSet = getCalendarTime();

        TimePickerDialog mTimePicker;
        mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                setTimeAfterPick(selectedHour, selectedMinute);
            }
        }, timeToSet.get(Calendar.HOUR_OF_DAY), timeToSet.get(Calendar.MINUTE), DateTimeUtils.isSystem24hFormat());
        mTimePicker.show();
    }

    private Calendar getCalendarTime() {
        Calendar cal = Calendar.getInstance();
        String time = mTask.getTime();
        Date timeToReturn;
        if(time != null) {
            timeToReturn = DateTimeUtils.getTime(time);
            if (timeToReturn == null) {
                return cal;
            }
            cal.setTime(timeToReturn);
        }
        return cal;
    }

    private void setTimeAfterPick(int selectedHour, int selectedMinute) {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, selectedHour);
        c.set(Calendar.MINUTE, selectedMinute);
        String time = DateTimeUtils.getTimeString(c);

        mTask.setTime(time);
        setTimeText(time);
    }

    private void setTimeText(String timeStr) {
        mTime.setText(timeStr);
    }
}