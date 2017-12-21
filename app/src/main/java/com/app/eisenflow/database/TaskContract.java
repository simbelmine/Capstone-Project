package com.app.eisenflow.database;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created on 12/21/17.
 */

public class TaskContract {
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
        public static final String KEY_REMINDER_DATE = "reminderDate";
        public static final String KEY_REMINDER_TIME = "reminderTime";
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
    }
}


