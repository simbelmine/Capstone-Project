package com.app.eisenflow.helpers;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.Task;
import com.app.eisenflow.services.OnAlarmReceiver;
import com.app.eisenflow.utils.DataUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_ROW_ID;
import static com.app.eisenflow.utils.Constants.DAILY_TIP;
import static com.app.eisenflow.utils.Constants.DAILY_TIP_CODE;
import static com.app.eisenflow.utils.Constants.REPEATING_REMINDER;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.utils.Constants.WEEKLY_OLD_TASKS_TIP;
import static com.app.eisenflow.utils.Constants.WEEKLY_TIP_CODE;
import static com.app.eisenflow.utils.Constants.WEEKLY_WEEK_DAY;
import static com.app.eisenflow.utils.Constants.WEEK_DAY;
import static com.app.eisenflow.utils.DataUtils.stringToIntegerCollection;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendar;
import static com.app.eisenflow.utils.DateTimeUtils.getCalendarTime;
import static com.app.eisenflow.utils.DateTimeUtils.getMonthDays;

/**
 * Created on 1/28/18.
 */

public class TaskReminderHelper {

    public static void setReminder(Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        long taskId = task.getId();
        Log.v(TAG, "--- taskId = " + taskId);
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, taskId);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    getCalendar(task.getDate(), task.getTime()).getTimeInMillis(),
                    getPendingIntent(context, intent, task.getId()));
        } else {
            alarmManager.set(
                    AlarmManager.RTC_WAKEUP,
                    getCalendar(task.getDate(), task.getTime()).getTimeInMillis(),
                    getPendingIntent(context, intent, task.getId()));
        }
    }

    public static void setRepeatingReminder(Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        long taskId = task.getId();
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, taskId)
                .putExtra(REPEATING_REMINDER, true);

        DataUtils.Occurrence reminderOccurrence = DataUtils.Occurrence.getOccurrenceType(task.getReminderOccurrence());
        switch (reminderOccurrence) {
            case DAILY:
                setDailyReminder(intent, task);
                break;
            case WEEKLY:
                setWeeklyReminder(intent, task);
                break;
            case MONTHLY:
                setMonthlyReminder(intent, task);
                break;
            case YEARLY:
                setYearlyReminder(intent, task);
                break;
        }
    }

    private static void setDailyReminder(Intent intent, Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar whenToRepeat = getCalendarTime(task.getTime());
        Calendar now = Calendar.getInstance();

        if (whenToRepeat.before(now)) {
            whenToRepeat.add(Calendar.DATE, 1);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(
                AlarmManager.RTC_WAKEUP,
                whenToRepeat.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                getPendingIntent(context, intent, task.getId()));
    }

    private static void setWeeklyReminder(Intent intent, Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar whenToRepeat = getCalendarTime(task.getTime());
        Calendar now = Calendar.getInstance();
        String weeklyOccurrenceString = task.getReminderWhen();
        List<Integer> weeklyOccurrenceList = (ArrayList<Integer>) stringToIntegerCollection(weeklyOccurrenceString);

        for (Integer weekDay : weeklyOccurrenceList) {
            // Set extra and action to distinct the different alarms.
            String weekDayStringValue = DataUtils.Occurrence.valueOf(weekDay);
            intent.putExtra(WEEK_DAY, weekDayStringValue);
            intent.setAction(weekDayStringValue);
            // Set when exactly to repeat.
            whenToRepeat.set(Calendar.DAY_OF_WEEK, weekDay);
            if (whenToRepeat.before(now)) {
                whenToRepeat.add(Calendar.DAY_OF_YEAR, 7);
            }
            // Set the alarm manager.
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            alarmManager.setRepeating(
                    AlarmManager.RTC_WAKEUP,
                    whenToRepeat.getTimeInMillis(),
                    AlarmManager.INTERVAL_DAY * 7,
                    getPendingIntent(context, intent, task.getId()));
        }
    }

    private static void setMonthlyReminder(Intent intent, Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar whenToRepeat = getCalendar(task.getDate(), task.getTime());
        Calendar now = Calendar.getInstance();

        // Check we aren't setting it in the past which will trigger it instantly.
        int daysInMonth = getMonthDays(task.getDate(), 0);

        if (whenToRepeat.before(now)) {
            whenToRepeat.add(Calendar.DAY_OF_MONTH, daysInMonth);
            daysInMonth = getMonthDays(task.getDate(), 1);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                whenToRepeat.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * daysInMonth,
                getPendingIntent(context, intent, task.getId()));
    }

    private static void setYearlyReminder(Intent intent, Task task) {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar whenToRepeat = getCalendar(task.getDate(), task.getTime());
        Calendar now = Calendar.getInstance();
        int daysToAdd = 365;
        //ToDo: leap year/s

        if (whenToRepeat.before(now)) {
            whenToRepeat.add(Calendar.DATE, daysToAdd);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                whenToRepeat.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * daysToAdd,
                getPendingIntent(context, intent, task.getId()));
    }

    /**
     * Daily Evening Alarms.
     */
    private static void setDailyTipAlarms() {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar now = Calendar.getInstance();
        Calendar whenToRepeat = getWhenToRepeat(Calendar.getInstance().get(Calendar.DAY_OF_WEEK), 20);
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, -1)
                .putExtra(DAILY_TIP, true)
                .setAction(DAILY_TIP);

        if (whenToRepeat.before(now)) {
            whenToRepeat.add(Calendar.DATE, 1);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                whenToRepeat.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY,
                getPendingIntent(context, intent, DAILY_TIP_CODE));
    }

    /**
     * Sunday evening alarms.
     * Triggered if old tasks are not Done.
     */
    private static void setWeeklyTipAlarms() {
        Context context = ApplicationEisenFlow.getAppContext();
        Calendar now = Calendar.getInstance();
        Calendar whenToRepeat = getWhenToRepeat(Calendar.SUNDAY, 18);
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, -1)
                .putExtra(WEEKLY_OLD_TASKS_TIP, true)
                .putExtra(WEEKLY_WEEK_DAY, Calendar.SUNDAY)
                .setAction(WEEKLY_OLD_TASKS_TIP);

        if (whenToRepeat.before(now)) {
            whenToRepeat.add(Calendar.DAY_OF_YEAR, 7);
        }
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                whenToRepeat.getTimeInMillis(),
                AlarmManager.INTERVAL_DAY * 7,
                getPendingIntent(context, intent, WEEKLY_TIP_CODE));
    }

    private static Calendar getWhenToRepeat(int dayOfWeek, int hour) {
        Calendar whenToRepeat;
        whenToRepeat = Calendar.getInstance();
        whenToRepeat.set(Calendar.DAY_OF_WEEK, dayOfWeek);
        whenToRepeat.set(Calendar.HOUR_OF_DAY, hour);
        whenToRepeat.set(Calendar.MINUTE, 0);
        whenToRepeat.set(Calendar.SECOND, 0);
        return whenToRepeat;
    }

    private static PendingIntent getPendingIntent(Context context, Intent intent, long id) {
        return PendingIntent.getBroadcast(
                context,
                (int) id,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public static void createDailyEveningTip() {
        if (!isDailyEveningTipSet()) {
            setDailyTipAlarms();
        }
    }

    public static boolean isWeeklyTipSet() {
        Context context = ApplicationEisenFlow.getAppContext();
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, -1)
                .putExtra(WEEKLY_OLD_TASKS_TIP, true)
                .putExtra(WEEKLY_WEEK_DAY, Calendar.SUNDAY)
                .setAction(WEEKLY_OLD_TASKS_TIP);
        return PendingIntent.getBroadcast(
                context,
                WEEKLY_TIP_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static boolean isDailyEveningTipSet() {
        Context context = ApplicationEisenFlow.getAppContext();
        Intent intent = new Intent(context, OnAlarmReceiver.class)
                .putExtra(KEY_ROW_ID, -1)
                .putExtra(DAILY_TIP, true)
                .setAction(DAILY_TIP);
        return PendingIntent.getBroadcast(
                context,
                DAILY_TIP_CODE,
                intent,
                PendingIntent.FLAG_NO_CREATE) != null;
    }

    public static void createWeeklyOldTasksTip() {
        if (!isWeeklyTipSet()) {
            setWeeklyTipAlarms();
        }
    }
}
