package com.app.eisenflow;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.app.eisenflow.activities.SingleTaskActivity;
import com.app.eisenflow.utils.DataUtils;

import java.util.ArrayList;
import java.util.List;

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
import static com.app.eisenflow.utils.DataUtils.Occurrence.WEEKLY;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.DataUtils.getBooleanValue;
import static com.app.eisenflow.utils.DataUtils.stringToIntegerCollection;
import static com.app.eisenflow.utils.Statics.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Statics.TAG;
import static com.app.eisenflow.utils.TaskUtils.addProgressAction;
import static com.app.eisenflow.utils.TaskUtils.calculateProgress;
import static com.app.eisenflow.utils.TaskUtils.deleteTaskAction;
import static com.app.eisenflow.utils.TaskUtils.getFormattedProgress;
import static com.app.eisenflow.utils.TaskUtils.setTaskBackgroundByPriority;
import static com.app.eisenflow.utils.TaskUtils.shareTaskAction;
import static com.app.eisenflow.utils.TaskUtils.startTimerActivityAction;

/**
 * Created on 1/15/18.
 */

public class EisenBottomSheet {
    @BindView(R.id.bottom_sheet_holder) ConstraintLayout mBottomSheetHolder;
    @BindView(R.id.bottom_sheet_title_holder_image) ImageView mTaskNameHolder;
    @BindView(R.id.bottom_sheet_task_name) TextView mTaskName;
    @BindView(R.id.bottom_sheet_date_txt) TextView mTaskDate;
    @BindView(R.id.bottom_sheet_time_txt) TextView mTaskTime;
    @BindView(R.id.bottom_sheet_reminder_holder) ConstraintLayout mReminderHolder;
    @BindView(R.id.bottom_sheet_occurrence_txt) TextView mTaskReminderOccurrence;
    @BindView(R.id.bottom_sheet_when_txt) TextView mTaskReminderWhen;
    @BindView(R.id.bottom_sheet_vibration_txt) TextView mVibrationValue;
    @BindView(R.id.bottom_sheet_note_txt) TextView mTaskNote;
    @BindView(R.id.bottom_sheet_progress_holder) FrameLayout mTaskProgressHolder;
    @BindView(R.id.bottom_sheet_progres_value) TextView mTaskProgressValue;
    @BindView(R.id.bottom_sheet_done_btn) ImageView mTaskDoneButton;
    @BindView(R.id.bottom_sheet_menu_btn) ImageView mTaskMenuButton;
    @BindView(R.id.bottom_sheet_edit_btn) ImageView mTaskEditButton;

    public static final String EXTRA_TRANSITION_NAME = "ExtraTransitionName";
    private Activity mActivity;
    private Cursor mCursor;
    private static int mPriority = -1;
    private static int mTaskPosition = -1;
    private BottomSheetBehavior mBottomSheetBehavior;
    private static boolean isDone;

