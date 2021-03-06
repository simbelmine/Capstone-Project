package com.app.eisenflow.activities;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.helpers.TaskReminderHelper;
import com.app.eisenflow.utils.Constants;
import com.app.eisenflow.utils.DataUtils;
import com.app.eisenflow.utils.DateTimeUtils;
import com.app.eisenflow.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

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
import butterknife.OnTextChanged;

import static android.text.TextUtils.isEmpty;
import static com.app.eisenflow.EisenBottomSheet.EXTRA_TRANSITION_NAME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ADDRESS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE_MILLIS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_VIBRATION_ENABLED;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_LOCATION;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_WHEN;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TOTAL_DAYS_PERIOD;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.Constants.DEFAULT_MAP_ZOOM;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Constants.IS_FROM_PREVIEW;
import static com.app.eisenflow.utils.Constants.PLACE_AUTOCOMPLETE_REQUEST_CODE;
import static com.app.eisenflow.utils.Constants.TASK_PERSISTENT_OBJECT;
import static com.app.eisenflow.utils.Constants.TASK_PERSISTENT_PRIORITY;
import static com.app.eisenflow.utils.Constants.WEEKLY_OCCURRENCE;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.DataUtils.getTaskLocation;
import static com.app.eisenflow.utils.DataUtils.integerCollectionToString;
import static com.app.eisenflow.utils.DataUtils.setViewVisibility;
import static com.app.eisenflow.utils.DateTimeUtils.getDateString;
import static com.app.eisenflow.utils.DateTimeUtils.getTimeString;
import static com.app.eisenflow.utils.TaskUtils.getTotalDays;
import static com.app.eisenflow.utils.Utils.createAlertMessage;
import static com.app.eisenflow.utils.Utils.isTablet;
import static com.app.eisenflow.utils.Utils.showAlertMessage;

/**
 * Created on 12/21/17.
 */

public class SingleTaskActivity extends AppCompatActivity implements OnMapReadyCallback {
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
    @BindView(R.id.location_views) RelativeLayout mLocationViews;
    @BindView(R.id.map_fragment_holder) FrameLayout mMapFragmentHolder;
    @BindView(R.id.location_text) TextView mLocationText;
    @BindView(R.id.location_delete) ImageView mDeleteLocationButton;

    private Task mTask;
    private DataUtils.Priority mPriority;
    private Calendar mToday;
    private Set<Integer> mCheckedDaysOfWeek;
    private List<CheckBox> mWeekDays;
    private int mCurrentPosition = -1;
    private int mTaskId = -1;
    private boolean isRedTipShown;
    private boolean isTaskSaved;
    private boolean isOpenedFromPreview;

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

