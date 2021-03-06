package com.app.eisenflow.helpers;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;

import com.app.eisenflow.R;
import com.app.eisenflow.activities.PreviewActivity;
import com.app.eisenflow.utils.DataUtils;

import static com.app.eisenflow.utils.Constants.ACTION_DELAY;
import static com.app.eisenflow.utils.Constants.DISMISS_DELAY;
import static com.app.eisenflow.utils.Constants.DISTANCE;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Constants.ICON_SHOW_DELAY;
import static com.app.eisenflow.utils.Constants.MIN_DISTANCE;
import static com.app.eisenflow.utils.Constants.MIN_LOCK_DISTANCE;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;
import static com.app.eisenflow.utils.TaskUtils.addProgressAction;
import static com.app.eisenflow.utils.TaskUtils.deleteTaskAction;
import static com.app.eisenflow.utils.TaskUtils.shareTaskAction;
import static com.app.eisenflow.utils.TaskUtils.startTimerActivityAction;
import static com.app.eisenflow.utils.Utils.isTablet;

/**
 * Created on 5/1/16.
 */
public class RecyclerItemSwipeDetector implements View.OnTouchListener {
    private Activity mActivity;
    private boolean motionInterceptDisallowed = false;
    private float downX, upX;
    private TasksViewHolder mHolder;
    private RecyclerView recyclerView;
    private RelativeLayout currentMenuLayout;
    //private SwipeRefreshLayout pullToRefreshLayout;
    boolean isLeftToRight = false;
    private Animation animZoomIn;
    private Animation animZoomOut;
    float oldDeltaX = -1;
    private boolean isTriggered_LtoR = false;
    private boolean isTriggered_RtoL = false;

