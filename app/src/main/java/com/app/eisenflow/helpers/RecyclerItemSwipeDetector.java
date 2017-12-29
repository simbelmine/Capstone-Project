package com.app.eisenflow.helpers;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;
import com.app.eisenflow.activities.MainActivity;
import com.app.eisenflow.activities.SingleTaskActivity;
import com.app.eisenflow.utils.DataUtils;

import butterknife.ButterKnife;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_PRIORITY;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;

/**
 * Created by Sve on 5/1/16.
 */
public class RecyclerItemSwipeDetector implements View.OnTouchListener {
    //    private static final int MIN_LOCK_DISTANCE = 30; // disallow motion intercept
//    private static final int MIN_DISTANCE = 550;
    private static final int MIN_LOCK_DISTANCE = 300; // disallow motion intercept
    private static final int MIN_DISTANCE = 100;
    private static final int DISTANCE = 70;
    private static final int ICON_SHOW_DELAY = 300;
    private static final int DISMISS_DELAY = 3000;
    private static final int ACTION_DELAY = 1500;
    int[] flags = new int[] {Intent.FLAG_ACTIVITY_NEW_TASK};
    private Activity context;
    private boolean motionInterceptDisallowed = false;
    private float downX, upX;
    private TasksViewHolder holder;
    private RecyclerView recyclerView;
    private RelativeLayout currentMenuLayout;
    //private SwipeRefreshLayout pullToRefreshLayout;

    boolean isLeftToRight = false;

    Animation animZoomIn;
    Animation animZoomOut;
    float oldDeltaX = -1;
    boolean isTriggered_LtoR = false;
    boolean isTriggered_RtoL = false;

    private Cursor mCursor;

    public RecyclerItemSwipeDetector(Activity mContext, Cursor cursor, TasksViewHolder viewHolder) {
        this.context = mContext;
        this.mCursor = cursor;
        this.holder = viewHolder;
        this.recyclerView = context.findViewById(R.id.tasks_recycler_view);

        animZoomIn = AnimationUtils.loadAnimation(context, R.anim.zoom_in);
        animZoomOut = AnimationUtils.loadAnimation(context, R.anim.zoom_out);

        currentMenuLayout = getCorrectLayout();

        //pullToRefreshLayout =  (SwipeRefreshLayout)recyclerView.getParent().getParent();
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        int priorityValue = mCursor.getInt(mCursor.getColumnIndex(KEY_PRIORITY));
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


                if(deltaX > 0) {
                    isLeftToRight = false;
                }
                else {
                    isLeftToRight = true;
                }

                currentMenuLayout.setVisibility(View.VISIBLE);
                if(priority == TWO) holder.mTaskProgress.setVisibility(View.INVISIBLE);
                holder.mDeleteActionLayout.setPressed(true);

                swipe(v, (int) deltaX);
                return true;
            }

            case MotionEvent.ACTION_UP: {
                upX = event.getX();
                float deltaX = upX - downX;

                if (priority == TWO) {
                    if (Math.abs(deltaX) > DISTANCE) {
                        holder.mTaskProgress.setVisibility(View.INVISIBLE);
                    } else {
                        holder.mTaskProgress.setVisibility(View.VISIBLE);
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

                holder.mDeleteActionLayout.setPressed(false);

                return true;
            }

            case MotionEvent.ACTION_CANCEL: {
                currentMenuLayout.setVisibility(View.VISIBLE);
                //pullToRefreshLayout.setEnabled(true);

                upX = event.getX();
                float deltaX = upX - downX;
                if(Math.abs(deltaX) > MIN_DISTANCE) {
                    performSwipeAction(deltaX);
                }
                return true;
            }
        }

        return true;
    }

    private void performIconAnimations(float deltaX) {
        if(oldDeltaX == -1) oldDeltaX = deltaX;
        if(deltaX > oldDeltaX) {
            if(!isTriggered_RtoL) {
                holder.mDeleteActionIcon.startAnimation(animZoomOut);
                holder.mRightActionIcon.startAnimation(animZoomIn);
                isTriggered_RtoL = true;
                isTriggered_LtoR = false;
            }
        }
        else if(deltaX < oldDeltaX) {
            if (!isTriggered_LtoR) {
                holder.mDeleteActionIcon.startAnimation(animZoomIn);
                holder.mRightActionIcon.startAnimation(animZoomOut);
                isTriggered_LtoR = true;
                isTriggered_RtoL = false;
            }
        }
        oldDeltaX = deltaX;
    }

    private void swipe(View v, int distance) {
        View animationView = holder.mTaskHolder;
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) animationView.getLayoutParams();
        int rightMargin, leftMargin;

