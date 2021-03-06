package com.app.eisenflow.utils;

import android.content.Context;
import android.os.Build;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import com.app.eisenflow.ApplicationEisenFlow;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.app.eisenflow.utils.Constants.TAG;

/**
 * Created on 12/23/17.
 */
public class DateTimeUtils {
    private static final String DATE_FORMAT = "EEE, MMM dd, yyyy";
    private static final String TIME_FORMAT_24 = "kk:mm";
    private static final String TIME_FORMAT_AP_PM = "hh:mm a";
    public static final long MILLIS_IN_A_DAY = 24 * 60 * 60 * 1000;
    public HashMap<String, Integer> dayOfMonthsMap = new HashMap<>();

    private static Pattern pattern24 = null;
    private static final String TIME24HOURS_PATTERN_ONLY =
            "^([01]?[0-9]|2[0-3]):[0-5][0-9]$";
    private static final String TIME24HOURS_PATTERN =
            "([01]?[0-9]|2[0-3]):[0-5][0-9]";

    private Context context;

    public DateTimeUtils(Context context) {
        this.context = context;

        populateDayOfMonthsMap();
    }

    private void populateDayOfMonthsMap() {
        dayOfMonthsMap.put("Mon", Calendar.MONDAY);
        dayOfMonthsMap.put("Tue", Calendar.TUESDAY);
        dayOfMonthsMap.put("Wed", Calendar.WEDNESDAY);
        dayOfMonthsMap.put("Thu", Calendar.THURSDAY);
        dayOfMonthsMap.put("Fri", Calendar.FRIDAY);
        dayOfMonthsMap.put("Sat", Calendar.SATURDAY);
        dayOfMonthsMap.put("Sun", Calendar.SUNDAY);
    }

    public static Date getDate(String dateStr) {
        if(dateStr != null && !"".equals(dateStr)) {
            SimpleDateFormat postFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
            try {
                return postFormatter.parse(dateStr);
            } catch (ParseException ex) {
                Log.e(TAG, "String to Date Formatting Exception : " + ex.getMessage());
            }
        }

        return null;
    }

    public static String getDateString(Calendar cal) {
        SimpleDateFormat postFormatter = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        return postFormatter.format(cal.getTime());
    }

    public static boolean isDateValid(String dateStr) {
        Calendar currDate = Calendar.getInstance();
        Calendar date  = Calendar.getInstance();
        date.setTime(getDate(dateStr));

        if(date.get(Calendar.MONTH) == currDate.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) < currDate.get(Calendar.DAY_OF_MONTH)) {
            return false;
        }

