package com.app.eisenflow.helpers;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.app.eisenflow.R;
import com.app.eisenflow.utils.DataUtils;
import com.app.eisenflow.utils.DateTimeUtils;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.utils.Constants.MAX_PROGRESS;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.TaskUtils.calculateProgress;
import static com.app.eisenflow.utils.TaskUtils.getFormattedProgress;
import static com.app.eisenflow.utils.TaskUtils.getTimeLeft;
import static com.app.eisenflow.utils.TaskUtils.isTaskDone;
import static com.app.eisenflow.utils.TaskUtils.setTaskBackgroundByPriority;

/**
 * Created on 12/25/17.
 */

public class TasksViewHolder extends RecyclerView.ViewHolder {
    @BindView(R.id.card_view) FrameLayout mCardView;
    @BindView(R.id.task_card_mainview) RelativeLayout mTaskHolder;
    @BindView(R.id.task_title) TextView mTaskTitle;
    @BindView(R.id.task_time_txt) TextView mTaskTime;
    @BindView(R.id.right_action_icon) ImageView mRightActionIcon;
    @BindView(R.id.card_day_of_month) TextView mDayOfMonth;
    @BindView(R.id.card_day_of_week) TextView mDayOfWeek;
    @BindView(R.id.task_p1_percentage) TextView mTaskProgress;
    @BindView(R.id.done_cross_line) ImageView mDoneCrossLine;
    @BindView(R.id.delete_action_layout) RelativeLayout mDeleteActionLayout;
    @BindView(R.id.action_delete_icon) ImageView mDeleteActionIcon;
    @BindView(R.id.undo_layout) RelativeLayout mUndoLayout;
    @BindView(R.id.undo_btn) TextView mUndoButton;
    @BindView(R.id.action_undo_btn) TextView mUndoActionBtn;
    @BindView(R.id.month_name) TextView mMonthName;

    private Activity mContext;
    private TasksCursorRecyclerViewAdapter mAdapter;
    private Cursor mCursor;

    public TasksViewHolder(Activity context, TasksCursorRecyclerViewAdapter adapter, View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
        this.mContext = context;
        this.mAdapter = adapter;
        itemView.setOnTouchListener(new RecyclerItemSwipeDetector(mContext, this));
    }

    public void setData(Cursor cursor) {
        if (cursor != null) {
            mCursor = cursor;

            // Task Row.
            setTaskDetails(cursor);
            // Month Row.
            setMonthDetails(cursor);
        }
    }

    public Cursor getHolderCursor() {
        return mCursor;
    }

    public TasksCursorRecyclerViewAdapter getAdapter() {
        return mAdapter;
    }

