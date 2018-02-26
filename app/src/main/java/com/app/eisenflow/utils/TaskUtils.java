package com.app.eisenflow.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.view.View;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.activities.TimerActivity;
import com.app.eisenflow.helpers.TaskReminderHelper;

import net.danlew.android.joda.DateUtils;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.format.PeriodFormatter;
import org.joda.time.format.PeriodFormatterBuilder;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Random;

import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PROGRESS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TOTAL_DAYS_PERIOD;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.helpers.TaskReminderHelper.cancelReminder;
import static com.app.eisenflow.helpers.TaskReminderHelper.cancelRepeatingReminder;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.DataUtils.getBooleanValue;

/**
 * Created on 12/31/17.
 */

public class TaskUtils {
    private static final String PRECISSION_FORMAT = "0.00";

    public static void startTimerActivityAction(Context context, int position) {
        Intent timerIntent = new Intent(context, TimerActivity.class);
        timerIntent.putExtra(EXTRA_TASK_POSITION, position);
        context.startActivity(timerIntent);
    }

    public static void addProgressAction(Cursor cursor, int position) {
        int progress = TaskUtils.getIncreasedTaskProgress(cursor, position);
        TaskUtils.updateProgress(cursor, position, progress);
    }

    public static void shareTaskAction(Cursor cursor, int position) {
        Context context = ApplicationEisenFlow.getAppContext();
        if (cursor != null && cursor.moveToPosition(position)) {
            Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
            sharingIntent.setType("text/plain");
            sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, getMessageToShare(cursor));
            context.startActivity(Intent.createChooser(
                    sharingIntent,
                    context.getResources().getString(R.string.share_task_title)
            ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        }
    }

    public static void deleteTaskAction(int taskId, int priorityValue) {
        Context context = ApplicationEisenFlow.getAppContext();
        Uri uri = buildFlavorsUri(taskId);
        context.getContentResolver().delete(uri, null, null);

        DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);
        if (priority == TWO) {
            cancelRepeatingReminder(taskId);
        } else {
            cancelReminder(taskId);
        }
    }

    public static void bulkDoneTasksDelete() {
        Context context = ApplicationEisenFlow.getAppContext();
        context.getContentResolver().delete(
                CONTENT_URI,
                KEY_IS_DONE+"=?",
                new String[]{getBooleanValue(true)+""});
    }

    private static String getMessageToShare(Cursor cursor) {
        Context context = ApplicationEisenFlow.getAppContext();
        String name = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
        String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        String note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));

        return String.format(context.getResources().getString(R.string.share_task_message),
                name, date, time, note);
    }

    public static double getTotalDays(String date) {
        DecimalFormat precision = new DecimalFormat(PRECISSION_FORMAT);
        Calendar calNow = Calendar.getInstance();
        Calendar calDate = Calendar.getInstance();
        calDate.setTime(DateTimeUtils.getDate(date));

        double diff = calDate.getTimeInMillis() - calNow.getTimeInMillis();
        return Double.valueOf(precision.format((diff / (24 * 60 * 60 * 1000)) + 1));
    }

    public static int getIncreasedTaskProgress(Cursor cursor, int position) {
        int progressToReturn = 1;
        if (cursor != null && cursor.moveToPosition(position)) {
            int progress = cursor.getInt(cursor.getColumnIndex(KEY_PROGRESS));
            progressToReturn += progress;
        }
        return progressToReturn;
    }

    public static void updateProgress(Cursor cursor, int position, int progress) {
        Context context = ApplicationEisenFlow.getAppContext();

        if (cursor != null && cursor.moveToPosition(position)) {
            int id = cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID));
            ContentValues values = new ContentValues();
            values.put(KEY_PROGRESS, progress);

            Uri uri = buildFlavorsUri(id);
            context.getContentResolver().update(uri, values, null, null);
        }
    }

    public static void setTaskBackgroundByPriority(Context context, View taskHolder, int priority) {
        DataUtils.Priority priorityValue = DataUtils.Priority.valueOf(priority);
        switch (priorityValue) {
            case ONE:
                taskHolder.setBackgroundColor(context.getResources().getColor(R.color.firstQuadrant));
                break;
            case TWO:
                taskHolder.setBackgroundColor(context.getResources().getColor(R.color.secondQuadrant));
                break;
            case THREE:
                taskHolder.setBackgroundColor(context.getResources().getColor(R.color.thirdQuadrant));
                break;
            case FOUR:
                taskHolder.setBackgroundColor(context.getResources().getColor(R.color.fourthQuadrant));
                break;
            case DEFAULT:
                taskHolder.setBackgroundColor(context.getResources().getColor(R.color.list_item_bg));
                break;
        }
    }

    public static int calculateProgress(Cursor cursor) {
        double totalDays = cursor.getDouble(cursor.getColumnIndex(KEY_TOTAL_DAYS_PERIOD));
        int progress = cursor.getInt(cursor.getColumnIndex(KEY_PROGRESS));

        return calculateProgress(totalDays, progress);
    }

    public static int calculateProgress(double totalDays, int progress) {
        int progressToReturn;
        double monthlyPercentage = 100 / totalDays;
        progressToReturn = (int) (Math.round(progress * monthlyPercentage));
        if (progress == totalDays || progressToReturn > 100) progressToReturn = 100;

        return progressToReturn;
    }

    public static String getFormattedProgress(int progress) {
        return String.valueOf(progress) + "%";
    }

    public static int generateRandomId() {
        Random r = new Random();
        return r.nextInt(100 - 1) + 1;
    }

    public static String getTimeLeft(Cursor cursor) {
        String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
        String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
        Calendar cal = DateTimeUtils.getCalendar(date, time);

        DateTime startDate = DateTime.now();
        DateTime endDate = new DateTime(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH), 0, 0);

        Period period = new Period(startDate, endDate);

        if(period.getDays() < 0) {
            return "Overdue";
        }
        else if(DateUtils.isToday(endDate.toLocalDate())) {
            return "Due Today";
        }
        else if(period.getDays() == 0) {
            return "Due Tomorrow";
        }
        else {
            PeriodFormatter formatter = new PeriodFormatterBuilder()
                    .appendYears().appendSuffix(" year ", " years ")
                    .appendMonths().appendSuffix(" month ", " months ")
                    .appendWeeks().appendSuffix(" week ", " weeks ")
                    .toFormatter();

            int days = period.getDays()+1;
            String daysSuffix = " days ";
            if(days == 1) daysSuffix = " day ";

            return "Due in " + formatter.print(period) + days + daysSuffix;
        }
    }

    public static void updateTaskDoneState(Context context, Cursor cursor, boolean isDone) {
            ContentValues values = new ContentValues();
            values.put(KEY_IS_DONE, getBooleanValue(isDone));

            Task task = cursorToTask(cursor);
            long taskId = task.getId();
            Uri uri = buildFlavorsUri(taskId);
            context.getContentResolver().update(uri, values, null, null);

            // Cancel task's alarm if done, otherwise - reschedule.
            DataUtils.Priority priority = DataUtils.Priority.valueOf(task.getPriority());
            if (isDone) {
                if (priority == TWO) {
                    cancelRepeatingReminder((int) taskId);
                } else {
                    cancelReminder((int) taskId);
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
