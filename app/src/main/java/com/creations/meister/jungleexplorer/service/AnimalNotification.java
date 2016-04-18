package com.creations.meister.jungleexplorer.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v7.preference.PreferenceManager;
import android.widget.RemoteViews;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.activity.MainActivity;
import com.creations.meister.jungleexplorer.activity.NewAnimal;
import com.creations.meister.jungleexplorer.db.DBHelper;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.google_api_utils.GoogleApiHelper;
import com.creations.meister.jungleexplorer.widget.AnimalWidget;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * Created by meister on 4/16/16.
 */
public class AnimalNotification extends BroadcastReceiver implements GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;
    private Context context;
    SharedPreferences prefs;

    @Override
    public void onReceive(Context context, Intent intent) {
        this.context = context;

        AlarmService as = AlarmService.getInstance(context);

        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if(prefs.getBoolean("background_service", false)) {
            if (intent.getAction() != null
                    && intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED))
            {
                as.start();
            } else if (intent.getAction() != null
                    && intent.getAction().equals(AlarmService.ACTION))
            {
                if(GoogleApiHelper.isAPIAvailable(context)) {
                    mGoogleApiClient =  new GoogleApiClient.Builder(context)
                            .addConnectionCallbacks(this)
                            .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                                @Override
                                public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                                    // Nothing to do here.
                                }
                            })
                            .addApi(LocationServices.API)
                            .build();

                    // And connect!
                    mGoogleApiClient.connect();
                }
            }
        } else {
            as.stop();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //noinspection MissingPermission
        Location cLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(cLocation != null) {
            SharedPreferences.Editor sharedEditor = prefs.edit();
            sharedEditor.putString("current_latitude", String.valueOf(cLocation.getLatitude()));
            sharedEditor.putString("current_longitude", String.valueOf(cLocation.getLongitude()));
            sharedEditor.commit();

            DBHelper dbHelper = DBHelper.getHelper(this.context);
            Animal animal = dbHelper.getNearestAnimal(cLocation);
            if (animal != null) {
                SharedPreferences sharedPref = context.getApplicationContext()
                        .getSharedPreferences("notification_animal", Context.MODE_PRIVATE);
                String previousAnimalID = sharedPref.getString("previous_animal", null);
                if ((previousAnimalID != null && !String.valueOf(animal.getId()).equals(previousAnimalID))
                        || previousAnimalID == null) {
                    SharedPreferences.Editor editor = sharedPref.edit();
                    editor.putString("previous_animal", String.valueOf(animal.getId()));

                    Intent resultIntent = new Intent(context, NewAnimal.class);
                    resultIntent.putExtra("ANIMAL", animal);

                    Intent parentIntent = new Intent(context, MainActivity.class);
                    parentIntent.putExtra("SHOW_ANIMALS", true);

                    TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                    stackBuilder.addNextIntent(parentIntent);
                    stackBuilder.addNextIntent(resultIntent);

                    PendingIntent pIntent = stackBuilder.getPendingIntent(
                            0,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );

                    ComponentName thisWidget = new ComponentName(context, AnimalWidget.class);
                    AppWidgetManager manager = AppWidgetManager.getInstance(context);
                    RemoteViews remoteViews = new RemoteViews(context.getPackageName(),
                            R.layout.animal_widget);

                    remoteViews.setTextViewText(R.id.textView,
                            context.getString(R.string.nearest_animal, animal.getName()));
                    remoteViews.setOnClickPendingIntent(R.id.widget, pIntent);

                    manager.updateAppWidget(thisWidget, remoteViews);

                    editor.commit();
                    this.showNotification(animal, pIntent);
                }
            }
        }
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Nothing to do here.
    }

    private void showNotification(final Animal animal, PendingIntent pIntent) {
        // build notification
        // the addAction re-use the same intent to keep the example short
        NotificationCompat.Builder n  = new NotificationCompat.Builder(this.context)
                .setContentTitle(context.getString(R.string.animal_found))
                .setContentText(context.getString(R.string.touch_view))
                .setContentIntent(pIntent)
                .setAutoCancel(true)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setSmallIcon(R.drawable.ic_small_icon);


        NotificationManager notificationManager = (NotificationManager)
                this.context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(0, n.build());
    }
}
