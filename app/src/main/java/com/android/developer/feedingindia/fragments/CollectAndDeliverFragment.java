package com.android.developer.feedingindia.fragments;


import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.activities.HungerSpotsMapActivity;
import com.android.developer.feedingindia.pojos.DeliveryDetails;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class CollectAndDeliverFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private LatLng mFoodLatLng;
    private LatLng mHungerSpotLatLng;
    private View mView;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOACTION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15;
    private static final String TAG = "CollectDeliverFragment";
    private boolean enableUserInteractionOk = false;
    private boolean onMapReadyOk = false;
    private LatLng hungerSpot, donationSpot;
    private Marker mHungerSpotMarker, mDonationSpotMarker;
    private String address, city, state, pinCode;


    public CollectAndDeliverFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mView = inflater.inflate(R.layout.fragment_collect_and_deliver, container, false);

        return mView;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        onMapReadyOk = true;
        if (mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.setOnMarkerClickListener(this);

            addMarker();
        }
    }

    private void addMarker() {
        hungerSpot = HomeFragment.hungerSpotLocation;
        donationSpot = HomeFragment.donorLocation;
        if (hungerSpot != null) {
            mHungerSpotMarker = mMap.addMarker(new MarkerOptions().position(hungerSpot).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
            mHungerSpotMarker.setTag("HungerSpot");
        }
        if (donationSpot != null) {
            mDonationSpotMarker = mMap.addMarker(new MarkerOptions().position(donationSpot));
            mDonationSpotMarker.setTag("DonationSpot");
        }
    }

    private void moveCamera(LatLng latLng, float zoom) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
    }

    private void getDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());
        try {
            if (mLocationPermissionGranted) {
                final Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Location currentLocation = (Location) task.getResult();
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), 12);
                        } else {
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
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
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.collect_and_deliver_map);
        mapFragment.getMapAsync(this);
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};
        if (ContextCompat.checkSelfPermission(getContext(),
                FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            if (ContextCompat.checkSelfPermission(getContext(),
                    COARSE_LOACTION) == PackageManager.PERMISSION_GRANTED) {
                mLocationPermissionGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(getActivity(), permissions, LOCATION_PERMISSION_REQUEST);

            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        String markerTag = marker.getTag().toString();
        if (markerTag.equals("HungerSpot")) {
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
            mBuilder.setTitle("Hungerspot Chosen");
            Geocoder mgeocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> mListAddress = mgeocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                if (mListAddress != null && mListAddress.size() > 0) {
                    address = "";
                    city = "";
                    state = "";
                    pinCode = "";
                    if (mListAddress.get(0).getFeatureName() != null) {
                        address += mListAddress.get(0).getFeatureName().toString() + " ";
                    }
                    if (mListAddress.get(0).getThoroughfare() != null) {
                        address += mListAddress.get(0).getThoroughfare().toString() + " ";
                    }
                    if (mListAddress.get(0).getSubAdminArea() != null) {
                        address += mListAddress.get(0).getSubAdminArea().toString() + " ";
                    }
                    if (mListAddress.get(0).getLocality() != null) {
                        city += mListAddress.get(0).getLocality().toString();
                    }
                    if (mListAddress.get(0).getAdminArea() != null) {
                        state += mListAddress.get(0).getAdminArea().toString();
                    }
                    if (mListAddress.get(0).getPostalCode() != null) {
                        pinCode += mListAddress.get(0).getPostalCode().toString();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            String mHungerSpotDetails = "HungerSpot Details :\n"+address +"\n"
                    +"City :"+city+"\n"
                    +"State :"+state+"\n"
                    +"Pincode :"+pinCode+"\n";
            mBuilder.setMessage(mHungerSpotDetails);
            mBuilder.setPositiveButton("Navigate in Google Maps", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String latitude = String.valueOf(marker.getPosition().latitude);
                    String longitude = String.valueOf(marker.getPosition().longitude);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
            AlertDialog alertDialog = mBuilder.create();
            alertDialog.show();
        }
        else if(markerTag.equals("DonationSpot")){
            AlertDialog.Builder mBuilder = new AlertDialog.Builder(getContext());
            mBuilder.setTitle("DonationSpot Chosen");
            Geocoder mgeocoder = new Geocoder(getContext(), Locale.getDefault());
            try {
                List<Address> mListAddress = mgeocoder.getFromLocation(marker.getPosition().latitude, marker.getPosition().longitude, 1);
                if (mListAddress != null && mListAddress.size() > 0) {
                    address = "";
                    city = "";
                    state = "";
                    pinCode = "";
                    if (mListAddress.get(0).getFeatureName() != null) {
                        address += mListAddress.get(0).getFeatureName().toString() + " ";
                    }
                    if (mListAddress.get(0).getThoroughfare() != null) {
                        address += mListAddress.get(0).getThoroughfare().toString() + " ";
                    }
                    if (mListAddress.get(0).getSubAdminArea() != null) {
                        address += mListAddress.get(0).getSubAdminArea().toString() + " ";
                    }
                    if (mListAddress.get(0).getLocality() != null) {
                        city += mListAddress.get(0).getLocality().toString();
                    }
                    if (mListAddress.get(0).getAdminArea() != null) {
                        state += mListAddress.get(0).getAdminArea().toString();
                    }
                    if (mListAddress.get(0).getPostalCode() != null) {
                        pinCode += mListAddress.get(0).getPostalCode().toString();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            String mHungerSpotDetails = "DonationSpot Details :\n"+address +"\n"
                    +"City :"+city+"\n"
                    +"State :"+state+"\n"
                    +"Pincode :"+pinCode+"\n";
            mBuilder.setMessage(mHungerSpotDetails);
            mBuilder.setPositiveButton("Navigate in Google Maps", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String latitude = String.valueOf(marker.getPosition().latitude);
                    String longitude = String.valueOf(marker.getPosition().longitude);
                    Uri gmmIntentUri = Uri.parse("google.navigation:q="+latitude+","+longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    startActivity(mapIntent);
                }
            });
            AlertDialog alertDialog = mBuilder.create();
            alertDialog.show();
        }
        return false;
    }

}


