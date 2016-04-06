package com.creations.meister.jungleexplorer.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.fragment.Preferences;

/**
 * Created by meister on 4/6/16.
 */
public class PreferencesActivity extends AppCompatActivity{

    FragmentManager mFragmentManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.activity_main);

        mFragmentManager = this.getSupportFragmentManager();

        FragmentTransaction transaction = mFragmentManager.beginTransaction();
        transaction.replace(R.id.contentFragment, new Preferences(), "SETTINGS");
        transaction.commit();
    }
}
