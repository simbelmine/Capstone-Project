package com.app.eisenflow.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.Toast;

import com.app.eisenflow.R;
import com.app.eisenflow.Task;
import com.app.eisenflow.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created on 12/21/17.
 */

public class SingleTaskActivity extends AppCompatActivity {
    @BindView(R.id.single_task_app_bar) AppBarLayout mAppBarLayout;
    @BindView(R.id.single_task_toolbar) Toolbar mToolbar;
    @BindView(R.id.task_name) EditText mTaskTitle;
    @BindView(R.id.do_it_holder) FrameLayout mDoItHolder;
    @BindView(R.id.decide_holder) FrameLayout mDecideHolder;
    @BindView(R.id.delegate_holder) FrameLayout mDelegateHolder;
    @BindView(R.id.dump_it_holder) FrameLayout mDumpItHolder;
    @BindView(R.id.date_holder) FrameLayout mDateHolder;
    @BindView(R.id.time_holder) FrameLayout mTimeHolder;
    @BindView(R.id.occurrence_holder) RadioGroup mOccurrenceHolder;
    @BindView(R.id.mon_cb) CheckBox mMonCheckBox;
    @BindView(R.id.tue_cb) CheckBox mTueCheckBox;
    @BindView(R.id.wed_cb) CheckBox mWedCheckBox;
    @BindView(R.id.thu_cb) CheckBox mThuCheckBox;
    @BindView(R.id.fri_cb) CheckBox mFriCheckBox;
    @BindView(R.id.sat_cb) CheckBox mSatCheckBox;
    @BindView(R.id.sun_cb) CheckBox mSunCheckBox;
    @BindView(R.id.vibration_switch) Switch mVibrationSwitch;
    @BindView(R.id.note_edit_text) EditText mNoteEditText;

    private Task mTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_task);
        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.close_small);

        init();
    }

    private void init() {
        mTask = new Task();
    }

    @OnClick (R.id.do_it_holder)
    public void onClickDoItHolder() {
        Toast.makeText(SingleTaskActivity.this, "DO It...", Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.single_task_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
           finish();
        }

        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.action_save_task:
                Toast.makeText(SingleTaskActivity.this, "TASK SAVED", Toast.LENGTH_SHORT).show();
                break;
        }

        return true;
    }

}
