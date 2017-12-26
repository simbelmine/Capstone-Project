package com.app.eisenflow.helpers;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created on 12/25/17.
 */

public class TasksCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter {
    private Context mContext;

    public TasksCursorRecyclerViewAdapter(Context context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(android.R.layout.simple_list_item_1, parent, false);
        return new TasksViewHolder(v);
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
}
