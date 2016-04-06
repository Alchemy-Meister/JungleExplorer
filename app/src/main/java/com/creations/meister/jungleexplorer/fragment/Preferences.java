package com.creations.meister.jungleexplorer.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import com.creations.meister.jungleexplorer.R;

/**
 * Created by meister on 4/6/16.
 */
public class Preferences extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        addPreferencesFromResource(R.xml.preferences);
    }
}
