package com.creations.meister.jungleexplorer.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by meister on 4/16/16.
 */
public class AlarmService {
    private static final int TRIGGER_AT_MILLIS = 60;

    public static final String ACTION = "ANIMAL_NOTIFICATION";

    private static AlarmService alarmService;

    private Context context;
    private PendingIntent mPendingIntent;
    private AlarmManager alarmManager;

    public static AlarmService getInstance(Context context) {
        if (alarmService == null) {
            alarmService = new AlarmService();
            alarmService.context = context;
            alarmService.alarmManager = (AlarmManager)context.getSystemService(
                    Context.ALARM_SERVICE);
        }
        return alarmService;
    }

    public void start(){
        Intent intent = new Intent(context, AnimalNotification.class);
        intent.setAction(ACTION);
        alarmService.mPendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        alarmService.alarmManager.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP, TRIGGER_AT_MILLIS * 1000, 10, mPendingIntent);
    }

    public void stop(){
        if(alarmService.mPendingIntent != null) {
            alarmService.mPendingIntent.cancel();
            alarmService.alarmManager.cancel(mPendingIntent);
        }
    }
}
