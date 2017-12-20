package com.app.eisenflow;

import android.animation.ArgbEvaluator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.app.eisenflow.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.app.eisenflow.LaunchActivity.PREF_FIRST_TIME_USER;

/**
 *  TutorialActivity should not contain UI elements that require user input,
 *  such as buttons and fields.
 *  Similarly, it should not be used as a UI element for a task
 *  the user will do regularly.
 *
 * Created on 12/18/17.
 */

public class TutorialActivity extends AppCompatActivity implements
        ViewPager.OnPageChangeListener,
        View.OnClickListener {
    @BindView(R.id.tutorial_view_pager) ViewPager mViewPager;
    @BindView(R.id.tutorial_navigation_layout) FrameLayout mTutorialNavigation;
    @BindView(R.id.intro_btn_next) ImageButton mNextButton;
    @BindView(R.id.intro_btn_skip) Button mSkipButton;
    @BindView(R.id.intro_btn_finish) Button mFinishButton;
    @BindView(R.id.intro_indicator_0) ImageView mZero;
    @BindView(R.id.intro_indicator_1) ImageView mOne;

    private PagerAdapter mPagerAdapter;
    private ImageView[] mIndicatorsList;
    private int[] colorList;
    private int mPage = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tutorial_layout);
        ButterKnife.bind(this);

        mIndicatorsList = new ImageView[]{mZero, mOne};
        updateIndicators(mPage);

        mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(mPage);
        mViewPager.setPageTransformer(false, new ParallaxPageTransformer());
        mViewPager.addOnPageChangeListener(this);

        mNextButton.setOnClickListener(this);
        mSkipButton.setOnClickListener(this);
        mFinishButton.setOnClickListener(this);

        final int color1 = ContextCompat.getColor(this, R.color.tile);
        final int color2 = ContextCompat.getColor(this, R.color.green);
        colorList = new int[]{color1, color2};
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        final ArgbEvaluator evaluator = new ArgbEvaluator();
        int colorUpdate = (Integer) evaluator.evaluate(positionOffset, colorList[position], colorList[position == 1 ? position : position + 1]);
        mTutorialNavigation.setBackgroundColor(colorUpdate);
    }

    @Override
    public void onPageSelected(int position) {
        mPage = position;
        updateIndicators(mPage);

        switch (position) {
            case 0:
                mTutorialNavigation.setBackgroundColor(colorList[0]);
                break;
            case 1:
                mTutorialNavigation.setBackgroundColor(colorList[1]);
                break;
        }

        mNextButton.setVisibility(position == 1 ? View.GONE : View.VISIBLE);
        mFinishButton.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.intro_btn_next:
                mPage += 1;
                mViewPager.setCurrentItem(mPage, true);
                break;
            case R.id.intro_btn_skip:
                startMainActivity();
                finish();
                break;
            case R.id.intro_btn_finish:
                Utils.saveSharedBooleanSetting(
                        TutorialActivity.this,
                        PREF_FIRST_TIME_USER, false);
                startMainActivity();
                finish();
                break;
        }
    }

    private void updateIndicators(int position) {
        for (int i = 0; i < mIndicatorsList.length; i++) {
            mIndicatorsList[i].setBackgroundResource(
                    i == position ? R.drawable.indicator_selected : R.drawable.indicator_unselected
            );
        }
    }

    private void startMainActivity() {
        startActivity(new Intent(this, MainActivity.class));
    }
}
