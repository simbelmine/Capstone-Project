package com.app.eisenflow.activities;

import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.helpers.RecyclerItemSwipeDetector;
import com.app.eisenflow.utils.DataUtils;
import com.app.eisenflow.utils.DateTimeUtils;
import com.app.eisenflow.utils.Utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnCheckedChanged;
import butterknife.OnClick;

import static android.text.TextUtils.isEmpty;
import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE_MILLIS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_VIBRATION_ENABLED;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_WHEN;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.helpers.RecyclerItemSwipeDetector.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.DataUtils.setViewVisibility;
import static com.app.eisenflow.utils.Utils.showAlertMessage;

/**
 * Created on 12/21/17.
 */

public class SingleTaskActivity extends AppCompatActivity {
    @BindView(R.id.single_task_holder) CoordinatorLayout mSingleTaskHolder;
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
    @BindView(R.id.reminder_holder) ConstraintLayout mReminderHolder;
    @BindView(R.id.occurrence_holder) RadioGroup mOccurrenceHolder;
    @BindView(R.id.week_days_holder) TableLayout mWeekDaysHolder;
    @BindView(R.id.mon_cb) CheckBox mMonCheckBox;
    @BindView(R.id.tue_cb) CheckBox mTueCheckBox;
    @BindView(R.id.wed_cb) CheckBox mWedCheckBox;
    @BindView(R.id.thu_cb) CheckBox mThuCheckBox;
    @BindView(R.id.fri_cb) CheckBox mFriCheckBox;
    @BindView(R.id.sat_cb) CheckBox mSatCheckBox;
    @BindView(R.id.sun_cb) CheckBox mSunCheckBox;
    @BindView(R.id.vibration_switch) Switch mVibrationSwitch;
    @BindView(R.id.note_edit_text) EditText mNoteEditText;

