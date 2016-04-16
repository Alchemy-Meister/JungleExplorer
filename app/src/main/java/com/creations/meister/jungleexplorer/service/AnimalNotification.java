package com.creations.meister.jungleexplorer.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Vibrator;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

/**
 * Created by meister on 4/16/16.
 */
public class AnimalNotification extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Log.d("ON", String.valueOf(prefs.getBoolean("background_service", false)));

        if(prefs.getBoolean("background_service", false)) {
            Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

            // Vibrate for 300 milliseconds
            v.vibrate(3000);
        }
    }
}
