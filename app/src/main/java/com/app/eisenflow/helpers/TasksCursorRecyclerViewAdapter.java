package com.app.eisenflow.helpers;

import android.app.Activity;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.eisenflow.R;

/**
 * Created on 12/25/17.
 */

public class TasksCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter {
    private Activity mContext;
    private String mLastSeenDate;
    private String mLastSeenMonth;

    public TasksCursorRecyclerViewAdapter(Activity context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.task_list_item, parent, false);
        return new TasksViewHolder(mContext, this, v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, Cursor cursor) {
        TasksViewHolder tasksHolder = (TasksViewHolder) viewHolder;
        cursor.moveToPosition(cursor.getPosition());
        tasksHolder.setData(cursor);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public Cursor swapCursor(Cursor newCursor) {
        setLastSeenMonth(null);
        return super.swapCursor(newCursor);
    }

    public void setLastSeenDate(String lastSeenDate) {
        this.mLastSeenDate = lastSeenDate;
    }
    public String getLastSeenDate() {
        return this.mLastSeenDate;
    }

    public void setLastSeenMonth(String lastSeenMonth) {
        this.mLastSeenMonth = lastSeenMonth;
    }
    public String getLastSeenMonth() {
        return this.mLastSeenMonth;
    }
}
