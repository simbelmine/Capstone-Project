package com.app.eisenflow;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.test.AndroidTestCase;

import com.app.eisenflow.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Created on 3/11/18.
 */
public class TimeStringTests extends AndroidTestCase {

    private static final Locale TW_LOCALE = new Locale("zh", "TW");
    private static final Locale ES_LOCALE = new Locale("es", "ES");
    private static final Locale DE_LOCALE = new Locale("de", "DE");

    private static final Locale[] LOCALES = new Locale[] {
            TW_LOCALE, Locale.US, Locale.TAIWAN, Locale.US, Locale.FRENCH, Locale.CANADA, Locale.UK,
            Locale.KOREAN, ES_LOCALE, DE_LOCALE
    };

    public void testCalendarToString() {
        for (Locale l : LOCALES) {
            testCalendarToStringWithLocale(l);
        }
    }


    public void testIsString24() {
        assertTrue(DateTimeUtils.isString24("11:39"));
        assertFalse(DateTimeUtils.isString24("11:39 am"));
        assertFalse(DateTimeUtils.isString24("11:39 pm"));
        assertTrue(DateTimeUtils.isString24("15:39"));
        assertTrue(DateTimeUtils.isString24("0:39"));
    }

    public void testTimeStringToCalendarAllLocales() {
        for (Locale l : LOCALES) {
            testTimeStringToCalendar(l, "11:39", 11, 39);
            testTimeStringToCalendar(l, "11:39 am", 11, 39);
            testTimeStringToCalendar(l, "11:39 pm", 23, 39);
            testTimeStringToCalendar(l, "00:39", 0, 39);
            testTimeStringToCalendar(l, "0:39", 0, 39);
            testTimeStringToCalendar(l, "1:39", 1, 39);
            testTimeStringToCalendar(l, "12:39", 12, 39);
            testTimeStringToCalendar(l, "12:39 am", 0, 39);
            testTimeStringToCalendar(l, "12:39 pm", 12, 39);
            testTimeStringToCalendar(l, "1:00 pm", 13, 00);
        }
    }

    public void testGetCalendar() {
        Calendar c = DateTimeUtils.getCalendar("Thu, Mar 29, 2018", "09:07");
        assertNotNull(c);
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 9);
        c = DateTimeUtils.getCalendar("Thu, Mar 29, 2018", "09:07 pm");
        assertNotNull(c);
        assertEquals(c.get(Calendar.HOUR_OF_DAY), 21);
    }

    private void testTimeStringToCalendar(
            Locale locale,
            String stringToTest,
            int hourToAssert, int minuteToAssert) {
        setLocale(locale);
        Date date = DateTimeUtils.getTime(stringToTest);
        System.out.println("date: " + date);
        assertNotNull(date);
        Calendar c = getCalendarFromDate(date);
        assertEquals(c.get(Calendar.HOUR_OF_DAY), hourToAssert);
        assertEquals(c.get(Calendar.MINUTE), minuteToAssert);
    }

    private void setLocale(Locale locale) {
        // here we update locale for date formatters
        Locale.setDefault(locale);
        // here we update locale for app resources
        Resources res = getContext().getResources();
        Configuration config = res.getConfiguration();
        config.locale = locale;
        res.updateConfiguration(config, res.getDisplayMetrics());
    }

    private void testCalendarToStringWithLocale(Locale locale) {
        Calendar c = Calendar.getInstance(locale);
        c.set(2000, 12, 12, 23, 23);

        assertTrue(DateTimeUtils.getTimeString(c).equalsIgnoreCase(isSystem24() ? "23:23" : "11:23 pm"));

        c.set(2000, 12, 12, 11, 23);
        assertTrue(DateTimeUtils.getTimeString(c).equalsIgnoreCase(isSystem24() ? "11:23" : "11:23 am"));
    }
    private Calendar getCalendarFromDate(Date date) {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        return c;
    }

    private boolean isSystem24() {
        return DateTimeUtils.isSystem24hFormat();
    }

}
