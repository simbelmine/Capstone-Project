package com.app.eisenflow.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import static com.app.eisenflow.database.TaskContract.AUTHORITY;
import static com.app.eisenflow.database.TaskContract.TaskEntry.CONTENT_DIR_TYPE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.CONTENT_ITEM_TYPE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.TaskContract.TaskEntry.TABLE_NAME;
import static com.app.eisenflow.database.TaskContract.TaskEntry.buildFlavorsUri;

/**
 * Created on 12/21/17.
 */

public class EisenContentProvider extends ContentProvider {
    private static final int TASK = 100;
    private static final int TASK_ID = 101;

    private DatabaseHelper mDatabaseHelper;
    private static UriMatcher sUriMatcher = buildUriMatcher();
    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);

        // See if matches task records.
        matcher.addURI(AUTHORITY, TABLE_NAME, TASK);
        // See if matches specific task.
        matcher.addURI(AUTHORITY, TABLE_NAME + "/#", TASK_ID);

        return matcher;
    }

    public EisenContentProvider() {
    }

    @Override
    public boolean onCreate() {
        Log.v("eisen", "Provider -> onCreate()");
        mDatabaseHelper = DatabaseHelper.getInstance(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case TASK:
                return CONTENT_DIR_TYPE;
            case TASK_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI : "+ uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.v("eisen", "URI --> " + uri);

        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case TASK:
                long _id = db.insert(TABLE_NAME, null, values);
                // Insert unless it is already contained in the database.
                if (_id > 0) {
                    returnUri = buildFlavorsUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into: " + uri);
                }
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        final SQLiteDatabase db = mDatabaseHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case TASK:
                retCursor = db.query(
                        TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                return retCursor;
            case TASK_ID:
                retCursor = db.query(
                        TABLE_NAME,
                        projection,
                        KEY_ROW_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                return retCursor;
            default:
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numUpdated = 0;

        if (values == null){
            throw new IllegalArgumentException("Cannot have null content values");
        }

        switch (match) {
            case TASK:
                numUpdated = db.update(
                        TABLE_NAME,
                        values,
                        selection,
                        selectionArgs);
                break;
            case TASK_ID:
                numUpdated = db.update(
                        TABLE_NAME,
                        values,
                        KEY_ROW_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (numUpdated > 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return numUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mDatabaseHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int numDeleted = 0;

        Log.v("eisen", "URI Delte = " + uri);

        switch (match) {
            case TASK:
                numDeleted = db.delete(
                        TABLE_NAME,
                        selection,
                        selectionArgs
                );
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        TABLE_NAME + "'");
                break;
            case TASK_ID:
                numDeleted = db.delete(
                        TABLE_NAME,
                        KEY_ROW_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                // reset _ID
                db.execSQL("DELETE FROM SQLITE_SEQUENCE WHERE NAME = '" +
                        TABLE_NAME + "'");
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return numDeleted;
    }
}
