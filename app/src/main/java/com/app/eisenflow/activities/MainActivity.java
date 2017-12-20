package com.app.eisenflow.activities;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.app.eisenflow.R;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        AppBarLayout.OnOffsetChangedListener,
        View.OnClickListener {
    @BindView(R.id.toolbar) Toolbar mToolbar;
    @BindView(R.id.fab) FloatingActionButton mFab;
    @BindView(R.id.drawer_layout) DrawerLayout mDrawer;
    @BindView(R.id.nav_view) NavigationView mNavigationView;
    @BindView(R.id.app_bar_layout) AppBarLayout mAppBarLayout;
    @BindView(R.id.toolbar_month_container) LinearLayout mToolbarMonthContainer;
    @BindView(R.id.toolbar_arrow) ImageView mToolbarArrow;
    @BindView(R.id.material_calendar_view) MaterialCalendarView mMaterialCalendarView;

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
    }

    private void initViews() {
        // Set toggle to open/close navigation drawer.
        mToggle = new ActionBarDrawerToggle(this, mDrawer, mToolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mToggle.syncState();
        // Set listener to navigation drawer.
        mNavigationView.setNavigationItemSelectedListener(this);
        // Set listener to the floating action button.
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
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
        }
    }

    private void rotateMonthArrow(boolean isExpanded) {
        float rotation = isExpanded ? 0 : 180;
        ViewCompat.animate(mToolbarArrow).rotation(rotation).start();
    }
}