        return true;
    }

    public static boolean isTimeValid(String dateStr, String timeStr) {
        Calendar currDate = Calendar.getInstance();
        Calendar currTime = Calendar.getInstance();
        currTime.setTime(getTime(getTimeString(Calendar.getInstance())));
        Calendar date  = Calendar.getInstance();
        date.setTime(getDate(dateStr));
        Calendar time  = Calendar.getInstance();
        time.setTime(getTime(timeStr));

        if(date.get(Calendar.MONTH) == currDate.get(Calendar.MONTH) &&
                date.get(Calendar.DAY_OF_MONTH) == currDate.get(Calendar.DAY_OF_MONTH) &&
                time.getTimeInMillis() < currTime.getTimeInMillis()) {
            return false;
        }

        return true;
    }

    public static String getTimeString(Calendar cal) {
        SimpleDateFormat postFormatter;
        if(isSystem24hFormat()) {
            postFormatter = new SimpleDateFormat(TIME_FORMAT_24, Locale.US);
        }
        else {
            postFormatter = new SimpleDateFormat(TIME_FORMAT_AP_PM, Locale.US);
        }
        return postFormatter.format(cal.getTime());
    }

    public static Date getTime(String timeStr) {
        SimpleDateFormat postFormatter;
        if(isSystem24hFormat()) {
            if(isString24(timeStr)) {
                postFormatter = new SimpleDateFormat(TIME_FORMAT_24, Locale.US);
            } else {
                postFormatter = new SimpleDateFormat(TIME_FORMAT_AP_PM, Locale.US);
            }
        }
        else {
            if(!isString24(timeStr)) {
                postFormatter = new SimpleDateFormat(TIME_FORMAT_AP_PM, Locale.US);
            } else {
                postFormatter = new SimpleDateFormat(TIME_FORMAT_24, Locale.US);
            }
        }

        try {
            return postFormatter.parse(timeStr);
        }
        catch (ParseException ex) {
            Log.e(TAG, "String to Time Formatting Exception : " + ex.getMessage());
        }

        return null;
    }

    @VisibleForTesting
    public static boolean isString24(String timeStr) {
        if (pattern24 == null) {
            pattern24 = Pattern.compile(TIME24HOURS_PATTERN_ONLY);
        }
        return pattern24.matcher(timeStr).find();
    }


    public static Date getTime24(String timeStr) {
        SimpleDateFormat postFormatter;
        postFormatter = new SimpleDateFormat(TIME_FORMAT_24, Locale.US);

        try {
            return postFormatter.parse(timeStr);
        }
        catch (ParseException ex) {
            Log.e(TAG, "String to Time Formatting Exception 24 : " + ex.getMessage());
        }

        return null;
    }

    public static Date getTime12(String timeStr) {
        SimpleDateFormat postFormatter;
        postFormatter = new SimpleDateFormat(TIME_FORMAT_AP_PM, Locale.US);

        try {
            return postFormatter.parse(timeStr);
        }
        catch (ParseException ex) {
            Log.e(TAG, "String to Time Formatting Exception 24 : " + ex.getMessage());
        }

        return null;
    }

    public static boolean isSystem24hFormat() {
        if(android.text.format.DateFormat.is24HourFormat(ApplicationEisenFlow.getAppContext()))
            return true;
        return false;
    }

    public static Calendar getCalendar(String date, String time) {
        String dateTime = date + " " + time;
        SimpleDateFormat postFormatter;
        String newFormat;
        if(isString24(time)) {
            newFormat = DATE_FORMAT + " " + TIME_FORMAT_24;
            postFormatter = new SimpleDateFormat(newFormat, Locale.US);
        }
        else {
            newFormat = DATE_FORMAT + " " + TIME_FORMAT_AP_PM;
            postFormatter = new SimpleDateFormat(newFormat, Locale.US);
        }

        try {
            Calendar cal = Calendar.getInstance();
            Date calDate = postFormatter.parse(dateTime);
            if(calDate != null) {
                cal.setTime(calDate);
                return cal;
            }
        }
        catch (ParseException ex) {
            Log.e(TAG, "String to Time Formatting Exception : " + ex.getMessage());
        }

        return null;
    }

    public static String getMonthName(Calendar cal) {
        return cal.getDisplayName(Calendar.MONTH, Calendar.LONG, getLocale());
    }

    private static Locale getLocale() {
        Context context = ApplicationEisenFlow.getAppContext();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N){
            return context.getResources().getConfiguration().getLocales().get(0);
        } else{
            //noinspection deprecation
            return context.getResources().getConfiguration().locale;
        }
    }

    public static int getMonthDays(String date, int monthToAdd) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getDate(date));
        int month = cal.get(Calendar.MONTH);
        month = month + monthToAdd;
        cal.set(Calendar.MONTH, month);

        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static Calendar getCalendarTime(String time) {
        Calendar cal = Calendar.getInstance();
        String[] splitTimeStr = time.split(":");
        String hours = getNonLeadingZero(splitTimeStr[0]);
        String mins;

        if(isSystem24hFormat()) {
            mins = getNonLeadingZero(splitTimeStr[1]);
            cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(hours));
        }
        else {
            mins = getNonLeadingZero(get12HoursMins(splitTimeStr[1]));
            int am_pm = get12HoursAM_PM(splitTimeStr[1]);
            cal.set(Calendar.AM_PM, am_pm);
            cal.set(Calendar.HOUR, Integer.parseInt(hours));
        }
        cal.set(Calendar.MINUTE, Integer.parseInt(mins));
        cal.set(Calendar.SECOND, 0);

        return cal;
    }

    private static String get12HoursMins(String s) {
        String[] split = s.split(" ");
        return split[0];
    }


    private static int get12HoursAM_PM(String s) {
        String[] split = s.split(" ");
        if("AM".equals(split[1])) return Calendar.AM;
        else if("PM".equals(split[1])) return Calendar.PM;

        return Calendar.AM;
    }

    // Thu, Aug 18, 2016
    public static Calendar getCalendarDateWithTime(String date, String time) {
        Calendar cal = getCalendarTime(time);

        Calendar dateCal = Calendar.getInstance();
        dateCal.setTime(getDate(date));
        int monthNum = dateCal.get(Calendar.MONTH);
        int dateNum = dateCal.get(Calendar.DAY_OF_MONTH);


        cal.set(Calendar.MONTH, monthNum);
        cal.set(Calendar.DAY_OF_MONTH, dateNum);

        return cal;
    }

    private static String getNonLeadingZero(String str) {
        return str.replaceFirst("^0+(?!$)", "");
    }

    public static boolean isPastDate(Calendar calDate){
        Calendar now = Calendar.getInstance();
        long nowInMillis = now.getTimeInMillis();
        long dateInMillis = calDate.getTimeInMillis();

        return (dateInMillis < nowInMillis);
    }

    public static String getActualTime(String time) {
        if(time != null && time.length() != 0) {
            Pattern pattern = Pattern.compile(TIME24HOURS_PATTERN);
            Matcher matcher = pattern.matcher(time);

            if (isSystem24hFormat()) {
                if (!matcher.matches()) {
                    // 12 to 24
                    Date d = getTime12(time);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    return getTimeString(cal);
                }
            } else {
                if (matcher.matches()) {
                    // 24 to 12
                    Date d = getTime24(time);
                    Calendar cal = Calendar.getInstance();
                    cal.setTime(d);
                    return getTimeString(cal);
                }
            }
        }

        return time;
    }

    public static boolean isTimerTimeValid(int hours, int minutes) {
        return (hours >= 0 && minutes > 0);
    }

    public static long getTimerTimeInMillis(int hour, int minutes) {
        return getTimerHourInMillis(hour) + getTimerMinutesInMillis(minutes);
    }

    public static long getTimerHourInMillis(int hour) {
        return TimeUnit.HOURS.toMillis(hour);
    }

    public static long getTimerMinutesInMillis(int minutes) {
        return TimeUnit.MINUTES.toMillis(minutes);
    }

    public static String getCorrectTimerTimeValue(long value) {
        return String.format(Locale.US, "%02d", value);
    }
}
