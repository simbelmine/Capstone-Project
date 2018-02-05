package com.app.eisenflow.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE_MILLIS;
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
import static com.app.eisenflow.database.EisenContract.TaskEntry.TABLE_NAME;

import static com.app.eisenflow.utils.Constants.TAG;

/**
 * Created on 12/20/17.
 */

public class EisenDatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "EisenFlowDb";

    private final Context context;
    private static EisenDatabaseHelper sSingletonHelper;
    public static EisenDatabaseHelper getInstance(final Context context) {
        if(sSingletonHelper == null) {
            sSingletonHelper = new EisenDatabaseHelper(context);
        }
        return sSingletonHelper;
    }

    private EisenDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context.getApplicationContext();
    }

    public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "("
                    + KEY_ROW_ID + " integer primary key autoincrement, "
                    + KEY_PRIORITY + " integer, "
                    + KEY_TITLE + " text not null, "
                    + KEY_DATE + " text not null, "
                    + KEY_TIME + " text not null, "
                    + KEY_DATE_MILLIS + " integer, "
                    + KEY_REMINDER_OCCURRENCE + " integer, "
                    + KEY_REMINDER_WHEN + " text not null, "
                    + KEY_NOTE + " text, "
                    + KEY_PROGRESS + " integer default 0, "
                    + KEY_IS_DONE + " integer default 0, "
                    + KEY_TOTAL_DAYS_PERIOD + " real, "
                    + KEY_IS_VIBRATION_ENABLED + " integer default 1);";

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "---Database " + DATABASE_NAME + " was created.");
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
