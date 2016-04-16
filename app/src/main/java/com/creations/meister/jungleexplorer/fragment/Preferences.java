package com.creations.meister.jungleexplorer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;
import com.creations.meister.jungleexplorer.service.AlarmService;

/**
 * Created by meister on 4/6/16.
 */
public class Preferences extends PreferenceFragmentCompat {

    private static final int REQUEST_LOCATION = 101;

    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private SwitchPreference backgroundService;

    private AlarmService alarmService;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        backgroundService = (SwitchPreference) findPreference("background_service");
        backgroundService.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((Boolean) newValue) {
                    int hasLocationPermission = ContextCompat.checkSelfPermission(
                            Preferences.this.getContext(),
                            requiredPermissions[0]);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        Preferences.this.requestPermissions(requiredPermissions, REQUEST_LOCATION);
                        return false;
                    }
                    alarmService.start();
                    return true;
                } else {
                    alarmService.stop();
                    return true;
                }
            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        alarmService = AlarmService.getInstance(this.getContext());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    backgroundService.setChecked(true);
                    alarmService.start();
                } else {
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                            R.string.location_permission_message,
                            getResources().getString(R.string.app_name)), Preferences.this.getContext());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }
}
