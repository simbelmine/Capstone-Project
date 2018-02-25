package com.app.eisenflow.utils;

import android.os.Build;

/**
 * Created on 1/23/18.
 */

public class Constants {
    /**
     * Launch Activity
     */
    public static final String PREF_FIRST_TIME_USER = "FirstTimeUser";
    public static final int SPLASH_TIME = 3000;
    /**
     * Main Activity
     */
    public static final String TAG = "eisen";
    public static final int LOADER_ID = 0x02;
    public static final String EXTRA_TASK_PRIORITY = "TaskPriority";
    /**
     * SingleTask Activity
     */
    public static final int WEEKLY_OCCURRENCE = 1;
    /**
     * EisenContent Provider
     */
    public static final int TASK = 100;
    public static final int TASK_ID = 101;
    /**
     * Placeholder Fragment
     */
    public static final String ARG_SECTION_NUMBER = "section_number";
    /**
     * RecyclerItemSwipe Detector
     */
    public static final int MIN_LOCK_DISTANCE = 300; // disallow motion intercept
    public static final int MIN_DISTANCE = 100;
    public static final int DISTANCE = 70;
    public static final int ICON_SHOW_DELAY = 300;
    public static final int DISMISS_DELAY = 3000;
    public static final int ACTION_DELAY = 1500;
    public static final String EXTRA_TASK_POSITION = "ExtraTaskPosition";
    /**
     * TasksViewHolder
     */
    public static int MAX_PROGRESS = 100;
    /**
     * Alarms
     */
    public static final String
            LOCK_NAME_STATIC = "com.app.eisenflow.services.Static";
    public static final String
            LOCK_NAME_LOCAL = "com.app.eisenflow.services.Local";
    public static final String REPEATING_REMINDER = "RepeatingReminder";
    public static final String WEEK_DAY = "WeekDay";
    public static final String DAILY_TIP = "DailyTip";
    public static final String WEEKLY_OLD_TASKS_TIP = "WeeklyOldTasksTip";
    public static final String WEEKLY_WEEK_DAY = "WeeklyWeekDay";
    public static final String NOTIFICATION_REMINDER_CHANEL = "R_CH_ID";
    public static final int NOTIFICATION_REMINDER_ACTION_CODE = 303;
    public static final int DAILY_TIP_CODE = 401;
    public static final int WEEKLY_TIP_CODE = 402;

    /**
     * Widget
     */
    public static final String WIDGET_TO_TASK_ACTION = "WidgetToTaskAction";
    public static final String WIDGET_REFRESH_ACTION = "WidgetRefreshAction";
    public static final String WIDGET_DONE_ACTION = "WidgetDoneAction";
}
