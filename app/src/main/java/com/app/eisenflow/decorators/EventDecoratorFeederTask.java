package com.app.eisenflow.decorators;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_DATE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_TIME;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendar;

/**
 * Created on 2/17/18.
 */

public class EventDecoratorFeederTask extends AsyncTask<Void, Void, List<CalendarDay>> {
    private MaterialCalendarView mCalendarView;

    public EventDecoratorFeederTask(MaterialCalendarView calendarView) {
        mCalendarView = calendarView;
    }

    @Override
    protected List<CalendarDay> doInBackground(Void... voids) {
        Cursor cursor = getCursor();
        List<CalendarDay> dates = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                String date = cursor.getString(cursor.getColumnIndex(KEY_DATE));
                String time = cursor.getString(cursor.getColumnIndex(KEY_TIME));
                Calendar calendarDate = getCalendar(date, time);
                CalendarDay day = CalendarDay.from(calendarDate);
                dates.add(day);
            }
        }

        return dates;
    }

    @Override
    protected void onPostExecute(List<CalendarDay> list) {
        super.onPostExecute(list);
        Context context = ApplicationEisenFlow.getAppContext();
        if (list != null) {
            mCalendarView.addDecorator(new EventDecorator(
                    context.getResources().getColor(R.color.calendar_event_color),
                    list));
        }
    }
}