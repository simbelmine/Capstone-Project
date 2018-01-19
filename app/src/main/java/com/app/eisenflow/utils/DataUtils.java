package com.app.eisenflow.utils;

import android.text.TextUtils;
import android.view.View;

import com.app.eisenflow.ApplicationEisenFlow;
import com.app.eisenflow.R;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created on 12/23/17.
 */

public class DataUtils {
    // Enum to set priority highest to lowest (1 to 4).
    public enum Priority {
        DEFAULT(0),
        ONE(1),   // Red
        TWO(2),   // Green
        THREE(3), // Yellow
        FOUR(4);  // Gray

        private static Map map = new HashMap<>();
        private int value;
        Priority(int value) {
            this.value = value;
        }

        static {
            for (Priority pageType : Priority.values()) {
                map.put(pageType.value, pageType);
            }
        }

        public static Priority valueOf(int pageType) {
            Priority priority = (Priority) map.get(pageType);
            if (priority == null) {
                return DEFAULT;
            }
            return priority ;
        }

        public int getValue() {
            return value;
        }
    }

    public enum Occurrence {
        DAILY(ApplicationEisenFlow.getAppContext().getString(R.string.daily_txt), 0),
        WEEKLY(ApplicationEisenFlow.getAppContext().getString(R.string.weekly_txt), 1),
        MONTHLY(ApplicationEisenFlow.getAppContext().getString(R.string.monthly_txt), 2),
        YEARLY(ApplicationEisenFlow.getAppContext().getString(R.string.yearly_txt), 3);

        private static Map map = new HashMap<>();
        private int value;
        private String strValue;
        Occurrence(String strValue, int value) {
            this.strValue = strValue;
            this.value = value;
        }

        static {
            for (Occurrence pageType : Occurrence.values()) {
                map.put(pageType.value, pageType.strValue);
            }
        }

        public static String valueOf(int value) {
            String occurrence = (String) map.get(value);
            return occurrence ;
        }

        public static Occurrence getOccurrenceType(int value) {
            return Occurrence.values()[value];
        }
    }

    public enum When {
        MON(ApplicationEisenFlow.getAppContext().getString(R.string.mon_txt), 0),
        TUE(ApplicationEisenFlow.getAppContext().getString(R.string.tue_txt), 1),
        WED(ApplicationEisenFlow.getAppContext().getString(R.string.wed_txt), 2),
        THU(ApplicationEisenFlow.getAppContext().getString(R.string.thu_txt), 3),
        FRI(ApplicationEisenFlow.getAppContext().getString(R.string.fri_txt), 4),
        SAT(ApplicationEisenFlow.getAppContext().getString(R.string.sat_txt), 5),
        SUN(ApplicationEisenFlow.getAppContext().getString(R.string.sun_txt), 6);

        private static Map map = new HashMap<>();
        private int value;
        private String strValue;
        When(String strValue, int value) {
            this.strValue = strValue;
            this.value = value;
        }

        static {
            for (When pageType : When.values()) {
                map.put(pageType.value, pageType.strValue);
            }
        }

        public static String valueOf(int value) {
            String when = (String) map.get(value);
            return when ;
        }
    }

    public static int getVibrationStateValue(boolean state) {
        return state? 1 : 0;
    }

    public static boolean getBooleanValue(int value) {
        return (value == 1) ? true : false;
    }

    public static String integerCollectionToString(Collection<Integer> collection) {
        StringBuilder sb = new StringBuilder();
        for (Object s : collection)
        {
            sb.append(String.valueOf(s));
            sb.append("\t");
        }

        return sb.toString();
    }

    public static Collection<Integer> stringToIntegerCollection(String str) {
        if (TextUtils.isEmpty(str)) {
            return null;
        }
        Collection<Integer> collection = new HashSet<>();
        String[] strArray = str.split("\t");
        for(int i = 0; i < strArray.length; i++) {
            collection.add(Integer.valueOf(strArray[i]));
        }
        return collection;
    }

    public static void setViewVisibility(View view, int visibility) {
        view.setVisibility(visibility);
    }

    public static int getTaskTextWidth(View view) {
        int totalWidth = 0;
        View chView = view;
        chView.measure(
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        totalWidth += chView.getMeasuredWidth();

        return totalWidth;
    }
}
