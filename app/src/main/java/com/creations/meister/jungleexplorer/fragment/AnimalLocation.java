package com.creations.meister.jungleexplorer.fragment;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.creations.meister.jungleexplorer.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Created by meister on 4/7/16.
 */
public class AnimalLocation extends SupportMapFragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener, GoogleMap.OnMarkerDragListener
{

    Marker mMarker;
    MarkerOptions mMarkerOptions;
    GoogleMap mMap;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.mMap = map;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMarkerDragListener(this);
        map.setOnMapClickListener(this);
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if(mMarker == null) {
            mMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title("Location")
                    .snippet(this.getLocationName(latLng))
                    .draggable(true);

            mMarker = this.mMap.addMarker(mMarkerOptions);
            mMarker.showInfoWindow();

        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // Nothing todo here.
    }

    @Override
    public void onMarkerDrag(Marker marker) {
        // Nothing todo here.
        mMarker.setPosition(marker.getPosition());
    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        marker.setSnippet(this.getLocationName(marker.getPosition()));
        marker.showInfoWindow();
    }

    private String getLocationName(LatLng latLng) {
        try {
            Geocoder gcd = new Geocoder(this.getContext(), Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                if(!TextUtils.isEmpty(addresses.get(0).getLocality())) {
                    return addresses.get(0).getLocality();
                } else {
                    return this.getContext().getResources().getString(R.string.unknown_location);
                }
            }
        } catch (IOException e) {
            return this.getContext().getResources().getString(R.string.unknown_location);
        }

        return this.getContext().getResources().getString(R.string.unknown_location);
    }
}