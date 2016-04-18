package com.creations.meister.jungleexplorer.google_api_utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

/**
 * Created by meister on 4/18/16.
 */
public class GoogleApiHelper {
    public static boolean isAPIAvailable(Context context) {
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(context);
        if (result == ConnectionResult.SUCCESS) {
            return true;
        }
        return false;
    }
}
