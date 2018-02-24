package com.app.eisenflow.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.app.eisenflow.R;
import com.app.eisenflow.utils.DataUtils;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Constants.MAX_PROGRESS;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.utils.Constants.WIDGET_DONE_ACTION;
import static com.app.eisenflow.utils.Constants.WIDGET_TO_TASK_ACTION;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.TaskUtils.calculateProgress;
import static com.app.eisenflow.utils.TaskUtils.getFormattedProgress;
import static com.app.eisenflow.utils.TaskUtils.getTimeLeft;

/**
 * Created on 2/19/18.
 */

public class ListProvider implements RemoteViewsService.RemoteViewsFactory {
    private Context mContext;
    private int mWidgetId;
    private Cursor mCursor;

    public ListProvider(Context context, Intent intent) {
        mContext = context;
        mWidgetId = intent.getIntExtra(
                AppWidgetManager.EXTRA_APPWIDGET_ID,
                AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int getCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        if (position == AdapterView.INVALID_POSITION ||
                mCursor == null || !mCursor.moveToPosition(position)) {
            return null;
        }
        final RemoteViews remoteView = new RemoteViews(
                mContext.getPackageName(), R.layout.widget_list_item);
        int priorityValue = mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY));
        String taskTitle = mCursor.getString(mCursor.getColumnIndex(KEY_TITLE));
        String timeLeft = getTimeLeft(mCursor);

        remoteView.setTextViewText(R.id.widget_task_title, taskTitle);
        remoteView.setTextViewText(R.id.widget_task_due_date, timeLeft);
        setTaskProgress(remoteView);
        setTextBackground(remoteView, priorityValue);
        updateDoneButtons(mCursor, remoteView);

        // Accept clicks on each list item.
        Intent fillInIntent = new Intent();
        fillInIntent.putExtra(EXTRA_TASK_POSITION, position);
        fillInIntent.setAction(WIDGET_TO_TASK_ACTION);
        remoteView.setOnClickFillInIntent(R.id.widget_task_details_container, fillInIntent);

        // Accept clicks on list item done image button.
        Intent fillInDoneIntent = new Intent();
        fillInDoneIntent.putExtra(EXTRA_TASK_POSITION, position);
        fillInDoneIntent.setAction(WIDGET_DONE_ACTION);
        remoteView.setOnClickFillInIntent(R.id.widget_done_check_box, fillInDoneIntent);

        return remoteView;
    }

    @Override
    public long getItemId(int position) {
        return mCursor.moveToPosition(position) ? mCursor.getLong(0) : position;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDataSetChanged() {
        Log.v(TAG, "onDataSetChanged");
        mCursor = getCursor();
    }

    @Override
    public void onDestroy() {

    }

    private void setTaskProgress(RemoteViews remoteViews) {
        int priorityValue = mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY));
        DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);
        int occurrence = mCursor.getInt(mCursor.getColumnIndex(KEY_REMINDER_OCCURRENCE));

        if(priority == TWO) {
            remoteViews.setViewVisibility(R.id.widget_task_progress, View.VISIBLE);
            int currProgress = 0;
            if(occurrence != -1) {
                currProgress = calculateProgress(mCursor);
            }

            if (currProgress >= MAX_PROGRESS) {
                remoteViews.setTextViewText(R.id.widget_task_progress, getFormattedProgress(MAX_PROGRESS));
            } else {
                remoteViews.setTextViewText(R.id.widget_task_progress, getFormattedProgress(currProgress));
            }
        }
        else {
            remoteViews.setViewVisibility(R.id.widget_task_progress, View.INVISIBLE);
        }
    }

    private void setTextBackground(RemoteViews remoteView, int priorityValue) {
        DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);
        switch (priority) {
            case ONE:
                remoteView.setTextColor(
                        R.id.widget_task_title,
                        mContext.getResources().getColor(R.color.firstQuadrant));
                break;
            case TWO:
                remoteView.setTextColor(
                        R.id.widget_task_title,
                        mContext.getResources().getColor(R.color.secondQuadrant));
                break;
            case THREE:
                remoteView.setTextColor(
                        R.id.widget_task_title,
                        mContext.getResources().getColor(R.color.thirdQuadrant));
                break;
            case FOUR:
                remoteView.setTextColor(
                        R.id.widget_task_title,
                        mContext.getResources().getColor(R.color.fourthQuadrant));
                break;

        }
    }

    private void updateDoneButtons(Cursor cursor, RemoteViews remoteView) {
        int isDoneValue = cursor.getInt(cursor.getColumnIndex(KEY_IS_DONE));
        boolean isDone = getBooleanState(isDoneValue);

        if (isDone) {
            remoteView.setImageViewResource(R.id.widget_done_check_box, R.mipmap.done_dark);
            remoteView.setViewVisibility(R.id.widget_done_cross_line, View.VISIBLE);
        } else {
            remoteView.setImageViewResource(R.id.widget_done_check_box, R.mipmap.not_done_dark);
            remoteView.setViewVisibility(R.id.widget_done_cross_line, View.INVISIBLE);
        }
    }
}
