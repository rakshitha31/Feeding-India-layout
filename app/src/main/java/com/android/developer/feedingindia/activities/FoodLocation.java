package com.android.developer.feedingindia.activities;

import android.Manifest;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.fragments.DonateFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class FoodLocation extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {
    private static final String TAG = "FoodLocation";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOACTION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 20;
    private boolean mMarkerAdded = false;
    private Button mLocationSubmitButton;
    private String state = "",city = "",donorAddress = "",pinCode = "";
    private LatLng mChoosenLatLng;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_location);
        getLocationPermission();
        mLocationSubmitButton = findViewById(R.id.foodLocationBotton);
    }

    private void moveCamera(LatLng latLng, float zoom) {
        Log.d(TAG, "moving camera to latitude " + latLng.latitude + " longitude" + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getting user location");
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "location was found");
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Log.d(TAG, "loction was not found");
                            Toast.makeText(FoodLocation.this, "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception" + e.getMessage());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted = true;
                    initMap();
                }
        }
    }

    private void initMap() {
        Toast.makeText(this, "Map is ready", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.foodLocationMap);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                    COARSE_LOACTION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(this, permissions, LOCATION_PERMISSION_REQUEST);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMapLongClickListener(this);

    }
    @Override
    public void onMapLongClick(LatLng latLng) {
        mMarkerAdded = true;
        mMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f));
        mMap.addMarker(markerOptions);
        mChoosenLatLng = latLng;
        Geocoder mgeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> mListAddress = mgeocoder.getFromLocation(latLng.latitude,latLng.longitude,1);
            if(mListAddress != null && mListAddress.size() > 0){
                donorAddress = "";
                city = "";
                state = "";
                pinCode = "";
                Log.i("Address",mListAddress.get(0).toString());
                if(mListAddress.get(0).getThoroughfare() != null){
                    Log.i("address",mListAddress.get(0).getThoroughfare().toString());
                    donorAddress += mListAddress.get(0).getThoroughfare().toString() + " ";
                }
                if(mListAddress.get(0).getSubAdminArea() != null){
                    Log.i("city",mListAddress.get(0).getSubAdminArea().toString());
                    donorAddress += mListAddress.get(0).getSubAdminArea().toString() + " ";
                }
                if(mListAddress.get(0).getLocality() != null){
                    Log.i("sub city",mListAddress.get(0).getLocality().toString());
                    city += mListAddress.get(0).getLocality().toString();
                }
                if(mListAddress.get(0).getAdminArea() != null) {
                    Log.i("state", mListAddress.get(0).getAdminArea().toString());
                    state += mListAddress.get(0).getAdminArea().toString();
                }
                if(mListAddress.get(0).getPostalCode() != null){
                    Log.i("postal code",mListAddress.get(0).getPostalCode().toString());
                    pinCode += mListAddress.get(0).getPostalCode().toString();
                }
            }
            Log.i("donor address",donorAddress);
            Log.i("city",city);
            Log.i("state",state);
            Log.i("pincode",pinCode);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void onSubmitLocation(View view){
        if(mMarkerAdded){
            DonateFragment.donorAddress = donorAddress;
            DonateFragment.city = city;
            DonateFragment.pinCode = pinCode;
            DonateFragment.state = state;
            DonateFragment.latitude = mChoosenLatLng.latitude;
            DonateFragment.longitude = mChoosenLatLng.longitude;
            onBackPressed();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
