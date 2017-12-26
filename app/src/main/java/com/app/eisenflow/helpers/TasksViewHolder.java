package com.app.eisenflow.helpers;

import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TITLE;

/**
 * Created on 12/25/17.
 */

public class TasksViewHolder extends RecyclerView.ViewHolder {
    public TextView textView1;

    public TasksViewHolder(View itemView) {
        super(itemView);
        textView1 = itemView.findViewById(android.R.id.text1);
    }

    public void setData(Cursor c) {
        textView1.setText(c.getString(c.getColumnIndex(KEY_TITLE)));
    }
}
