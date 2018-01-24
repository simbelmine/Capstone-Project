package com.app.eisenflow.activities;

import android.content.Intent;
import android.database.Cursor;
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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.eisenflow.R;
import com.app.eisenflow.helpers.TasksCursorRecyclerViewAdapter;
import com.app.eisenflow.utils.Utils;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.utils.Statics.LOADER_ID;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_month_container) LinearLayout mToolbarMonthContainer;
    @BindView(R.id.toolbar_arrow) ImageView mToolbarArrow;
    @BindView(R.id.material_calendar_view) MaterialCalendarView mMaterialCalendarView;
    @BindView(R.id.tasks_recycler_view) RecyclerView mTasksRecyclerView;

    private ActionBarDrawerToggle mToggle;
    private RecyclerView.LayoutManager mLinearLayoutManager;
    private TasksCursorRecyclerViewAdapter mTasksAdapter;

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
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);


        int itemsCountLocal = getItemsCountLocal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isServiceRunning(TimerService.class)) {
            stopService(new Intent(this, TimerService.class));
        }
    }

    private void initViews() {
        // Set toggle to open/close navigation drawer.
        mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mToggle.syncState();
        // Set listener to navigation drawer.
        mNavigationView.setNavigationItemSelectedListener(this);
        mAppBarLayout.addOnOffsetChangedListener(this);

        mLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        mTasksAdapter = new TasksCursorRecyclerViewAdapter(this, null);

        mTasksRecyclerView.setLayoutManager(mLinearLayoutManager);
        mTasksRecyclerView.setAdapter(mTasksAdapter);
        ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, false);
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
            ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, true);
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            mCurrentState = State.COLLAPSED;
            rotateMonthArrow(false);
            ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, false);
        }
    }

    @OnClick (R.id.toolbar_month_container)
    public void onToolbarMonthClick() {
        boolean isExpanded = mCurrentState == State.EXPANDED ? true : false;
        rotateMonthArrow(isExpanded);
        isExpanded = !isExpanded;
        ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, isExpanded);
        mAppBarLayout.setExpanded(isExpanded, true);
    }

    @OnClick (R.id.fab)
    public void onFabClick() {
        startActivity(new Intent(MainActivity.this, SingleTaskActivity.class));
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case LOADER_ID:
                return new CursorLoader(
                        this,
                        CONTENT_URI,
                        null,
                        null,
                        null,
                        null
                );
            default:
                throw new IllegalArgumentException("no id handled!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        // ToDo: Add -> ((SimpleCursorAdapter) getListAdapter()).swapCursor(c);
        // mFlavorAdapter.swapCursor(data);

        switch (loader.getId()) {
            case LOADER_ID:
                if (data == null) {
                    return;
                }

                mTasksAdapter.swapCursor(data);
                break;
            default:
                throw new IllegalArgumentException("no loader id handled!");
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

    private int getItemsCountLocal() {
        int itemCount = 0;
        Cursor query = getContentResolver().query(CONTENT_URI, null, null, null, null);
        if (query != null) {
            itemCount = query.getCount();
            query.close();
        }

        return itemCount;
    }
}