        if (savedInstanceState != null) {
            mTask = savedInstanceState.getParcelable(TASK_PERSISTENT_OBJECT);
            mPriority = (DataUtils.Priority) savedInstanceState.getSerializable(TASK_PERSISTENT_PRIORITY);
            if (mTask != null) {
                isTaskSaved = true;
            }
        }
        if (getIntent() != null) {
            isOpenedFromPreview = getIntent().getBooleanExtra(IS_FROM_PREVIEW, false);
        }
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
        populateData();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(TASK_PERSISTENT_OBJECT, mTask);
        outState.putSerializable(TASK_PERSISTENT_PRIORITY, mPriority);
    }

    private void setTransitionName() {
        Bundle extras = getIntent().getExtras();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (extras != null) {
                String imageTransitionName = extras.getString(EXTRA_TRANSITION_NAME);
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
            mDate.setText(getDateString(mToday));
            mTask.setDate(getDateString(mToday));
        } else {
            mDate.setText(mTask.getDate());
        }
        // Set Time.
        if (mTask.getTime() == null) {
            mTime.setText(getTimeString(mToday));
            mTask.setTime(getTimeString(mToday));
        } else {
            mTime.setText(getTimeString(getCalendarTime()));
        }
    }

    @OnClick(R.id.do_it_holder)
    public void onDoItHolderClick() {
        mPriority = DataUtils.Priority.ONE;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.ONE.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick(R.id.decide_holder)
    public void onDecideHolderClick() {
        mPriority = TWO;
        setBgPriorityColor();
        mTask.setPriority(TWO.getValue());
        setViewVisibility(mReminderHolder, View.VISIBLE);
    }

    @OnClick(R.id.delegate_holder)
    public void onDelegateHolderClick() {
        mPriority = DataUtils.Priority.THREE;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.THREE.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick(R.id.dump_it_holder)
    public void onDumpItHolderClick() {
        mPriority = DataUtils.Priority.FOUR;
        setBgPriorityColor();
        mTask.setPriority(DataUtils.Priority.FOUR.getValue());
        setViewVisibility(mReminderHolder, View.GONE);
    }

    @OnClick(R.id.date_holder)
    public void onDateHolderClick() {
        openDatePickerDialog();
    }

    @OnClick(R.id.time_holder)
    public void onTimeHolderClick() {
        openTimePickerDialog();
    }

    @OnClick(R.id.location_views)
    public void onLocationClick() {
        try {
            Intent intent =
                    new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                            .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        } catch (GooglePlayServicesRepairableException e) {
            Log.e(Constants.TAG, "GooglePlayServicesRepairableException: " + e.getMessage());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.e(Constants.TAG, "GooglePlayServicesNotAvailableException: " + e.getMessage());
        }
    }

    @OnClick(R.id.location_delete)
    public void onDeleteLocation() {
        mTask.setLocation(null);
        mTask.setAddress(null);
        mLocationText.setText(getResources().getString(R.string.location));
        mMapFragmentHolder.setVisibility(View.GONE);
        mDeleteLocationButton.setVisibility(View.INVISIBLE);
    }

    @OnCheckedChanged({R.id.daily_rb, R.id.weekly_rb, R.id.monthly_rb, R.id.yearly_rb})
    public void onOccurrenceChecked(CompoundButton button, boolean checked) {
        if (checked) {
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

    @OnCheckedChanged({R.id.mon_cb, R.id.tue_cb, R.id.wed_cb, R.id.thu_cb, R.id.fri_cb, R.id.sat_cb, R.id.sun_cb})
    public void onDayOfWeekChecked(CompoundButton button, boolean checked) {
        int buttonIdx = Integer.valueOf(button.getTag().toString());
        if (checked) {
            mCheckedDaysOfWeek.add(buttonIdx);
        } else {
            mCheckedDaysOfWeek.remove(buttonIdx);
        }
        mTask.setReminderWhen(integerCollectionToString(mCheckedDaysOfWeek));
    }

    @OnCheckedChanged({R.id.vibration_switch})
    public void onVibrationSwitchChecked(CompoundButton button, boolean checked) {
        mTask.setVibrationEnabled(DataUtils.getBooleanValue(checked));
    }

    @OnTextChanged(R.id.task_name)
    public void onTaskTitleChanged(CharSequence text) {
        String featureName = text.toString();
        mTask.setTitle(featureName);
    }

    @OnTextChanged(R.id.note_edit_text)
    public void onTaskNoteChanged(CharSequence text) {
        String featureName = text.toString();
        mTask.setNote(featureName);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        Utils.forceEditTextToLooseFocus(this, event);
        return super.dispatchTouchEvent(event);
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
                showDialogOnClose();
                return true;
            case R.id.action_save_task:
                saveTask();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE && resultCode == RESULT_OK) {
            if (data != null && data.getExtras() != null) {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Log.v(Constants.TAG, "Location: " + place.getAddress() + ", " + place.getLatLng());

                LatLng location = place.getLatLng();
                isTaskSaved = true;
                mTask.setAddress(place.getAddress().toString());
                mTask.setLocation(location.latitude + "," + location.longitude);
                //setTaskLocation();
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e(Constants.TAG, "PlacesAutocomplete: Something went wrong." + status);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                Toast.makeText(
                        this,
                        getResources().getString(R.string.no_place_selected),
                        Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        LatLng location = getTaskLocation(mTask.getLocation());
        String address = mTask.getAddress();

        if (location != null) {
            googleMap.clear();
            googleMap.addMarker(new MarkerOptions().position(location));
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, DEFAULT_MAP_ZOOM));
            mMapFragmentHolder.setVisibility(View.VISIBLE);
            mDeleteLocationButton.setVisibility(View.VISIBLE);
        }
        if (address != null) {
            mLocationText.setText(address);
        }
    }

    @Override
    public void onBackPressed() {
        showDialogOnClose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isTablet(this) && isOpenedFromPreview) {
            backToPreview();
        }
    }

    private void navigateBackIfRoot() {
        if (isTaskRoot()) {
            Intent upIntent = new Intent(this, LaunchActivity.class);
            startActivity(upIntent);
        } else if (isTablet(this) && isOpenedFromPreview) {
            backToPreview();
        }
    }

    private void backToPreview() {
        Intent intent = new Intent(this, PreviewActivity.class);
        intent.putExtra(EXTRA_TASK_POSITION, mCurrentPosition);
        startActivity(intent);
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
                int blended = Utils.blendColors(
                        getResources().getColor(fromColor),
                        getResources().getColor(toColor),
                        position);

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
        String date = getDateString(c);

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
        if (time != null) {
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
        String time = getTimeString(c);

        mTask.setTime(time);
        setTimeText(time);
    }

    private void setTimeText(String timeStr) {
        mTime.setText(timeStr);
    }

    private void saveTask() {
        if (isDataValid()) {
            // Show tip message for Urgent-Important tasks.
            // Ideally this tasks should be scheduled for next day in order to have better performance.
            if (isRedTask() && isScheduledTooInAdvance() && !isRedTipShown) {
                Object alertMessage = createAlertMessage(
                        this,
                        findViewById(R.id.single_task_holder),
                        getResources().getString(R.string.priority_0_tip_snackbar),
                        R.color.date);
                showAlertMessage(alertMessage);
                isRedTipShown = true;
                return;
            }

            // Set unset task details.
            mTask.setTitle(mTaskTitle.getText().toString());
            mTask.setNote(mNoteEditText.getText().toString());
            if (mCheckedDaysOfWeek != null && !mCheckedDaysOfWeek.isEmpty()) {
                mTask.setReminderWhen(integerCollectionToString(mCheckedDaysOfWeek));
            }
            if (mTask.getDate() == null) {
                mTask.setDate(getDateString(Calendar.getInstance()));
            }
            if (mTask.getTime() == null) {
                mTask.setTime(getTimeString(Calendar.getInstance()));
            }
            String taskActualTime = DateTimeUtils.getActualTime(mTask.getTime());
            Calendar cal = DateTimeUtils.getCalendar(mTask.getDate(), taskActualTime);
            mTask.setDateMillis(cal.getTimeInMillis());
            mTask.setTotalDaysPeriod(getTotalDays(mTask.getDate()));

            // Insert/Update with created content values.
            ContentValues values = getContentValues();
            if (mTask.getId() == -1) {
                Uri uri = insertData(values);
                long taskId = ContentUris.parseId(uri);
                mTask.setId(taskId);
            } else {
                updateData(values);
            }

            if (mPriority == TWO) {
                TaskReminderHelper.setRepeatingReminder(mTask);
            } else {
                TaskReminderHelper.setReminder(mTask);
            }

            if (isTablet(this) && isOpenedFromPreview) {
                backToPreview();
            }
            finish();
        }
    }

    private boolean isDataValid() {
        String taskTitle = mTaskTitle.getText().toString();
        if (isEmpty(taskTitle)) {
            Object alertMessage = createAlertMessage(
                    this,
                    mSingleTaskHolder,
                    getString(R.string.add_task_name_alert),
                    R.color.alert_color);
            showAlertMessage(alertMessage);
            return false;
        }
        if (mTask.getPriority() == -1) {
            Object alertMessage = createAlertMessage(
                    this,
                    mSingleTaskHolder,
                    getString(R.string.add_task_priority_alert),
                    R.color.alert_color);
            showAlertMessage(alertMessage);
            return false;
        }

        if (DataUtils.Priority.valueOf(mTask.getPriority()) == TWO &&
                mCheckedDaysOfWeek != null && mCheckedDaysOfWeek.isEmpty() &&
                mTask.getReminderOccurrence() == WEEKLY_OCCURRENCE) {
            Object alertMessage = createAlertMessage(
                    this,
                    mSingleTaskHolder,
                    getString(R.string.add_task_reminder_when_alert),
                    R.color.alert_color);
            showAlertMessage(alertMessage);
            return false;
        }

        if (!checkDateTime()) {
            return false;
        }
        return true;
    }

    private boolean checkDateTime() {
        String dateStr = mDate.getText().toString();
        String timeS = mTime.getText().toString();

        if (!DateTimeUtils.isDateValid(dateStr)) {
            Object alertMessage = createAlertMessage(
                    this, mSingleTaskHolder,
                    getResources().getString(R.string.add_task_date_alert),
                    R.color.alert_color);
            showAlertMessage(alertMessage);
            return false;
        }
        if (!DateTimeUtils.isTimeValid(dateStr, timeS)) {
            Object alertMessage = createAlertMessage(
                    this,
                    mSingleTaskHolder,
                    getResources().getString(R.string.add_task_time_alert),
                    R.color.alert_color);
            showAlertMessage(alertMessage);
            return false;
        }
        return true;
    }

    private boolean isRedTask() {
        return DataUtils.Priority.valueOf(mTask.getPriority()) == DataUtils.Priority.ONE;
    }

    private boolean isScheduledTooInAdvance() {
        Calendar currDate = Calendar.getInstance();
        Calendar date = Calendar.getInstance();
        date.setTime(DateTimeUtils.getDate(mTask.getDate()));

        return date.get(Calendar.MONTH) >= currDate.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) >= (currDate.get(Calendar.DAY_OF_MONTH) + 2);
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

    private void updateData(ContentValues values) {
        Uri uri = buildFlavorsUri(mTask.getId());
        getContentResolver().update(uri, values, null, null);
    }

    private Uri insertData(ContentValues values) {
        return getContentResolver().insert(CONTENT_URI, values);
    }

    private ContentValues getContentValues() {
        ContentValues values;
        values = new ContentValues();
        values.put(KEY_PRIORITY, mTask.getPriority());
        values.put(KEY_TITLE, mTask.getTitle());
        values.put(KEY_DATE, mTask.getDate());
        values.put(KEY_TIME, mTask.getTime());
        values.put(KEY_DATE_MILLIS, mTask.getDateMillis());
        values.put(KEY_REMINDER_OCCURRENCE, mTask.getReminderOccurrence());
        values.put(KEY_REMINDER_WHEN, mTask.getReminderWhen());
        values.put(KEY_ADDRESS, mTask.getAddress());
        values.put(KEY_LOCATION, mTask.getLocation());
        values.put(KEY_TOTAL_DAYS_PERIOD, mTask.getTotalDaysPeriod());
        values.put(KEY_IS_VIBRATION_ENABLED, mTask.isVibrationEnabled());
        values.put(KEY_NOTE, mTask.getNote());

        return values;
    }

    private boolean isNewTask() {
        return mCurrentPosition == -1;
    }

    private void populateData() {
        Cursor cursor = getCursor();
        if (cursor != null && cursor.moveToPosition(mCurrentPosition)) {
            if (!isNewTask() && !isTaskSaved) {
                mTask = cursorToTask(cursor);
            }
        }

        mPriority = DataUtils.Priority.valueOf(mTask.getPriority());
        setBackgroundWithAnimation();
        mTaskTitle.setText(mTask.getTitle());
        initDateTime();
        setReminderOccurrenceAndWhen(mPriority);
        setTaskLocation();
        setVibration();
        mNoteEditText.setText(mTask.getNote());
        isTaskSaved = false;
    }

    private void setReminderOccurrenceAndWhen(DataUtils.Priority priority) {
        if (priority == TWO) {
            // Set Reminder's occurrence.
            setViewVisibility(mReminderHolder, View.VISIBLE);
            int occurrenceChoice = mTask.getReminderOccurrence();
            setReminderOccurrenceChoice(occurrenceChoice);
            // Set Reminder's when (weekly occurrence).
            int idxChild = mOccurrenceHolder.indexOfChild(findViewById(R.id.weekly_rb));
            if (occurrenceChoice == idxChild) {
                setViewVisibility(mWeekDaysHolder, View.VISIBLE);
                setReminderWhenChoice();
            }
        }
    }

    private void setReminderOccurrenceChoice(int choice) {
        int radioButtonId;
        switch (choice) {
            case 1:
                radioButtonId = R.id.weekly_rb;
                break;
            case 2:
                radioButtonId = R.id.monthly_rb;
                break;
            case 3:
                radioButtonId = R.id.yearly_rb;
                break;
            default:
                radioButtonId = R.id.daily_rb;
                break;
        }
        mOccurrenceHolder.check(radioButtonId);
    }

    private void setReminderWhenChoice() {
        mCheckedDaysOfWeek = (HashSet<Integer>) DataUtils.stringToIntegerCollection(mTask.getReminderWhen());
        if (mCheckedDaysOfWeek != null && mCheckedDaysOfWeek.size() > 0) {
            for (CheckBox cb : mWeekDays) {
                int idx = Integer.valueOf((String) cb.getTag());
                if (mCheckedDaysOfWeek.contains(idx)) {
                    cb.setChecked(true);
                }
            }
        }
    }

    private void setTaskLocation() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setVibration() {
        mVibrationSwitch.setChecked(DataUtils.getBooleanState(mTask.isVibrationEnabled()));
    }

    private void showDialogOnClose() {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.close_dialog_text)
                .setPositiveButton(R.string.discard_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        backWithTransition();
                    }
                })
                .setNegativeButton(R.string.keep_edit, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        navigateBackIfRoot();
                    }
                })
                .setCancelable(false)
                .show();
    }
}
