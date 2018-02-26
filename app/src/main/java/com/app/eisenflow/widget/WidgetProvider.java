package com.app.eisenflow.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.app.eisenflow.R;
import com.app.eisenflow.activities.MainActivity;

import static com.app.eisenflow.database.EisenContract.TaskEntry.KEY_IS_DONE;
import static com.app.eisenflow.database.EisenContract.TaskEntry.getCursor;
import static com.app.eisenflow.utils.Constants.EXTRA_TASK_POSITION;
import static com.app.eisenflow.utils.Constants.TAG;
import static com.app.eisenflow.utils.Constants.WIDGET_DONE_ACTION;
import static com.app.eisenflow.utils.Constants.WIDGET_REFRESH_ACTION;
import static com.app.eisenflow.utils.Constants.WIDGET_TO_TASK_ACTION;
import static com.app.eisenflow.utils.DataUtils.getBooleanState;
import static com.app.eisenflow.utils.TaskUtils.updateTaskDoneState;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object.
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_provider);

        // RemoteViews Service needed to provide adapter for ListView
        Intent serviceIntent = new Intent(context, UpdateWidgetService.class);
        // Pass the app widget id to that RemoteViews Service.
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        // Set a unique Uri to the intent.
        serviceIntent.setData(Uri.parse(
                serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        // Set the adapter to listview of the widget.
        views.setRemoteAdapter(appWidgetId, R.id.widget_list_view,
                serviceIntent);

        // Create template to handle the click listener for each list item.
        Intent clickIntentTemplate = new Intent(context, WidgetProvider.class);
        PendingIntent clickPendingIntentTemplate = PendingIntent.getBroadcast(
                context,
                0,
                clickIntentTemplate,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate);

        // Click on Home icon.
        Intent clickIntentHome = new Intent(context, MainActivity.class);
        PendingIntent clickPendingIntentHome = PendingIntent.getActivity(
                context,
                0,
                clickIntentHome,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_home_icon, clickPendingIntentHome);

        // Click on Refresh icon.
        Intent clickIntentRefresh = new Intent(context, WidgetProvider.class);
        clickIntentRefresh.setAction(WIDGET_REFRESH_ACTION);
        PendingIntent clickPendingIntentRefresh = PendingIntent.getBroadcast(
                context,
                0,
                clickIntentRefresh,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_refresh_icon, clickPendingIntentRefresh);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        switch (intent.getAction()) {
            case WIDGET_TO_TASK_ACTION:
                Intent intentToStart = new Intent(context, MainActivity.class);
                intentToStart.putExtras(intent);
                context.startActivity(intentToStart);
                break;
            case WIDGET_REFRESH_ACTION:
                refreshWidget(context);
                break;
            case WIDGET_DONE_ACTION:
                int position = intent.getIntExtra(EXTRA_TASK_POSITION, -1);
                Cursor cursor = getCursor();
                if (cursor != null && cursor.moveToPosition(position)) {
                    int isDoneValue = cursor.getInt(cursor.getColumnIndex(KEY_IS_DONE));
                    boolean isDone = getBooleanState(isDoneValue);
                    updateTaskDoneState(context, cursor, position, !isDone); // Get the opposite value to save.
                    refreshWidget(context);
                }
                break;
        }
            super.onReceive(context, intent);
    }

    public static void refreshWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidgetComponentName =
                new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_list_view);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

