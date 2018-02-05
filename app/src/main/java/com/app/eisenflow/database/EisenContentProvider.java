package com.app.eisenflow.database;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import static com.app.eisenflow.database.EisenContract.AUTHORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_DIR_TYPE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_ITEM_TYPE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.EisenContract.TaskEntry.ORDER_BY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.TABLE_NAME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.buildFlavorsUri;
import static com.app.eisenflow.utils.Constants.TASK;
import static com.app.eisenflow.utils.Constants.TASK_ID;

/**
 * Created on 12/21/17.
 */

public class EisenContentProvider extends ContentProvider {
    private EisenDatabaseHelper mDatabaseHelper;
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
        mDatabaseHelper = EisenDatabaseHelper.getInstance(getContext());
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
                        ORDER_BY);
                break;
            case TASK_ID:
                retCursor = db.query(
                        TABLE_NAME,
                        projection,
                        KEY_ROW_ID + " = ?",
                        new String[] {String.valueOf(ContentUris.parseId(uri))},
                        null,
                        null,
                        sortOrder);
                break;
            default:
                // By default, we assume a bad URI
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
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
        int rowsDeleted = 0;

        switch (match) {
            case TASK:
                rowsDeleted = db.delete(
                        TABLE_NAME,
                        selection,
                        selectionArgs
                );
                break;
            case TASK_ID:
                rowsDeleted = db.delete(
                        TABLE_NAME,
                        KEY_ROW_ID + " = ?",
                        new String[]{String.valueOf(ContentUris.parseId(uri))});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        };

        if (selection == null || rowsDeleted != 0)
        {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsDeleted;
    }
}