    public RecyclerItemSwipeDetector(Activity mContext, TasksViewHolder viewHolder) {
        this.mActivity = mContext;
        this.mHolder = viewHolder;
        this.recyclerView = this.mActivity.findViewById(R.id.tasks_recycler_view);

        animZoomIn = AnimationUtils.loadAnimation(this.mActivity, R.anim.zoom_in);
        animZoomOut = AnimationUtils.loadAnimation(this.mActivity, R.anim.zoom_out);

        currentMenuLayout = getCorrectLayout();

        //pullToRefreshLayout =  (SwipeRefreshLayout)recyclerView.getParent().getParent();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Cursor cursor = mHolder.getHolderCursor();
        if (cursor != null && cursor.moveToPosition(mHolder.getAdapterPosition())) {
            int priorityValue = cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY));
            DataUtils.Priority priority = DataUtils.Priority.valueOf(priorityValue);

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    downX = event.getX();
                    return true; // allow other events like Click to be processed
                }

                case MotionEvent.ACTION_MOVE: {
                    upX = event.getX();
                    float deltaX = downX - upX;

                    //pullToRefreshLayout.setEnabled(false);

                    // If we opened the menu enough => the RecyclerView is going to accept the change if not, skip it
                    if (Math.abs(deltaX) > MIN_LOCK_DISTANCE && recyclerView != null && !motionInterceptDisallowed) {
                        recyclerView.requestDisallowInterceptTouchEvent(true);
                        motionInterceptDisallowed = true;
                    }

                    performIconAnimations(deltaX);


                    if (deltaX > 0) {
                        isLeftToRight = false;
                    } else {
                        isLeftToRight = true;
                    }

                    currentMenuLayout.setVisibility(View.VISIBLE);
                    if (priority == TWO) mHolder.mTaskProgress.setVisibility(View.INVISIBLE);
                    mHolder.mDeleteActionLayout.setPressed(true);

                    swipe(v, (int) deltaX);
                    return true;
                }

                case MotionEvent.ACTION_UP: {
                    upX = event.getX();
                    float deltaX = upX - downX;

                    if (priority == TWO) {
                        if (Math.abs(deltaX) > DISTANCE) {
                            mHolder.mTaskProgress.setVisibility(View.INVISIBLE);
                        } else {
                            mHolder.mTaskProgress.setVisibility(View.VISIBLE);
                        }
                    }

                    if (upX == downX) {
                        performClick(v);
                    } else {
                        performSwipeAction(deltaX);
                    }


                    if (recyclerView != null) {
                        recyclerView.requestDisallowInterceptTouchEvent(false);
                        motionInterceptDisallowed = false;
                    }

                    mHolder.mDeleteActionLayout.setPressed(false);

                    return true;
                }

                case MotionEvent.ACTION_CANCEL: {
                    currentMenuLayout.setVisibility(View.VISIBLE);
                    //pullToRefreshLayout.setEnabled(true);

                    upX = event.getX();
                    float deltaX = upX - downX;
                    if (Math.abs(deltaX) > MIN_DISTANCE) {
                        performSwipeAction(deltaX);
                    }
                    return true;
                }
            }
        }

        return true;
    }

    private void performIconAnimations(float deltaX) {
        if(oldDeltaX == -1) oldDeltaX = deltaX;
        if(deltaX > oldDeltaX) {
            if(!isTriggered_RtoL) {
                mHolder.mDeleteActionIcon.startAnimation(animZoomOut);
                mHolder.mRightActionIcon.startAnimation(animZoomIn);
                isTriggered_RtoL = true;
                isTriggered_LtoR = false;
            }
        }
        else if(deltaX < oldDeltaX) {
            if (!isTriggered_LtoR) {
                mHolder.mDeleteActionIcon.startAnimation(animZoomIn);
                mHolder.mRightActionIcon.startAnimation(animZoomOut);
                isTriggered_LtoR = true;
                isTriggered_RtoL = false;
            }
        }
        oldDeltaX = deltaX;
    }

    private void swipe(View v, int distance) {
        View animationView = mHolder.mTaskHolder;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) animationView.getLayoutParams();
        int rightMargin, leftMargin;

        if(distance == 0)  mHolder.mDeleteActionIcon.startAnimation(animZoomOut);

        if(v != null && Math.abs(distance) >= DISTANCE) {
            if(distance < 0)
                distance = -v.getWidth()/2;
            else
                distance = v.getWidth()/2;
        }

        // L to R
        if(distance < 0) {
            rightMargin = 0;
            leftMargin = -distance;
        }
        // R to L
        else {
            rightMargin = distance;
            leftMargin = 0;
        }
        params.rightMargin = rightMargin;
        params.leftMargin = leftMargin;

        animationView.setLayoutParams(params);
    }

    private RelativeLayout getCorrectLayout() {
        return mHolder.mDeleteActionLayout;
    }

    private void deleteTask() {
        mHolder.mDeleteActionLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mHolder.mDeleteActionLayout.getVisibility() == View.VISIBLE) {
                    mHolder.getAdapter().getBottomSheet().closeBottomSheet();
                    mHolder.mTaskProgress.setVisibility(View.INVISIBLE);
                    mHolder.mDeleteActionLayout.setVisibility(View.INVISIBLE);
                    mHolder.mUndoLayout.setVisibility(View.VISIBLE);
                    mHolder.mUndoActionBtn.setVisibility(View.INVISIBLE);
                    mHolder.mUndoButton.setVisibility(View.VISIBLE);
                }
            }
        }, ICON_SHOW_DELAY);

        mHolder.mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHolder.mTaskProgress.setVisibility(View.VISIBLE);
                mHolder.mUndoButton.setVisibility(View.INVISIBLE);
                mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                mHolder.mTaskHolder.setVisibility(View.VISIBLE);
                swipe(null, 0);
            }
        });

        mHolder.mUndoButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                Cursor cursor = mHolder.getHolderCursor();
                if (cursor != null && cursor.moveToPosition(mHolder.getAdapterPosition())) {
                    if (mHolder.mUndoButton.getVisibility() == View.VISIBLE) {
                        deleteTaskAction(
                                cursor.getInt(cursor.getColumnIndex(KEY_ROW_ID)),
                                cursor.getInt(cursor.getColumnIndex(KEY_PRIORITY)));
                        swipe(null, 0);
                    }
                }
            }
        }, DISMISS_DELAY);
    }

    private void activateAction() {
        if(mHolder.mRightActionIcon.getTag() != null) {
            final DataUtils.Priority priority = DataUtils.Priority.valueOf((int) mHolder.mRightActionIcon.getTag());
            mHolder.mRightActionIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mHolder.mRightActionIcon.getVisibility() == View.VISIBLE) {
                        mHolder.mDeleteActionLayout.setVisibility(View.INVISIBLE);
                        mHolder.mUndoLayout.setVisibility(View.VISIBLE);
                        mHolder.mUndoActionBtn.setVisibility(View.VISIBLE);
                        mHolder.mUndoButton.setVisibility(View.INVISIBLE);
                    }
                }
            }, ICON_SHOW_DELAY);

            mHolder.mUndoActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mHolder.mUndoActionBtn.setVisibility(View.INVISIBLE);
                    mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                    mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                    mHolder.mTaskHolder.setVisibility(View.VISIBLE);
                    mHolder.mTaskProgress.setVisibility(View.VISIBLE);
                    swipe(null, 0);
                }
            });

            mHolder.mUndoActionBtn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (mHolder.mUndoActionBtn.getVisibility() == View.VISIBLE) {

                        switch (priority) {
                            case ONE:
                                Log.v(TAG, "Action: Timer");
                                startTimerActivityAction(mActivity, mHolder.getAdapterPosition());
                                swipe(null, 0);
                                mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                                mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                            case TWO: {
                                Log.v(TAG, "Action: Progress++");
                                addProgressAction(mHolder.getHolderCursor(), mHolder.getAdapterPosition());
                                swipe(null, 0);
                                mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                                mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                            }
                            case THREE:
                                Log.v(TAG, "Action: Share");
                                shareTaskAction(mHolder.getHolderCursor(), mHolder.getAdapterPosition());
                                swipe(null, 0);
                                mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                                mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                    else {
                        mHolder.mUndoLayout.setVisibility(View.INVISIBLE);
                        mHolder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                    }
                }
            }, ACTION_DELAY);
        } else {
            swipe(null, 0);
        }
    }

    private void sendCardActionBroadcast(String action) {
//        Intent intent = new Intent(action);
//        intent.putExtra(LocalDataBaseHelper.KEY_ROW_ID, taskId);
//        intent.putExtra("position", position);
//        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
    }

    private void performClick(View v) {
        int taskPosition = mHolder.getAdapterPosition();
        if (!isTablet(mActivity)) {
            mHolder.getAdapter().getBottomSheet().openBottomSheet(taskPosition);
        } else {
            Intent previewIntent = new Intent(mActivity, PreviewActivity.class);
            previewIntent.putExtra(EXTRA_TASK_POSITION, taskPosition);
            mActivity.startActivity(previewIntent);
        }
    }

    private void performSwipeAction(float deltaX) {

//        if (Math.abs(deltaX) >= MIN_DISTANCE || Math.abs(deltaX) >= DISTANCE) {
        if (Math.abs(deltaX) > DISTANCE) {
            // L to R  +
            // R to L  -
            if(deltaX > 0) {
                deleteTask();
            }
            else {
                activateAction();
            }

        } else {
            swipe(null, 0);
        }

        if (recyclerView != null) {
            recyclerView.requestDisallowInterceptTouchEvent(false);
            motionInterceptDisallowed = false;
        }
        currentMenuLayout.setVisibility(View.VISIBLE);
    }
}
