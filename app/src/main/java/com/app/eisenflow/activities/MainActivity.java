package com.app.eisenflow.activities;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.app.eisenflow.database.TaskContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_NOTE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_REMINDER_DATE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_REMINDER_OCCURRENCE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_REMINDER_TIME;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_REMINDER_WHEN;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.TaskContract.TaskEntry.KEY_TITLE;
import static com.app.eisenflow.database.TaskContract.TaskEntry.buildFlavorsUri;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener,
        LoaderManager.LoaderCallbacks<Cursor>,
        View.OnClickListener {
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_month_container) LinearLayout mToolbarMonthContainer;
    @BindView(R.id.toolbar_arrow) ImageView mToolbarArrow;
    @BindView(R.id.material_calendar_view) MaterialCalendarView mMaterialCalendarView;

    private static final int LOADER_ID = 0x02;
    private ContentResolver mContentResolver;
    private ActionBarDrawerToggle mToggle;

    public enum State {
        EXPANDED,
        COLLAPSED
    }
    private State mCurrentState = State.COLLAPSED;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        initViews();
        rotateMonthArrow(false);
//        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    private void initViews() {
        // Set toggle to open/close navigation drawer.
        mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mToggle.syncState();
        // Set listener to navigation drawer.
        mNavigationView.setNavigationItemSelectedListener(this);
        // Set listener to the floating action button.
        mFab.setOnClickListener(this);
        mToolbarMonthContainer.setOnClickListener(this);
        mAppBarLayout.addOnOffsetChangedListener(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            mCurrentState = State.EXPANDED;
            rotateMonthArrow(true);
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            mCurrentState = State.COLLAPSED;
            rotateMonthArrow(false);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.toolbar_month_container:
                boolean isExpanded = mCurrentState == State.EXPANDED ? true : false;
                rotateMonthArrow(isExpanded);
                isExpanded = !isExpanded;
                mAppBarLayout.setExpanded(isExpanded, true);
                break;
            case R.id.fab:

                Task task = new Task();
                task.setPriority(1);
                task.setTitle("My New Task");
                task.setDate("21/12/17");
                task.setTime("10:49");
                task.setReminderDate("22/12/17");
                task.setReminderOccurrence("weekly");
                task.setReminderTime("22:10");
                task.setReminderWhen("Monday");
                task.setNote("Something for the Soul");

                tasks = new ArrayList<>();
                tasks.add(task);
//
                Cursor c = getContentResolver().query(
                        CONTENT_URI,
                        new String[]{KEY_ROW_ID},
                        null,
                        null,
                        null);
//                if (c.getCount() == 0){
//                    insertData();
//                } else {
//                    updateData();
//                }

                deleteData();

                // initialize loader
                getSupportLoaderManager().initLoader(LOADER_ID, null, this);
                break;
        }
    }

    private void deleteData() {
        Uri uri = buildFlavorsUri(id);
        Log.v("eisen", "DELETE: Uri with Id --> " + uri);

        // Delete record in the DB.
        getContentResolver().delete(uri, null, null);
    }

    private void updateData() {
        ContentValues values = new ContentValues();
        values.put(KEY_PRIORITY, 2);

        Uri uri = buildFlavorsUri(id);
        Log.v("eisen", "UPDATE: Uri with Id --> " + uri);


        // Update record in the DB.
        getContentResolver().update(uri, values, null, null);
    }

    List<Task> tasks;
    long id = -1;
    public void insertData(){
        ContentValues valuesArr;
        // Loop through static array of Flavors, add each to an instance of ContentValues
        // in the array of ContentValues
        //for(int i = 0; i < tasks.size(); i++){
        valuesArr = new ContentValues();
        valuesArr.put(KEY_PRIORITY, tasks.get(0).getPriority());
        valuesArr.put(KEY_TITLE, tasks.get(0).getTitle());
        valuesArr.put(KEY_DATE, tasks.get(0).getDate());
        valuesArr.put(KEY_TIME, tasks.get(0).getTime());
        valuesArr.put(KEY_REMINDER_DATE, tasks.get(0).getReminderDate());
        valuesArr.put(KEY_REMINDER_TIME, tasks.get(0).getReminderTime());
        valuesArr.put(KEY_REMINDER_WHEN, tasks.get(0).getReminderWhen());
        valuesArr.put(KEY_REMINDER_OCCURRENCE, tasks.get(0).getReminderOccurrence());
        valuesArr.put(KEY_NOTE, tasks.get(0).getNote());
        //}

        Log.v("eisen", "URI = " + CONTENT_URI);

        // Insert our ContentValues.
        Uri uri = getContentResolver().insert(CONTENT_URI, valuesArr);

        id = Long.valueOf(uri.getLastPathSegment());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return new CursorLoader(
                this,
                CONTENT_URI,
                null,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        // ToDo: Add -> ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
        // mFlavorAdapter.swapCursor(data);
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Log.v("eisen", cursor.getString(cursor.getColumnIndex(KEY_TITLE)));
                cursor.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // ToDo: Add -> ((SimpleCursorAdapter) getListAdapter()).swapCursor(null);
        // mFlavorAdapter.swapCursor(null);
    }

    private void rotateMonthArrow(boolean isExpanded) {
        float rotation = isExpanded ? 0 : 180;
        ViewCompat.animate(mToolbarArrow).rotation(rotation).start();
    }
}
