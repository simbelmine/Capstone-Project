package com.app.eisenflow.utils;

import android.text.TextUtils;
import android.view.View;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * Created on 12/23/17.
 */

public class DataUtils {
    // Enum to set priority highest to lowest (1 to 4).
    public enum Priority {
        DEFAULT(0),
        ONE(1),
        TWO(2),
        THREE(3),
        FOUR(4);

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
