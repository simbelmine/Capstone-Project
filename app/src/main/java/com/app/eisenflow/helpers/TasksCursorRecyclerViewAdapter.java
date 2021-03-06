package com.app.eisenflow.helpers;

import android.app.Activity;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.app.eisenflow.EisenBottomSheet;
import com.app.eisenflow.R;
import com.app.eisenflow.widget.WidgetProvider;

import java.util.HashMap;
import java.util.Map;

/**
 * Created on 12/25/17.
 */

public class TasksCursorRecyclerViewAdapter extends CursorRecyclerViewAdapter<TasksViewHolder> {
    private Activity mContext;
    private String mLastSeenDate;
    private String mLastSeenMonth;
    public Map<String, Integer> mDateHeaderMap;
    public Map<String, Integer> mMonthHeaderMap;
    private EisenBottomSheet mEisenBottomSheet;
    private int mCurrantDateRow = -1;

    public TasksCursorRecyclerViewAdapter(Activity context, Cursor cursor) {
        super(context, cursor);
        mContext = context;
        mDateHeaderMap = new HashMap<>();
        mMonthHeaderMap = new HashMap<>();
        mEisenBottomSheet = new EisenBottomSheet(mContext);
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    @Override
    public TasksViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.task_list_item, parent, false);
        return new TasksViewHolder(mContext, this, v);
    }

    @Override
    public void onBindViewHolder(TasksViewHolder viewHolder, Cursor cursor) {
        TasksViewHolder tasksHolder = viewHolder;
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
        mDateHeaderMap.clear();
        mMonthHeaderMap.clear();
        mEisenBottomSheet.updateCursor(newCursor);
        WidgetProvider.refreshWidget(mContext);
        return super.swapCursor(newCursor);
    }

    public EisenBottomSheet getBottomSheet() {
        return mEisenBottomSheet;
    }

    public void setCurrentDateRow(int row) {
        mCurrantDateRow = row;
    }

    public int getCurrentDateRow() {
        return mCurrantDateRow;
    }
}
