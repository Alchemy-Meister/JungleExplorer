package com.creations.meister.jungleexplorer.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.meister.jungleexplorer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

/**
 * Created by meister on 4/7/16.
 */
public class AnimalLocation extends Fragment implements OnMapReadyCallback {

    SupportMapFragment fragment;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.location_map, container, false);

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        FragmentManager fm = this.getActivity().getSupportFragmentManager();
//        fragment = (SupportMapFragment) fm.findFragmentById(R.id.map);
//        fragment = SupportMapFragment.newInstance();
//        fm.beginTransaction().replace(R., fragment).commit();
        if(fragment != null)
            fragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {

    }
}
