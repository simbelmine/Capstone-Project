package com.app.eisenflow;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created on 12/20/17.
 */
public class Task implements Parcelable {
    private long mId = -1;
    private int mPriority = -1;
    private String mTitle;
    private String mDate;
    private String mTime;
    private long mDateMillis = -1;
    private double mTotalDaysPeriod = -1;
    private int mReminderOccurrence = 0; //Daily occurrence by default.
    private String mReminderWhen = "";
    private String mReminderDate;
    private String mReminderTime;
    private String mAddress;
    private String mLocation;
    private String mNote;
    private int mProgress = -1;
    private int isDone = -1;
    private int isVibrationEnabled = 1;

    public Task (Parcel in) {
        this.mId = in.readLong();
        this.mPriority = in.readInt();
        this.mTitle = in.readString();
        this.mDate = in.readString();
        this.mTime = in.readString();
        this.mDateMillis = in.readLong();
        this.mTotalDaysPeriod = in.readDouble();
        this.mReminderOccurrence = in.readInt();
        this.mReminderWhen = in.readString();
        this.mReminderDate = in.readString();
        this.mReminderTime = in.readString();
        this.mAddress = in.readString();
        this.mLocation = in.readString();
        this.mNote = in.readString();
        this.mProgress = in.readInt();
        this.isDone = in.readInt();
        this.isVibrationEnabled = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int i) {
        dest.writeLong(mId);
        dest.writeInt(mPriority);
        dest.writeString(mTitle);
        dest.writeString(mDate);
        dest.writeString(mTime);
        dest.writeLong(mDateMillis);
        dest.writeDouble(mTotalDaysPeriod);
        dest.writeInt(mReminderOccurrence);
        dest.writeString(mReminderWhen);
        dest.writeString(mReminderDate);
        dest.writeString(mReminderTime);
        dest.writeString(mAddress);
        dest.writeString(mLocation);
        dest.writeString(mNote);
        dest.writeInt(mProgress);
        dest.writeInt(isDone);
        dest.writeInt(isVibrationEnabled);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>() {
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };

    public Task () {
    }

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

    public long getDateMillis() {
        return mDateMillis;
    }

    public void setDateMillis(long dateMillis) {
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

    public String getAddress () {
        return mAddress;
    }

    public void setAddress(String address) {
        this.mAddress = address;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
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
