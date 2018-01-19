package com.app.eisenflow.utils;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;

import java.text.DecimalFormat;
import java.util.Calendar;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PROGRESS;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TOTAL_DAYS_PERIOD;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;

/**
 * Created on 12/31/17.
 */

public class TaskUtils {
    private static final String PRECISSION_FORMAT = "0.00";

    public static void deleteTask(int taskId) {
        Context context = ApplicationEisenFlow.getAppContext();
        Uri uri = buildFlavorsUri(taskId);
        context.getContentResolver().delete(uri, null, null);
    }

    public static void shareTask(Cursor cursor, int position) {
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
}