    private void setTaskDetails(Cursor cursor) {
        String taskTitle = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
        mTaskTitle.setText(taskTitle);
        mTaskTime.setText(cursor.getString(cursor.getColumnIndex(KEY_TIME)));

        // Set 'Priority' connected resources.
        int priority = cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY));
        setTaskBackgroundByPriority(mContext, mTaskHolder, priority);
        setPriorityActionIcon(cursor, priority);
        // Set time left.
        mTaskTime.setText(getTimeLeft(cursor));
        // Set date to task list item.
        setVerticalCalendarDate(cursor);
        // Set task progress.
        setTaskProgress(cursor);
        // Cross task if done.
        crossTaskIfDone(cursor);
        // Set different text color if task is overdue.
        setTaskOverdue(cursor);

        mTaskTime.setVisibility(View.VISIBLE);
    }

    private void setPriorityActionIcon(Cursor cursor, int priorityValue) {
        DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);
        switch (priority) {
            case ONE:
                mRightActionIcon.setImageResource(R.drawable.timer);
                mRightActionIcon.setTag(priorityValue);
                break;
            case TWO:
                int reminderOccurrence = cursor.getInt(cursor.getColumnIndex(KEY_REMINDER_OCCURRENCE));
                if(reminderOccurrence != -1) {
                    mRightActionIcon.setImageResource(R.drawable.calendar_plus);
                    mRightActionIcon.setTag(priorityValue);
                }
                break;
            case THREE:
                mRightActionIcon.setImageResource(R.drawable.share);
                mRightActionIcon.setTag(priorityValue);
                break;
        }
    }

    private void setVerticalCalendarDate(Cursor cursor) {
        Calendar cal = getCalendarDateFromCursor(cursor);

        String dateMonthStr = String.valueOf(cal.get(Calendar.DAY_OF_MONTH)) + ", " + String.valueOf(cal.get(Calendar.MONTH));

        if (!mAdapter.mDateHeaderMap.containsKey(dateMonthStr)) {
            mAdapter.mDateHeaderMap.put(dateMonthStr, cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID)));
        }

        int currTaskId = cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID));
        int mapTaskId = mAdapter.mDateHeaderMap.get(dateMonthStr);

        if (mapTaskId == currTaskId) {
            mDayOfMonth.setText(String.valueOf(cal.get(Calendar.DAY_OF_MONTH)));
            mDayOfWeek.setText(cal.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.getDefault()));
        } else {
            mDayOfMonth.setText("");
            mDayOfWeek.setText("");
        }
    }

    private void setTaskProgress(Cursor cursor) {
        int priorityValue = cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY));
        DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);
        int occurrence = cursor.getInt(cursor.getColumnIndex(KEY_REMINDER_OCCURRENCE));

        if(priority == TWO) {
            mTaskProgress.setVisibility(View.VISIBLE);
            int currProgress = 0;
            if(occurrence != -1) {
                currProgress = calculateProgress(cursor);
            }

            if (currProgress >= MAX_PROGRESS) {
                mTaskProgress.setText(getFormattedProgress(MAX_PROGRESS));
            } else {
                mTaskProgress.setText(getFormattedProgress(currProgress));
            }
        }
        else {
            mTaskProgress.setVisibility(View.INVISIBLE);
        }
    }

    private void crossTaskIfDone(Cursor cursor) {
        if(isTaskDone(cursor)) {
            mDoneCrossLine.setVisibility(View.VISIBLE);
            mDoneCrossLine.getLayoutParams().width = DataUtils.getTaskTextWidth(mTaskTitle);
        }
        else {
            mDoneCrossLine.setVisibility(View.GONE);
        }
    }

    private void setTaskOverdue(Cursor cursor) {
        String taskDate = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String taskTime= cursor.getString(cursor.getColumnIndex(KEY_TIME));
        Calendar calDate = DateTimeUtils.getCalendar(taskDate, taskTime);

        if(DateTimeUtils.isPastDate(calDate) && !isTaskDone(cursor)) {
            mDayOfMonth.setTextColor(mContext.getResources().getColor(R.color.firstQuadrant));
            mDayOfWeek.setTextColor(mContext.getResources().getColor(R.color.firstQuadrant));
        } else {
            mDayOfMonth.setTextColor(mContext.getResources().getColor(R.color.date));
            mDayOfWeek.setTextColor(mContext.getResources().getColor(R.color.date));
        }
    }

    private void setMonthDetails(Cursor cursor) {
        String monthName = getMonthName(cursor);
        Calendar cal = getCalendarDateFromCursor(cursor);
        String monthYearStr = String.valueOf(cal.get(Calendar.MONTH)) + ", " + String.valueOf(cal.get(Calendar.YEAR));

        if (!mAdapter.mMonthHeaderMap.containsKey(monthYearStr)) {
            mAdapter.mMonthHeaderMap.put(monthYearStr, cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID)));
        }

        int currTaskId = cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID));
        int mapTaskId = mAdapter.mMonthHeaderMap.get(monthYearStr);

        if (mapTaskId == currTaskId) {
            mMonthName.setText(monthName);
            mMonthName.setVisibility(View.VISIBLE);
            // Set Current Date Row.
            updateCurrantDateRow(cursor);
        } else {
            mMonthName.setVisibility(View.GONE);
        }
    }

    private Calendar getCalendarDateFromCursor(Cursor cursor) {
        String taskDate = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String taskTime = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        String taskActualTime = DateTimeUtils.getActualTime(taskTime);
        return DateTimeUtils.getCalendar(taskDate, taskActualTime);
    }

    private String getMonthName(Cursor cursor) {
        String monthStr = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        Date date = DateTimeUtils.getDate(monthStr);
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.setTime(date);
        return DateTimeUtils.getMonthName(calendarDate);
    }

    private void updateCurrantDateRow(Cursor cursor) {
        String taskDate = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String taskTime= cursor.getString(cursor.getColumnIndex(KEY_TIME));
        Calendar rowDate = DateTimeUtils.getCalendar(taskDate, taskTime);
        Calendar now = Calendar.getInstance();

        if (rowDate.get(Calendar.MONTH) == now.get(Calendar.MONTH)) {
            mAdapter.setCurrentDateRow(getAdapterPosition());
        }
    }
}