    public EisenBottomSheet(Activity activity) {
        mActivity = activity;
        ButterKnife.bind(this, this.mActivity);
        mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheetHolder);
        mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        mBottomSheetBehavior.setPeekHeight(0);
        setCallbacks();
    }

    public void openBottomSheet(int position) {
        if (mTaskPosition != position) {
            updateTaskDone();
        }
        mTaskPosition = position;
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        setTaskDetails();
    }

    public void closeBottomSheet() {
        if (mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED) {
            mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    public boolean isBottomSheetExpanded() {
        return mBottomSheetBehavior.getState() != BottomSheetBehavior.STATE_COLLAPSED;
    }

    @OnClick (R.id.bottom_sheet_done_btn)
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
            Intent intent = new Intent(mActivity, SingleTaskActivity.class);
            intent.putExtra(EXTRA_TASK_POSITION, mTaskPosition);
            startActivityWithTransition(intent);
        }
    }

    private void startActivityWithTransition(Intent intent) {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        Bundle b;
        // Start activity with transition animation if Android version bigger or equal than Jelly Bean.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN &&
                mCursor != null && mCursor.moveToPosition(mTaskPosition)) {

            View transitionView = mActivity.findViewById(R.id.bottom_sheet_title_holder);

            // Use Task name to transition from MainActivity's list item to SingleTaskActivity.
            String transitionName = mCursor.getColumnName(mCursor.getColumnIndex(KEY_TITLE));
            // Set transition name to the view we want to transform.
            ViewCompat.setTransitionName(transitionView, transitionName);
            // Pass the transition name to next activity so we can set it there to the relevant view.
            intent.putExtra(EXTRA_TRANSITION_NAME, transitionName);

            ActivityOptionsCompat options = ActivityOptionsCompat.
                    makeSceneTransitionAnimation(mActivity, transitionView, transitionName);
            b = options.toBundle();
            mActivity.startActivity(intent, b);
        }
        else {
            mActivity.startActivity(intent);
        }
    }

    private void updateDoneButton(boolean isDone) {
        if (isDone) {
            mTaskDoneButton.setImageResource(R.drawable.done);
        } else {
            mTaskDoneButton.setImageResource(R.drawable.not_done);
        }
    }

    private void setCallbacks() {
        if (mBottomSheetBehavior == null) {
            return;
        }

        mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        updateTaskDone();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
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
            double totalDays = mCursor.getDouble(mCursor.getColumnIndex(KEY_TOTAL_DAYS_PERIOD));
            int progress = mCursor.getInt(mCursor.getColumnIndex(KEY_PROGRESS));
            int done = mCursor.getInt(mCursor.getColumnIndex(KEY_IS_DONE));
            int isVibrationEnabled = mCursor.getInt(mCursor.getColumnIndex(KEY_IS_VIBRATION_ENABLED));

            mPriority = priority;
            setTaskBackgroundByPriority(mActivity, mTaskNameHolder, priority);
            mTaskName.setText(title);
            mTaskDate.setText(date);
            mTaskTime.setText(time);
            setReminderOccurrence(getReminderOccurrence(reminderOccurrence), priority);
            setReminderWhen(getReminderWhen(reminderWhen), reminderOccurrence);
            mVibrationValue.setText(isVibrating(isVibrationEnabled));
            mTaskNote.setText(note);
            isDone = DataUtils.getBooleanState(done);
            updateDoneButton(isDone);

            DataUtils.Priority priorityType = DataUtils.Priority.valueOf(priority);
            if (priorityType == DataUtils.Priority.TWO) {
                mTaskProgressHolder.setVisibility(View.VISIBLE);
                mTaskProgressValue.setText(getFormattedProgress(calculateProgress(totalDays, progress)));
            } else {
                mTaskProgressHolder.setVisibility(View.INVISIBLE);
            }
        }
    }

    private void setReminderOccurrence(String occurrence, int priority) {
        DataUtils.Priority priorityType = DataUtils.Priority.valueOf(priority);
        if (TextUtils.isEmpty(occurrence) || priorityType != DataUtils.Priority.TWO) {
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
        List<Integer> whenValues = new ArrayList<>(stringToIntegerCollection(when));
        StringBuilder sb = new StringBuilder();
        for (Integer i : whenValues) {
            String whenValue = DataUtils.When.valueOf(i);
            sb.append(whenValue + "\t\t");
        }
        return sb.toString();
    }

    private String isVibrating(int vibrating) {
        return getBooleanState(vibrating) ? mActivity.getString(R.string.vibration_on) : mActivity.getString(R.string.vibration_off);
    }

    private void showBottomSheetOverflowMenu() {
        PopupMenu popup = new PopupMenu(mActivity, mTaskMenuButton);
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
                        startTimerActivityAction(mActivity, mTaskPosition);
                        break;
                    case R.id.action_add_progress:
                        addProgressAction(mCursor, mTaskPosition);
                        break;
                    case R.id.action_share:
                        shareTaskAction(mCursor, mTaskPosition);
                        break;
                    case R.id.action_delete:
                        deleteTaskAction(mCursor.getInt(mCursor.getColumnIndex(KEY_ROW_ID)));
                        closeBottomSheet();
                        mCursor = null;
                        mTaskPosition = -1;
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    public void updateCursor(Cursor cursor) {
        mCursor = cursor;
        setTaskDetails();
    }

    private void updateTaskDone() {
        if (mCursor == null || mTaskPosition == -1) {
            return;
        }
        ContentValues values = new ContentValues();
        values.put(KEY_IS_DONE, getBooleanValue(isDone));

        if (mCursor != null && mCursor.moveToPosition(mTaskPosition)) {
            long taskId =  mCursor.getInt(mCursor.getColumnIndex(KEY_ROW_ID));
            Uri uri = buildFlavorsUri(taskId);
            mActivity.getContentResolver().update(uri, values, null, null);
        }
    }
}
