package com.app.eisenflow.activities;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.helpers.TaskReminderHelper;
import com.app.eisenflow.utils.DataUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_VIBRATION_ENABLED;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PROGRESS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_WHEN;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TOTAL_DAYS_PERIOD;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.helpers.TaskReminderHelper.cancelReminder;
import static com.app.eisenflow.helpers.TaskReminderHelper.cancelRepeatingReminder;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.DataUtils.Occurrence.WEEKLY;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.DataUtils.getBooleanValue;
import static com.app.eisenflow.utils.DataUtils.stringToIntegerCollection;
import static com.app.eisenflow.utils.TaskUtils.addProgressAction;
import static com.app.eisenflow.utils.TaskUtils.calculateProgress;
import static com.app.eisenflow.utils.TaskUtils.deleteTaskAction;
import static com.app.eisenflow.utils.TaskUtils.getFormattedProgress;
import static com.app.eisenflow.utils.TaskUtils.setTaskBackgroundByPriority;
import static com.app.eisenflow.utils.TaskUtils.shareTaskAction;
import static com.app.eisenflow.utils.TaskUtils.startTimerActivityAction;
import static com.app.eisenflow.utils.Utils.isTablet;

/**
 * Created on 2/10/18.
 */

public class PreviewActivity extends AppCompatActivity {
    @BindView(R.id.bottom_sheet_holder)
    ConstraintLayout mBottomSheetHolder;
    @BindView(R.id.bottom_sheet_title_holder_image)
    ImageView mTaskNameHolder;
    @BindView(R.id.bottom_sheet_task_name)
    TextView mTaskName;
    @BindView(R.id.bottom_sheet_date_txt) TextView mTaskDate;
    @BindView(R.id.bottom_sheet_time_txt) TextView mTaskTime;
    @BindView(R.id.bottom_sheet_reminder_holder) ConstraintLayout mReminderHolder;
    @BindView(R.id.bottom_sheet_occurrence_txt) TextView mTaskReminderOccurrence;
    @BindView(R.id.bottom_sheet_when_txt) TextView mTaskReminderWhen;
    @BindView(R.id.bottom_sheet_vibration_txt) TextView mVibrationValue;
    @BindView(R.id.bottom_sheet_note_txt) TextView mTaskNote;
    @BindView(R.id.bottom_sheet_progress_holder)
    FrameLayout mTaskProgressHolder;
    @BindView(R.id.bottom_sheet_progres_value) TextView mTaskProgressValue;
    @BindView(R.id.bottom_sheet_done_btn) ImageView mTaskDoneButton;
    @BindView(R.id.bottom_sheet_menu_btn) ImageView mTaskMenuButton;
    @BindView(R.id.bottom_sheet_edit_btn) ImageView mTaskEditButton;

