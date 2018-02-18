package com.app.eisenflow;

import android.app.Activity;
import android.database.Cursor;
import android.os.AsyncTask;
import android.support.v7.widget.LinearSmoothScroller;
import android.support.v7.widget.RecyclerView;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import java.util.Calendar;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendar;

/**
 * Created on 2/18/18.
 */

public class ScrollToDateTask extends AsyncTask<Void, Void, Integer> {
    private Activity mActivity;
    private RecyclerView.LayoutManager mLinearLayoutManager;
    private CalendarDay mDate;

    public ScrollToDateTask(Activity activity, RecyclerView.LayoutManager manager, CalendarDay date) {
        mActivity = activity;
        mLinearLayoutManager = manager;
        mDate = date;
    }

    @Override
    protected Integer doInBackground(Void... voids) {
        Cursor cursor = getCursor();
        int counter = 0;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
                Calendar calendar = getCalendar(date, time);

                int month = mDate.getMonth();
                int day = mDate.getDay();

                if (calendar.get(Calendar.MONTH) == month && calendar.get(Calendar.DAY_OF_MONTH) == day) {
                    return counter;
                }
                counter++;
            }
        }
        return -1;
    }

    @Override
    protected void onPostExecute(Integer position) {
        super.onPostExecute(position);
        if (position != -1) {
            final RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(mActivity) {
                @Override
                protected int getVerticalSnapPreference() {
                    return LinearSmoothScroller.SNAP_TO_START;
                }
            };
            smoothScroller.setTargetPosition(position);
            mLinearLayoutManager.startSmoothScroll(smoothScroller);
        }
    }
}