        if(distance == 0)  holder.mDeleteActionIcon.startAnimation(animZoomOut);

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
        return holder.mDeleteActionLayout;
    }

    private void deleteTask() {
        holder.mDeleteActionLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(holder.mDeleteActionLayout.getVisibility() == View.VISIBLE) {
                    holder.mTaskProgress.setVisibility(View.INVISIBLE);
                    holder.mDeleteActionLayout.setVisibility(View.INVISIBLE);
                    holder.mUndoLayout.setVisibility(View.VISIBLE);
                    holder.mUndoActionBtn.setVisibility(View.INVISIBLE);
                    holder.mUndoButton.setVisibility(View.VISIBLE);
                }
            }
        }, ICON_SHOW_DELAY);

        holder.mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.mTaskProgress.setVisibility(View.VISIBLE);
                holder.mUndoButton.setVisibility(View.INVISIBLE);
                holder.mUndoLayout.setVisibility(View.INVISIBLE);
                holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                holder.mTaskHolder.setVisibility(View.VISIBLE);
                swipe(null, 0);
            }
        });

        holder.mUndoButton.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(holder.mUndoButton.getVisibility() == View.VISIBLE) {
                    sendDeleteBroadcast();
                }
            }
        }, DISMISS_DELAY);
    }

    private void sendDeleteBroadcast() {
//        Intent deleteIntent = new Intent(NewTaskListAdapterDB.ACTION_DELETE);
//        deleteIntent.putExtra(LocalDataBaseHelper.KEY_ROW_ID, taskId);
//        deleteIntent.putExtra("position", position);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(deleteIntent);
    }

    private void activateAction() {
        if(holder.mRightActionIcon.getTag() != null) {
            holder.mRightActionIcon.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.mRightActionIcon.getVisibility() == View.VISIBLE) {
                        holder.mDeleteActionLayout.setVisibility(View.INVISIBLE);
                        holder.mUndoLayout.setVisibility(View.VISIBLE);
                        holder.mUndoActionBtn.setVisibility(View.VISIBLE);
                        holder.mUndoButton.setVisibility(View.INVISIBLE);
                    }
                }
            }, ICON_SHOW_DELAY);

            holder.mUndoActionBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.mUndoActionBtn.setVisibility(View.INVISIBLE);
                    holder.mUndoLayout.setVisibility(View.INVISIBLE);
                    holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                    holder.mTaskHolder.setVisibility(View.VISIBLE);
                    holder.mTaskProgress.setVisibility(View.VISIBLE);
                    swipe(null, 0);
                }
            });

            holder.mUndoActionBtn.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (holder.mUndoActionBtn.getVisibility() == View.VISIBLE) {
                        switch ((int)holder.mRightActionIcon.getTag()) {
                            case 0:
                                Log.v("eisen", "Action -> Timer");
                                //sendCardActionBroadcast(NewTaskListAdapterDB.TIMER_ACTION);
                                swipe(null, 0);
                                holder.mUndoLayout.setVisibility(View.INVISIBLE);
                                holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                            case 1: {
                                Log.v("eisen", "Action -> Up++");
                                //sendCardActionBroadcast(NewTaskListAdapterDB.PROGRESS_UP_ACTION);
                                swipe(null, 0);
                                holder.mUndoLayout.setVisibility(View.INVISIBLE);
                                holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                            }
                            case 2:
                                Log.v("eisen", "Action -> Share");
                                //sendCardActionBroadcast(NewTaskListAdapterDB.SHARE_ACTION);
                                swipe(null, 0);
                                holder.mUndoLayout.setVisibility(View.INVISIBLE);
                                holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                                break;
                        }
                    }
                    else {
                        holder.mUndoLayout.setVisibility(View.INVISIBLE);
                        holder.mDeleteActionLayout.setVisibility(View.VISIBLE);
                    }
                }
            }, ACTION_DELAY);
        }
    }

    private void sendCardActionBroadcast(String action) {
//        Intent intent = new Intent(action);
//        intent.putExtra(LocalDataBaseHelper.KEY_ROW_ID, taskId);
//        intent.putExtra("position", position);
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    private void startActivity(Class<?> activityClass, View view, int[] flags, String[] extras_names, long[] extras_values) {
        Intent intent = new Intent(context, activityClass);
        if(flags != null) {
            for(int i = 0; i < flags.length; i++) {
                intent.addFlags(flags[i]);
            }
        }
        if(extras_names != null && extras_values != null) {
            if(extras_names.length == extras_values.length) {
                for(int i = 0; i < extras_names.length; i++) {
                    intent.putExtra(extras_names[i], extras_values[i]);
                }
            }
        }

        Bundle b;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            b = ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight()).toBundle();
            context.startActivity(intent, b);
        }
        else {
            context.startActivity(intent);
        }
    }

    private void performClick(View v) {
//        String[] extra_names = new String[]{KEY_ROW_ID, "position"};
//        long[] extra_value = new long[]{taskId, position};
        String[] extra_names = new String[]{};
        long[] extra_value = new long[]{};

        startActivity(SingleTaskActivity.class, v, flags, extra_names, extra_value);
    }

    private void performSwipeAction(float deltaX) {

//        if (Math.abs(deltaX) >= MIN_DISTANCE || Math.abs(deltaX) >= DISTANCE) {
        if (Math.abs(deltaX) > DISTANCE) {
            // L to R  +
            // R to L  -
            if(deltaX > 0) {
                deleteTask();
                Toast.makeText(context, "Delete", Toast.LENGTH_SHORT).show();
            }
            else {
                activateAction();
                Toast.makeText(context, "Action", Toast.LENGTH_SHORT).show();
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