    public static final String EXTRA_TRANSITION_NAME = "ExtraTransitionName";
    private Cursor mCursor;
    private static int mPriority = -1;
    private static int mTaskPosition = -1;
    private static boolean isDone;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.preview_layout);
        ButterKnife.bind(this);

        mTaskPosition = getIntent().getIntExtra(EXTRA_TASK_POSITION, -1);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCursor = getCursor();
        updateTaskDone();
        setTaskDetails();
    }

    @OnClick(R.id.bottom_sheet_done_btn)
    public void onDoneButtonClick()
    {
        // Toggle Done button.
        if (isDone) {
            isDone = false;
        } else {
            isDone = true;
        }
        updateDoneButton(isDone);
    }

    @OnClick (R.id.bottom_sheet_menu_btn)
    public void onMenuButtonClick() {
        showBottomSheetOverflowMenu();
    }

    @OnClick (R.id.bottom_sheet_edit_btn)
    public void onEditButtonClick() {
        if (mTaskPosition != -1) {
            Intent intent = new Intent(this, SingleTaskActivity.class);
            intent.putExtra(EXTRA_TASK_POSITION, mTaskPosition);
            startActivityWithTransition(intent);
            finish();
        }
    }

    private void startActivityWithTransition(Intent intent) {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        Bundle b;
        // Start activity with transition animation if Android version bigger or equal than Jelly Bean.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                mCursor != null && mCursor.moveToPosition(mTaskPosition) && !isTablet(this)) {

            View transitionView = findViewById(R.id.bottom_sheet_title_holder);

            // Use Task name to transition from MainActivity's list item to SingleTaskActivity.
            String transitionName = mCursor.getColumnName(mCursor.getColumnIndex(KEY_TITLE));
            // Set transition name to the view we want to transform.
            ViewCompat.setTransitionName(transitionView, transitionName);
            // Pass the transition name to next activity so we can set it there to the relevant view.
            intent.putExtra(EXTRA_TRANSITION_NAME, transitionName);

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(this, transitionView, transitionName);
            b = options.toBundle();
            startActivity(intent, b);
        }
        else {
            startActivity(intent);
        }
    }

    private void updateDoneButton(boolean isDone) {
        if (isDone) {
            mTaskDoneButton.setImageResource(R.drawable.done);
        } else {
            mTaskDoneButton.setImageResource(R.drawable.not_done);
        }
    }

    private void setTaskDetails() {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        if (mCursor != null && mCursor.moveToPosition(mTaskPosition)) {
            int priority = mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY));
            String title = mCursor.getString(mCursor.getColumnIndex(KEY_TITLE));
            String date = mCursor.getString(mCursor.getColumnIndex(KEY_DATE));
            String time = mCursor.getString(mCursor.getColumnIndex(KEY_TIME));
            int reminderOccurrence = mCursor.getInt(mCursor.getColumnIndex(KEY_REMINDER_OCCURRENCE));
            String reminderWhen = mCursor.getString(mCursor.getColumnIndex(KEY_REMINDER_WHEN));
            String note = mCursor.getString(mCursor.getColumnIndex(KEY_NOTE));
            int done = mCursor.getInt(mCursor.getColumnIndex(KEY_IS_DONE));
            int isVibrationEnabled = mCursor.getInt(mCursor.getColumnIndex(KEY_IS_VIBRATION_ENABLED));

            mPriority = priority;
            setTaskBackgroundByPriority(this, mTaskNameHolder, priority);
            mTaskName.setText(title);
            mTaskDate.setText(date);
            mTaskTime.setText(time);
            setReminderOccurrence(getReminderOccurrence(reminderOccurrence), priority);
            setReminderWhen(getReminderWhen(reminderWhen), reminderOccurrence);
            mVibrationValue.setText(isVibrating(isVibrationEnabled));
            mTaskNote.setText(note);
            isDone = DataUtils.getBooleanState(done);
            updateDoneButton(isDone);
            setTaskProgress();
        }
    }

    private void updateTaskProgress() {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        if (mCursor != null && mCursor.moveToPosition(mTaskPosition)) {
            setTaskProgress();
        }
    }

    private void setTaskProgress() {
        int priority = mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY));
        int progress = mCursor.getInt(mCursor.getColumnIndex(KEY_PROGRESS));
        double totalDays = mCursor.getDouble(mCursor.getColumnIndex(KEY_TOTAL_DAYS_PERIOD));

        DataUtils.Priority priorityType = DataUtils.Priority.valueOf(priority);
        if (priorityType == TWO) {
            mTaskProgressHolder.setVisibility(View.VISIBLE);
            mTaskProgressValue.setText(getFormattedProgress(calculateProgress(totalDays, progress)));
        } else {
            mTaskProgressHolder.setVisibility(View.INVISIBLE);
        }
    }

    private void setReminderOccurrence(String occurrence, int priority) {
        DataUtils.Priority priorityType = DataUtils.Priority.valueOf(priority);
        if (TextUtils.isEmpty(occurrence) || priorityType != TWO) {
            mReminderHolder.setVisibility(View.GONE);
        } else {
            mTaskReminderOccurrence.setText(occurrence);
            mReminderHolder.setVisibility(View.VISIBLE);
        }
    }

    private void setReminderWhen(String when, int occurrence) {
        DataUtils.Occurrence occurrenceType = DataUtils.Occurrence.getOccurrenceType(occurrence);

        if (TextUtils.isEmpty(when) || WEEKLY != occurrenceType) {
            mTaskReminderWhen.setVisibility(View.GONE);
        } else {
            mTaskReminderWhen.setText(when);
            mTaskReminderWhen.setVisibility(View.VISIBLE);
        }
    }

    private String getReminderOccurrence(int occurrence) {
        return DataUtils.Occurrence.valueOf(occurrence);
    }

    private String getReminderWhen(String when) {
        if (TextUtils.isEmpty(when)) {
            return null;
        }
        Set<Integer> whenValues = new HashSet<>(stringToIntegerCollection(when));
        StringBuilder sb = new StringBuilder();
        for (Integer i : whenValues) {
            String whenValue = DataUtils.When.valueOf(i);
            sb.append(whenValue + "\t\t");
        }
        return sb.toString();
    }

    private String isVibrating(int vibrating) {
        return getBooleanState(vibrating) ? getString(R.string.vibration_on) : getString(R.string.vibration_off);
    }

    private void showBottomSheetOverflowMenu() {
        PopupMenu popup = new PopupMenu(this, mTaskMenuButton);
        Menu menu = popup.getMenu();
        popup.getMenuInflater().inflate(R.menu.bottom_sheet_overflow_menu, popup.getMenu());

        if (mPriority != -1) {
            DataUtils.Priority priorityType = DataUtils.Priority.valueOf(mPriority);
            switch (priorityType) {
                case ONE:
                    menu.findItem(R.id.action_add_progress).setVisible(false);
                    menu.findItem(R.id.action_share).setVisible(false);
                    break;
                case TWO:
                    menu.findItem(R.id.action_start_timer).setVisible(false);
                    menu.findItem(R.id.action_share).setVisible(false);
                    break;
                case THREE:
                    menu.findItem(R.id.action_start_timer).setVisible(false);
                    menu.findItem(R.id.action_add_progress).setVisible(false);
                    break;
                case FOUR:
                    menu.findItem(R.id.action_start_timer).setVisible(false);
                    menu.findItem(R.id.action_add_progress).setVisible(false);
                    menu.findItem(R.id.action_share).setVisible(false);
                    break;
            }
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_start_timer:
                        startTimerActivityAction(PreviewActivity.this, mTaskPosition);
                        break;
                    case R.id.action_add_progress:
                        addProgressAction(mCursor, mTaskPosition);
                        mCursor = getCursor();
                        updateTaskProgress();
                        break;
                    case R.id.action_share:
                        shareTaskAction(mCursor, mTaskPosition);
                        break;
                    case R.id.action_delete:
                        deleteTaskAction(
                                mCursor.getInt(mCursor.getColumnIndex(KEY_ROW_ID)),
                                mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY)));
                        mCursor = null;
                        mTaskPosition = -1;
                        finish();
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private void updateTaskDone() {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(KEY_IS_DONE, getBooleanValue(isDone));

        if (mCursor != null && mCursor.moveToPosition(mTaskPosition)) {
            Task task = cursorToTask(mCursor);
            long taskId =  task.getId();
            Uri uri = buildFlavorsUri(taskId);
            getContentResolver().update(uri, values, null, null);

            // Cancel task's alarm if done, otherwise - reschedule.
            DataUtils.Priority priority = DataUtils.Priority.valueOf(task.getPriority());
            if (isDone) {
                if (priority == TWO) {
                    cancelRepeatingReminder((int)taskId);
                } else {
                    cancelReminder((int)taskId);
                }
            } else {
                if (priority == TWO) {
                    TaskReminderHelper.setRepeatingReminder(task);
                } else {
                    TaskReminderHelper.setReminder(task);
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        updateTaskDone();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateTaskDone();
    }
}