    private static final int WEEKLY_OCCURRENCE = 1;
    private Task mTask;
    private DataUtils.Priority mPriority;
    private Calendar mToday;
    private Set<Integer> mCheckedDaysOfWeek;
    private List<CheckBox> mWeekDays;
    private int mCurrentPosition = -1;
    private int mTaskId = -1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.close_vector);

        init();


        setTransitionName();
        getCurrentPosition();
    }

    private void init() {
        mTask = new Task();
        mPriority = DataUtils.Priority.DEFAULT;
        mToday = Calendar.getInstance();
        mCheckedDaysOfWeek = new HashSet<>();
        mWeekDays = getWeekDaysList();
        setViewVisibility(mReminderHolder, View.GONE);
        setViewVisibility(mWeekDaysHolder, View.GONE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        initDateTime();
        if (!isNewTask()) {
            populateData();
        }
    }

    private void setTransitionName() {
        Bundle extras = getIntent().getExtras();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (extras != null) {
                String imageTransitionName = extras.getString(RecyclerItemSwipeDetector.EXTRA_TRANSITION_NAME);
                findViewById(R.id.task_title_holder).setTransitionName(imageTransitionName);
            }
        }
    }

    private void getCurrentPosition() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
             mCurrentPosition = extras.getInt(EXTRA_TASK_POSITION);
        }
    }

    private void initDateTime() {
        // Set Date.
        if (mTask.getDate() == null) {
            mDate.setText(DateTimeUtils.getDateString(mToday));
        } else {
            mDate.setText(mTask.getDate());
        }
        // Set Time.
        if (mTask.getTime() == null) {
            mTime.setText(DateTimeUtils.getTimeString(mToday));
        } else {
            mTime.setText(DateTimeUtils.getTimeString(getCalendarTime()));
        }
    }

    @OnClick (R.id.do_it_holder)
    public void onDoItHolderClick() {
        mPriority = DataUtils.Priority.ONE;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.ONE.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick (R.id.decide_holder)
    public void onDecideHolderClick() {
        mPriority = DataUtils.Priority.TWO;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.TWO.getValue());
        setViewVisibility(mReminderHolder, View.VISIBLE);
    }

    @OnClick (R.id.delegate_holder)
    public void onDelegateHolderClick() {
        mPriority = DataUtils.Priority.THREE;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.THREE.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick (R.id.dump_it_holder)
    public void onDumpItHolderClick() {
        mPriority = DataUtils.Priority.FOUR;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.FOUR.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick (R.id.date_holder)
    public void onDateHolderClick() {
        openDatePickerDialog();
    }

    @OnClick (R.id.time_holder)
    public void onTimeHolderClick() {
        openTimePickerDialog();
    }

    @OnCheckedChanged ({R.id.daily_rb, R.id.weekly_rb, R.id.monthly_rb, R.id.yearly_rb})
    public void onOccurrenceChecked(CompoundButton button, boolean checked) {
        if (checked) {
            //ToDo: Add Occurrence from task like that:  mOccurrenceHolder.indexOfChild(button));
            //ToDo: +before+ : int radioButtonID = radioButtonGroup.getCheckedRadioButtonId();
            //ToDo:                 View radioButton = radioButtonGroup.findViewById(radioButtonID);

            if (checked) {
                int buttonIdx = mOccurrenceHolder.indexOfChild(button);
                mTask.setReminderOccurrence(buttonIdx);
                if (button.getId() == R.id.weekly_rb) {
                    setViewVisibility(mWeekDaysHolder, View.VISIBLE);
                } else {
                    setViewVisibility(mWeekDaysHolder, View.GONE);
                }
            }
        }
    }

    @OnCheckedChanged ({R.id.mon_cb, R.id.tue_cb, R.id.wed_cb, R.id.thu_cb, R.id.fri_cb, R.id.sat_cb, R.id.sun_cb})
    public void onDayOfWeekChecked(CompoundButton button, boolean checked) {
        int buttonIdx = Integer.valueOf(button.getTag().toString());
        if (checked) {
            mCheckedDaysOfWeek.add(buttonIdx);
        } else {
            mCheckedDaysOfWeek.remove(buttonIdx);
        }
    }

    @OnCheckedChanged ({R.id.vibration_switch})
    public void onVibrationSwitchChecked(CompoundButton button, boolean checked) {
        mTask.setVibrationEnabled(DataUtils.getVibrationStateValue(checked));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                backWithTransition();
                return true;
            case R.id.action_save_task:
                Toast.makeText(SingleTaskActivity.this, "TASK SAVED", Toast.LENGTH_SHORT).show();
                saveTask();
                return true;
        }

        return true;
    }

    private void backWithTransition() {
        if (Build.VERSION.SDK_INT >= 21) {
            supportFinishAfterTransition();
        } else {
            finish();
        }
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
        final int fromColor = getBackgroundColorByPriority(DataUtils.Priority.valueOf(priority));
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

    private int getBackgroundColorByPriority(DataUtils.Priority priority) {
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

    private void saveTask() {
        if(isDataValid()) {
            mTask.setTitle(mTaskTitle.getText().toString());
            mTask.setNote(mNoteEditText.getText().toString());
            if (!mCheckedDaysOfWeek.isEmpty()) {
                mTask.setReminderWhen(DataUtils.integerCollectionToString(mCheckedDaysOfWeek));
            }
            if (mTask.getDate() == null) {
                mTask.setDate(DateTimeUtils.getDateString(Calendar.getInstance()));
            }
            if (mTask.getTime() == null) {
                mTask.setTime(DateTimeUtils.getTimeString(Calendar.getInstance()));
            }
            Calendar cal = DateTimeUtils.getCalendar(mTask.getDate(), mTask.getTime());
            mTask.setDateMillis((int)cal.getTimeInMillis());

            Cursor c = getCursor();
            //if (c.getCount() == 0){
            if (mTask.getId() == -1) {
                insertData();
            } else {
                updateData();
            }
            finish();
        }
    }

    private boolean isDataValid() {
        String taskTitle = mTaskTitle.getText().toString();
        if (isEmpty(taskTitle)) {
            showAlertMessage(mSingleTaskHolder, getString(R.string.add_task_name_alert), R.color.alert_color);
            return false;
        }
        if(mTask.getPriority() == -1) {
            showAlertMessage(mSingleTaskHolder, getString(R.string.add_task_priority_alert), R.color.alert_color);
            return false;
        }

        if (DataUtils.Priority.valueOf(mTask.getPriority()) == DataUtils.Priority.TWO && mCheckedDaysOfWeek.isEmpty() && mTask.getReminderOccurrence() == WEEKLY_OCCURRENCE) {
            showAlertMessage(mSingleTaskHolder, getString(R.string.add_task_reminder_when_alert), R.color.alert_color);
            return false;
        }

        if(!checkDateTime()) {
            return false;
        }
        return true;
    }

    private boolean checkDateTime() {
        String dateStr = mDate.getText().toString();
        String timeS = mTime.getText().toString();

        if (!DateTimeUtils.isDateValid(dateStr)) {
            showAlertMessage(mSingleTaskHolder, getResources().getString(R.string.add_task_date_alert), R.color.alert_color);
            return false;
        }
        if (!DateTimeUtils.isTimeValid(dateStr, timeS)) {
            showAlertMessage(mSingleTaskHolder, getResources().getString(R.string.add_task_time_alert), R.color.alert_color);
            return false;
        }
        return true;
    }

    private List<CheckBox> getWeekDaysList() {
        List<CheckBox> list = new ArrayList<>();
        list.add(mMonCheckBox);
        list.add(mTueCheckBox);
        list.add(mWedCheckBox);
        list.add(mThuCheckBox);
        list.add(mFriCheckBox);
        list.add(mSatCheckBox);
        list.add(mSunCheckBox);
        return list;
    }

    private Cursor getCursor() {
        return getContentResolver().query(
                CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    private void updateData() {
        // *** Example ***
//        ContentValues values = new ContentValues();
//        values.put(KEY_PRIORITY, 2);
        // ***************

        Uri uri = buildFlavorsUri(mTask.getId());

        // Update record in the DB.
//        getContentResolver().update(uri, values, null, null);
    }

    public void insertData(){
        ContentValues values;
        values = new ContentValues();
        values.put(KEY_PRIORITY, mTask.getPriority());
        values.put(KEY_TITLE, mTask.getTitle());
        values.put(KEY_DATE, mTask.getDate());
        values.put(KEY_TIME, mTask.getTime());
        values.put(KEY_DATE_MILLIS, mTask.getDateMillis());
        values.put(KEY_REMINDER_OCCURRENCE, mTask.getReminderOccurrence());
        values.put(KEY_REMINDER_WHEN, mTask.getReminderWhen());
        values.put(KEY_IS_VIBRATION_ENABLED, mTask.isVibrationEnabled());
        values.put(KEY_NOTE, mTask.getNote());

        // Insert our ContentValues.
        getContentResolver().insert(CONTENT_URI, values);
    }

    private boolean isNewTask() {
        return mCurrentPosition == -1 ? true : false;
    }

    private void populateData() {
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(mCurrentPosition)) {
            mTask = cursorToTask(cursor);

            mPriority = DataUtils.Priority.valueOf(mTask.getPriority());
            setBackgroundWithAnimation();
            mTaskTitle.setText(mTask.getTitle());
            initDateTime();
            setReminderOccurrenceChoice();
            setReminderWhenChoice();
            setVibration();
            mNoteEditText.setText(mTask.getNote());
        }
    }

    private void setReminderOccurrenceChoice() {
        int choice = mTask.getReminderOccurrence();
        mOccurrenceHolder.check(choice);
    }

    private void setReminderWhenChoice() {
        mCheckedDaysOfWeek = (HashSet<Integer>)DataUtils.stringToIntegerCollection(mTask.getReminderWhen());
        if (mCheckedDaysOfWeek != null && mCheckedDaysOfWeek.size() > 0) {
            for (CheckBox cb : mWeekDays) {
                int idx = Integer.valueOf((String) cb.getTag());
                if (mCheckedDaysOfWeek.contains(idx)) {
                    cb.setChecked(true);
                }
            }
        }
    }

    private void setVibration() {
        mVibrationSwitch.setChecked(DataUtils.getBooleanValue(mTask.isVibrationEnabled()));
    }
}
