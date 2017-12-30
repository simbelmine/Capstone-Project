package com.app.eisenflow.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;

import com.app.eisenflow.Task;

/**
 * Created on 12/21/17.
 */

public class EisenContract {
    public static final String AUTHORITY = "com.app.eisenflow.provider";
    public static final String SCHEME = "content://";
    public static final Uri BASE_CONTENT_URI = Uri.parse(SCHEME+AUTHORITY);

    public static final class TaskEntry implements BaseColumns {
        // Table name.
        public static final String TABLE_NAME = "Task";
        // Columns.
        public static final String KEY_ROW_ID = "_id";
        public static final String KEY_PRIORITY = "priority";
        public static final String KEY_TITLE = "title";
        public static final String KEY_DATE = "date";
        public static final String KEY_DATE_MILLIS = "calDate";
        public static final String KEY_TIME = "time";
        public static final String KEY_REMINDER_OCCURRENCE = "reminderOccurrence";
        public static final String KEY_REMINDER_WHEN = "reminderWhen";
        public static final String KEY_NOTE = "note";
        public static final String KEY_PROGRESS = "progress";
        public static final String KEY_IS_DONE = "done";
        public static final String KEY_TOTAL_DAYS_PERIOD = "total";
        public static final String KEY_IS_VIBRATION_ENABLED = "isVibrationChecked";

        // Content Uri.
        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon()
                .appendPath(TABLE_NAME).build();
        // Create cursor of base type directory for multiple entries.
        public static final String CONTENT_DIR_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + AUTHORITY + "/" + TABLE_NAME;
        // Create cursor of base type item for single entry.
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE +"/" + AUTHORITY + "/" + TABLE_NAME;

        // Building URIs on insertion.
        public static Uri buildFlavorsUri(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String[] Columns = new String[]{
                KEY_ROW_ID, KEY_PRIORITY, KEY_TITLE, KEY_DATE,
                KEY_DATE_MILLIS, KEY_TIME, KEY_REMINDER_OCCURRENCE, KEY_REMINDER_WHEN, KEY_NOTE,
                KEY_PROGRESS, KEY_IS_DONE, KEY_TOTAL_DAYS_PERIOD, KEY_IS_VIBRATION_ENABLED
        };

        public static final String ORDER_BY =  KEY_DATE_MILLIS + " ASC";

        public static Object[] getDataRow(Cursor cursor) {
            return new Object[]{
                    cursor.getString(cursor.getColumnIndex(KEY_ROW_ID)),
                    cursor.getString(cursor.getColumnIndex(KEY_PRIORITY)),
                    cursor.getString(cursor.getColumnIndex(KEY_TITLE)),
                    cursor.getString(cursor.getColumnIndex(KEY_DATE)),
                    cursor.getString(cursor.getColumnIndex(KEY_DATE_MILLIS)),
                    cursor.getString(cursor.getColumnIndex(KEY_TIME)),
                    cursor.getString(cursor.getColumnIndex(KEY_REMINDER_OCCURRENCE)),
                    cursor.getString(cursor.getColumnIndex(KEY_REMINDER_WHEN)),
                    cursor.getString(cursor.getColumnIndex(KEY_NOTE)),
                    cursor.getString(cursor.getColumnIndex(KEY_PROGRESS)),
                    cursor.getString(cursor.getColumnIndex(KEY_IS_DONE)),
                    cursor.getString(cursor.getColumnIndex(KEY_TOTAL_DAYS_PERIOD)),
                    cursor.getString(cursor.getColumnIndex(KEY_IS_VIBRATION_ENABLED))
            };
        }

        public static Task cursorToTask(Cursor cursor) {
            Task task = new Task();
            int taskId = cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID));
            int priority = cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY));
            String title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
            int dateMillis = cursor.getInt(cursor.getColumnIndex(KEY_DATE_MILLIS));
            int reminderOccurrence = cursor.getInt(cursor.getColumnIndex(KEY_REMINDER_OCCURRENCE));
            String reminderWhen = cursor.getString(cursor.getColumnIndex(KEY_REMINDER_WHEN));
            String note = cursor.getString(cursor.getColumnIndex(KEY_NOTE));
            int progress = cursor.getInt(cursor.getColumnIndex(KEY_PROGRESS));
            int isDone = cursor.getInt(cursor.getColumnIndex(KEY_IS_DONE));
            double totalDaysPeriod = cursor.getDouble(cursor.getColumnIndex(KEY_TOTAL_DAYS_PERIOD));
            int isVibrationEnabled = cursor.getInt(cursor.getColumnIndex(KEY_IS_VIBRATION_ENABLED));

            task.setId(taskId);
            task.setPriority(priority);
            task.setTitle(title);
            task.setDate(date);
            task.setTime(time);
            task.setDateMillis(dateMillis);
            task.setReminderOccurrence(reminderOccurrence);
            task.setReminderWhen(reminderWhen);
            task.setNote(note);
            task.setProgress(progress);
            task.setIsDone(isDone);
            task.setTotalDaysPeriod(totalDaysPeriod);
            task.setVibrationEnabled(isVibrationEnabled);

            return task;
        }
    }
}


