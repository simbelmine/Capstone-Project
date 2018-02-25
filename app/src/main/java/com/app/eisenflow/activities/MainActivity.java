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
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.eisenflow.R;
import com.app.eisenflow.ScrollToDateTask;
import com.app.eisenflow.decorators.EventDecoratorFeederTask;
import com.app.eisenflow.helpers.TaskReminderHelper;
import com.app.eisenflow.helpers.TasksCursorRecyclerViewAdapter;
import com.app.eisenflow.services.TimerService;
import com.app.eisenflow.utils.DataUtils;
import com.app.eisenflow.utils.Utils;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.util.Calendar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.app.eisenflow.database.EisenContract.TaskEntry.CONTENT_URI;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_PRIORITY;
import static com.app.eisenflow.utils.Constants.LOADER_ID;
import static com.app.eisenflow.utils.TaskUtils.bulkDoneTasksDelete;
import static com.app.eisenflow.utils.Utils.isTablet;
import static com.app.eisenflow.utils.Utils.setOrientation;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener,
        SwipeRefreshLayout.OnRefreshListener,
        OnDateSelectedListener,
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
    @BindView(R.id.refresh_container) SwipeRefreshLayout mRefreshContainer;

    private ActionBarDrawerToggle mToggle;
    private RecyclerView.LayoutManager mLinearLayoutManager;
    private TasksCursorRecyclerViewAdapter mTasksAdapter;
    private int mTaskPosition = -1;

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
        setOrientation(this);
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
        mRefreshContainer.setRefreshing(true);

        // Set the evening daily alarm and the weekly Sunday alarm.
        TaskReminderHelper.setDailyTipAlarms();
        TaskReminderHelper.setWeeklyTipAlarms();

        // Add Event decorators.
        new EventDecoratorFeederTask(mMaterialCalendarView).execute();

        mTaskPosition = getIntent().getIntExtra(EXTRA_TASK_POSITION, -1);

        int itemsCountLocal = getItemsCountLocal();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Utils.isServiceRunning(TimerService.class)) {
            stopService(new Intent(this, TimerService.class));
        }
        if (mTaskPosition != -1 && mTasksAdapter != null) {
            mTasksAdapter.getBottomSheet().openBottomSheet(mTaskPosition);
        }
        setCalendarCurrentDate();
    }

    @Override
    protected void onPause() {
        super.onPause();
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

        mRefreshContainer.setOnRefreshListener(this);

        mMaterialCalendarView.setOnDateChangedListener(this);
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

        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_today:
                setCalendarCurrentDate();
                scrollListToCurrentMonth();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.nav_view_all:
                applyFilter(null);
                closeDrawer();
                return true;
            case R.id.nav_show_do_it:
                applyFilter(DataUtils.Priority.ONE);
                closeDrawer();
                return true;
            case R.id.nav_show_decide:
                applyFilter(DataUtils.Priority.TWO);
                closeDrawer();
                return true;
            case R.id.nav_show_delegate:
                applyFilter(DataUtils.Priority.THREE);
                closeDrawer();
                return true;
            case R.id.nav_show_drop_it:
                applyFilter(DataUtils.Priority.FOUR);
                closeDrawer();
                return true;
            case R.id.clear_all_done:
                bulkDoneTasksDelete();
                closeDrawer();
                return true;
            case R.id.nav_open_tutorial:
//                mainSharedPrefs.edit().putBoolean(SplashScreens.TUTORIAL_ACTIVATED, false).apply();
//                startActivity(new Intent(this, SplashScreens.class));
                return true;
            case R.id.nav_info:
                closeDrawer();
//                startActivity(new Intent(this, AboutDialog.class));
                return true;
            case R.id.nav_send_feedback:
//                sendFeedback();
                return true;
            case R.id.nav_rate_app:
//                showRateDialog();
                return true;
        }
        return false;
    }

    private void applyFilter(DataUtils.Priority priority) {
        Bundle bundle = new Bundle();
        if (priority != null) {
            bundle.putInt(EXTRA_TASK_PRIORITY, priority.getValue());
        }
        getSupportLoaderManager().restartLoader(LOADER_ID, bundle, this);
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (verticalOffset == 0) {
            mCurrentState = State.EXPANDED;
            rotateMonthArrow(true);
            mTasksAdapter.getBottomSheet().closeBottomSheet();
            ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, true);
        } else if (Math.abs(verticalOffset) >= appBarLayout.getTotalScrollRange()) {
            mCurrentState = State.COLLAPSED;
            rotateMonthArrow(false);
            ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, false);
        }
    }

    @OnClick (R.id.toolbar_month_container)
    public void onToolbarMonthClick() {
        if (!isTablet(this)) {
            boolean isExpanded = mCurrentState == State.EXPANDED ? true : false;
            rotateMonthArrow(isExpanded);
            isExpanded = !isExpanded;
            ViewCompat.setNestedScrollingEnabled(mTasksRecyclerView, isExpanded);
            mAppBarLayout.setExpanded(isExpanded, true);
        }
    }

    @OnClick (R.id.fab)
    public void onFabClick() {
        startActivity(new Intent(MainActivity.this, SingleTaskActivity.class));
        if (mTasksAdapter != null && mTasksAdapter.getBottomSheet().isBottomSheetExpanded()) {
            mTasksAdapter.getBottomSheet().closeBottomSheet();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        String selection = null;
        String[] selectionArgs = null;
        if (args != null && args.containsKey(EXTRA_TASK_PRIORITY)) {
            int priority = args.getInt(EXTRA_TASK_PRIORITY);
            selection = KEY_PRIORITY+"=?";
            selectionArgs = new String[]{priority+""};
        }

        switch (id) {
            case LOADER_ID:
                return new CursorLoader(
                        this,
                        CONTENT_URI,
                        null,
                        selection,
                        selectionArgs,
                        null
                );
            default:
                throw new IllegalArgumentException("no id handled!");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mRefreshContainer.setRefreshing(false);
        switch (loader.getId()) {
            case LOADER_ID:
                if (data == null) {
                    return;
                }
                mTasksAdapter.swapCursor(data);
                mMaterialCalendarView.removeDecorators();
                new EventDecoratorFeederTask(mMaterialCalendarView).execute();
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

    @Override
    public void onRefresh() {
        getSupportLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) {
        new ScrollToDateTask(this, mLinearLayoutManager, date).execute();
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

    private void setCalendarCurrentDate() {
        mMaterialCalendarView.setCurrentDate(Calendar.getInstance());
        mMaterialCalendarView.setSelectedDate(Calendar.getInstance());
    }

    private void scrollListToCurrentMonth() {
        final int row = mTasksAdapter.getCurrentDateRow();
        if (row != -1) {
            final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(this) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(row);
            mLinearLayoutManager.startSmoothScroll(smoothScroller);
        }
    }

    private void closeDrawer() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
    }

    @Override
    public void onBackPressed() {
        if (mTasksAdapter != null && mTasksAdapter.getBottomSheet().isBottomSheetExpanded()) {
            mTasksAdapter.getBottomSheet().closeBottomSheet();
            return;
        }
        super.onBackPressed();
    }
}
