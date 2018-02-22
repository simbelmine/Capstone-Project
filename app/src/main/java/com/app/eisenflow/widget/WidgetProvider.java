package com.app.eisenflow.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

import com.app.eisenflow.R;
import com.app.eisenflow.activities.MainActivity;

import static com.app.eisenflow.utils.Constants.WIDGET_TO_TASK_ACTION;

/**
 * Implementation of App Widget functionality.
 */
public class WidgetProvider extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        CharSequence widgetText = context.getString(R.string.appwidget_text);
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_provider);
        //views.setTextViewText(R.id.appwidget_text, widgetText);
        // this was setting the text to the only textView inside the widget

        //RemoteViews Service needed to provide adapter for ListView
        Intent serviceIntent = new Intent(context, UpdateWidgetService.class);
        //passing app widget id to that RemoteViews Service
        serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //setting a unique Uri to the intent
        //don't know its purpose to me right now
        serviceIntent.setData(Uri.parse(
                serviceIntent.toUri(Intent.URI_INTENT_SCHEME)));
        //setting adapter to listview of the widget
        views.setRemoteAdapter(appWidgetId, R.id.widget_list_view,
                serviceIntent);


        // template to handle the click listener for each list item
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
        }
            super.onReceive(context, intent);
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

