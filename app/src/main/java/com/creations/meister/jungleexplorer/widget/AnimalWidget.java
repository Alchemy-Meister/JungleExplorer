package com.creations.meister.jungleexplorer.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.widget.RemoteViews;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;

/**
 * Created by meister on 4/17/16.
 */
public class AnimalWidget extends AppWidgetProvider {

    private DBHelper dbHelper;
    private SharedPreferences prefs;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String lat = prefs.getString("current_latitude", null);
        String lng = prefs.getString("current_longitude",null);

        Log.d("LAT", lat);
        Log.d("LNG", lng);

        Location cLocation = null;
        Animal animal = null;

        if(lat != null && lng != null) {
            cLocation = new Location("");
            cLocation.setLatitude(Double.valueOf(lat));
            cLocation.setLongitude(Double.valueOf(lng));

            dbHelper = DBHelper.getHelper(context);
            animal = dbHelper.getNearestAnimal(cLocation);
        }

        for (int i = 0; i < appWidgetIds.length; i++) {
            int appWidgetId = appWidgetIds[i];

            RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.animal_widget);

            Intent resultIntent = new Intent(context, NewAnimal.class);

            Intent parentIntent = new Intent(context, MainActivity.class);
            parentIntent.putExtra("SHOW_ANIMALS", true);

            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(parentIntent);
            stackBuilder.addNextIntent(resultIntent);

            if(animal != null) {
                remoteViews.setTextViewText(R.id.textView, animal.getName());

                resultIntent.putExtra("ANIMAL", animal);

                PendingIntent pIntent = stackBuilder.getPendingIntent(
                        0,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

                remoteViews.setOnClickPendingIntent(R.id.widget, pIntent);
            } else {
                remoteViews.setTextViewText(R.id.textView,
                        context.getResources().getString(R.string.no_animal_available));
            }

            appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        }
    }
}
