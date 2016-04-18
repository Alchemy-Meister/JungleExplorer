package com.creations.meister.jungleexplorer.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v14.preference.SwitchPreference;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.EditTextPreferenceFix;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompatFix;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;
import com.creations.meister.jungleexplorer.service.AlarmService;

/**
 * Created by meister on 4/6/16.
 */
public class Preferences extends PreferenceFragmentCompatFix {

    private static final int REQUEST_LOCATION = 101;

    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION
    };

    private SwitchPreference backgroundService;

    private AlarmService alarmService;

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);

        alarmService = AlarmService.getInstance(this.getContext());

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

        SwitchPreference animalFilter = (SwitchPreference) findPreference("filter_animal_list");
        final EditTextPreferenceFix distanceRadius =
                (EditTextPreferenceFix) findPreference("animal_list_radius");

        if(animalFilter.isChecked()) {
            distanceRadius.setEnabled(true);
        } else {
            distanceRadius.setEnabled(false);
        }

        animalFilter.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if((boolean) newValue) {
                    int hasLocationPermission = ContextCompat.checkSelfPermission(
                            Preferences.this.getContext(),
                            requiredPermissions[0]);
                    if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                        Preferences.this.requestPermissions(requiredPermissions, REQUEST_LOCATION);
                        return false;
                    }

                    distanceRadius.setEnabled(true);
                    return true;
                } else {
                    distanceRadius.setEnabled(false);
                    return true;
                }
            }
        });

        Resources res = getResources();
        TypedArray ta = res.obtainTypedArray(R.array.continents);
        int n = ta.length();
        String[][] continentArray = new String[n][];
        for (int i = 0; i < n; ++i) {
            int id = ta.getResourceId(i, 0);
            if (id > 0) {
                continentArray[i] = res.getStringArray(id);
            }
        }
        ta.recycle();

        String[] continentNames = new String[continentArray.length];
        String[] values = new String[continentArray.length];
        for(int i = 0; i < continentArray.length; i++) {
            continentNames[i] = continentArray[i][0];
            values[i] = String.valueOf(i);
        }

        ListPreference continents = (ListPreference) findPreference("map_default_continent");
        continents.setEntries(continentNames);
        continents.setEntryValues(values);

        int hasLocationPermission = ContextCompat.checkSelfPermission(
                Preferences.this.getContext(),
                requiredPermissions[0]);
        if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
            backgroundService.setChecked(false);
            alarmService.stop();
            animalFilter.setChecked(false);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION:
                if (grantResults.length > 0 && grantResults[0] != PackageManager.PERMISSION_GRANTED) {
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
