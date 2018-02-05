package com.app.eisenflow.services;

import android.content.Intent;
import android.database.Cursor;
import android.support.annotation.Nullable;

import com.app.eisenflow.Task;
import com.app.eisenflow.helpers.TaskReminderHelper;
import com.app.eisenflow.utils.DataUtils;

import static com.app.eisenflow.database.EisenContract.TaskEntry.cursorToTask;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.DataUtils.Priority.TWO;

/**
 * Created on 1/28/18.
 */

public class TaskReminderService extends WakefulIntentService {
    public TaskReminderService() {
        super("TaskReminderService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        // ToDo: set the alarms here.

        TaskReminderHelper taskReminder = new TaskReminderHelper();
        Cursor cursor = getCursor();
        try {
            while (cursor.moveToNext()) {
                Task task = cursorToTask(cursor);
                DataUtils.Priority priority = DataUtils.Priority.valueOf(task.getPriority());

                if (priority == TWO) {
                    taskReminder.setRepeatingReminder(task);
                } else {
                    taskReminder.setReminder(task);
                }
            }
        } finally {
            cursor.close();
        }

        super.onHandleIntent(intent);
    }
}
