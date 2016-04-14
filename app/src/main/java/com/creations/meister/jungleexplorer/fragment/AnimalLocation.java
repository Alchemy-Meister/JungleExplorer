package com.creations.meister.jungleexplorer.fragment;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.creations.meister.jungleexplorer.R;
import com.creations.meister.jungleexplorer.domain.Animal;
import com.creations.meister.jungleexplorer.permission_utils.RuntimePermissionsHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


/**
 * Created by meister on 4/7/16.
 */
public class AnimalLocation extends Fragment implements GoogleMap.OnMapClickListener,
        GoogleMap.OnMarkerDragListener, OnMapReadyCallback {
    private static final int LOCATION_ASK_REQUEST = 123;
    private static final String MAP_KEY = "map";
    private static final String LATLNG_KEY = "latlng";

    private static final String[] requiredPermissions = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
    };

    private CameraPosition cp = null;
    private Marker mMarker = null;
    private MapView mapView = null;
    private GoogleMap mMap = null;
    private LatLng mLatLng = null;
    private Bundle mapState = null;

    private boolean editable = false;

    private final String ANIMAL_KEY = "ANIMAL";
    private Animal animal;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.map_container, container, false);

        mapView = (MapView) v.findViewById(R.id.map);
        mapView.getMapAsync(this);

        if(savedInstanceState != null) {
            mLatLng = savedInstanceState.getParcelable(AnimalLocation.LATLNG_KEY);
            mapState = savedInstanceState.getBundle(AnimalLocation.MAP_KEY);
        }
        mapView.onCreate(mapState);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(savedInstanceState == null) {
            Bundle bundle = this.getActivity().getIntent().getExtras();
            if(bundle != null) {
                animal = (Animal) bundle.get(ANIMAL_KEY);

            } else {
                editable = true;
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        map.getUiSettings().setMapToolbarEnabled(false);
        map.setOnMarkerDragListener(AnimalLocation.this);
        map.setOnMapClickListener(AnimalLocation.this);
        initializeMyLocation();

        this.setEditable(editable);

        if(mLatLng != null) {
            initializeMarker(mLatLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(mMarker.getPosition()));
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (mMarker == null && editable) {
            initializeMarker(latLng);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        }
    }

    @Override
    public void onMarkerDragStart(Marker marker) {
        // Nothing todo here.
    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {
        marker.setSnippet(this.getLocationName(marker.getPosition()));
        marker.showInfoWindow();
        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
    }

    @Override
    public void onPause() {
        super.onPause();
        if(mapView != null) {
            mapView.onPause();
        }
        if(mMarker != null) {
            mLatLng = mMarker.getPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        if(mMap != null) {
            if(RuntimePermissionsHelper.hasPermissions(this.getContext(), requiredPermissions)) {
                if(editable) {
                    myLocationMap();
                }
            }
            if(mMarker != null) {
                mMarker.setPosition(mLatLng);
                mMarker.setVisible(true);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mapView != null) {
            mapView.onDestroy();
        }
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if(mapView != null) {
            mapView.onLowMemory();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_ASK_REQUEST:
                if (ActivityCompat.checkSelfPermission(
                        this.getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                        || ActivityCompat.checkSelfPermission(
                        this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                } else {
                    RuntimePermissionsHelper.showMessageOKCancel(getResources().getString(
                            R.string.location_permission_message,
                            getResources().getString(R.string.app_name)),
                            AnimalLocation.this.getContext());
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void initializeMyLocation() {
        if(editable) {
            int hasFineLocationPermission = ContextCompat.checkSelfPermission(
                    AnimalLocation.this.getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);

            if (hasFineLocationPermission != PackageManager.PERMISSION_GRANTED) {
                this.requestPermissions(requiredPermissions, LOCATION_ASK_REQUEST);
                return;
            }
            myLocationMap();
        }
    }

    private void myLocationMap() {
        //noinspection MissingPermission
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {

                Location myLocation = getMyLocation();

                if (myLocation != null) {
                    LatLng mLatLngClick = new LatLng(myLocation.getLatitude(),
                            myLocation.getLongitude());

                    if (mMarker == null) {
                        initializeMarker(mLatLngClick);
                    } else {
                        mMarker.setPosition(mLatLngClick);
                        mMarker.setSnippet(getLocationName(mLatLngClick));
                        mMarker.showInfoWindow();
                    }
                } else {
                    showLocationSettingsMsg(
                            getContext().getResources().getString(R.string.location_unavailable));
                }

                return true;
            }
        });
    }

    private void initializeMarker(LatLng latLng) {
        MarkerOptions mMarkerOptions = new MarkerOptions()
                .position(latLng)
                .title(getResources().getString(R.string.location))
                .snippet(this.getLocationName(latLng))
                .draggable(editable);

        mMarker = this.mMap.addMarker(mMarkerOptions);
        mMarker.showInfoWindow();
    }

    private Location getMyLocation() {
        LocationManager lm = (LocationManager)
                this.getActivity().getSystemService(Context.LOCATION_SERVICE);
        //noinspection MissingPermission
        Location myLocation = lm.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        if (myLocation == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_COARSE);
            String provider = lm.getBestProvider(criteria, true);
            //noinspection MissingPermission
            myLocation = lm.getLastKnownLocation(provider);
        }

        return myLocation;
    }

    private String getLocationName(LatLng latLng) {
        try {
            Geocoder gcd = new Geocoder(this.getContext(), Locale.getDefault());
            List<Address> addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses.size() > 0) {
                if (!TextUtils.isEmpty(addresses.get(0).getLocality())) {
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

    private void showLocationSettingsMsg(String message) {
        new AlertDialog.Builder(this.getContext())
                .setMessage(message)
                .setPositiveButton(R.string.ok, null)
                .setNegativeButton(R.string.settings, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        goLocationSettings();
                    }
                })
                .create()
                .show();
    }

    private void goLocationSettings() {
        this.getContext().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
    }

    public Animal setAnimalLocation(Animal animal) {
        if(mMarker != null) {
            animal.setLatitude(mMarker.getPosition().latitude);
            animal.setLongitude(mMarker.getPosition().longitude);
        }
        return animal;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle mapBundle = new Bundle();

        if(mapView != null) {
            mapView.onSaveInstanceState(mapBundle);
            outState.putBundle(AnimalLocation.MAP_KEY, mapBundle);
            if(mMarker != null) {
                outState.putParcelable(AnimalLocation.LATLNG_KEY, mMarker.getPosition());
            }
        }

        super.onSaveInstanceState(outState);
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
        if(this.isVisible()) {
            if(editable) {
                if(mMap != null) {
                    int hasFineLocationPermission = ContextCompat.checkSelfPermission(
                            AnimalLocation.this.getContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION);

                    if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED) {
                        myLocationMap();
                    }
                }
                if(mMarker != null) {
                    mMarker.setDraggable(true);
                }
            } else {
                if(mMap != null) {
                    mMap.setMyLocationEnabled(false);
                }
                if(mMarker != null) {
                    mMarker.setDraggable(false);
                }
            }
        }
    }
}