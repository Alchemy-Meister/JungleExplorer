package com.creations.meister.jungleexplorer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.TaskStackBuilder;
import android.widget.RemoteViews;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.activity.NewAnimal;

/**
 * Created by meister on 4/17/16.
 */
public class AnimalWidget extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.animal_widget);
            remoteViews.setTextViewText(R.id.textView, String.valueOf(666));

            Intent resultIntent = new Intent(context, NewAnimal.class);

            Intent parentIntent = new Intent(context, MainActivity.class);
            parentIntent.putExtra("SHOW_ANIMALS", true);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(parentIntent);
            stackBuilder.addNextIntent(resultIntent);

            PendingIntent pIntent = stackBuilder.getPendingIntent(
                    0,
                    PendingIntent.FLAG_UPDATE_CURRENT
            );

            remoteViews.setOnClickPendingIntent(R.id.widget, pIntent);

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
