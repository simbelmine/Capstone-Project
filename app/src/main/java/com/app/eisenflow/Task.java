package com.app.eisenflow;

/**
 * Created on 12/20/17.
 */
public class Task {
    private long mId = -1;
    private int mPriority = -1;
    private String mTitle;
    private String mDate;
    private String mTime;
    private int mDateMillis = -1;
    private double mTotalDaysPeriod = -1;
    private int mReminderOccurrence;
    private String mReminderWhen;
    private String mReminderDate;
    private String mReminderTime;
    private String mNote;
    private int mProgress = -1;
    private int isDone = -1;
    private int isVibrationEnabled = -1;

    public long getId() {
        return mId;
    }

    public void setId(long id) {
        this.mId = id;
    }

    public int getPriority() {
        return mPriority;
    }

    public void setPriority(int priority) {
        this.mPriority = priority;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        this.mDate = date;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        this.mTime = time;
    }

    public int getDateMillis() {
        return mDateMillis;
    }

    public void setDateMillis(int dateMillis) {
        this.mDateMillis = dateMillis;
    }

    public double getTotalDaysPeriod() {
        return mTotalDaysPeriod;
    }

    public void setTotalDaysPeriod(double totalDaysPeriod) {
        this.mTotalDaysPeriod = totalDaysPeriod;
    }

    public int getReminderOccurrence() {
        return mReminderOccurrence;
    }

    public void setReminderOccurrence(int reminderOccurrence) {
        this.mReminderOccurrence = reminderOccurrence;
    }

    public String getReminderWhen() {
        return mReminderWhen;
    }

    public void setReminderWhen(String reminderWhen) {
        this.mReminderWhen = reminderWhen;
    }

    public String getReminderDate() {
        return mReminderDate;
    }

    public void setReminderDate(String reminderDate) {
        this.mReminderDate = reminderDate;
    }

    public String getReminderTime() {
        return mReminderTime;
    }

    public void setReminderTime(String reminderTime) {
        this.mReminderTime = reminderTime;
    }

    public String getNote() {
        return mNote;
    }

    public void setNote(String note) {
        this.mNote = note;
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
    }

    public int isDone() {
        return isDone;
    }

    public void setIsDone(int isDone) {
        this.isDone = isDone;
    }

    public int isVibrationEnabled() {
        return isVibrationEnabled;
    }

    public void setVibrationEnabled(int isVibrationEnabled) {
        this.isVibrationEnabled = isVibrationEnabled;
    }
}
