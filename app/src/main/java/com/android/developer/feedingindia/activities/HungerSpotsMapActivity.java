package com.android.developer.feedingindia.activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.fragments.CollectAndDeliverFragment;
import com.android.developer.feedingindia.fragments.FeedFragment;
import com.android.developer.feedingindia.fragments.HomeFragment;
import com.android.developer.feedingindia.pojos.DeliveryDetails;
import com.android.developer.feedingindia.pojos.HungerSpot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class HungerSpotsMapActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private ProgressBar progressBar;
    private LinearLayout mLinearLayout;
    private Query hungerSpotQuery;
    private ChildEventListener hungerSpotChildEventListener;
    public static HashMap<String,HungerSpot> hungerSpots;
    private  HashMap<String,String> hungerSpotAddress;
    private long hungerSpotCount = 0,readHungerSpotCount = 0;
    private boolean doneReadingHungerSpots = false;
    public static  String chosenHungerSpotPushId;

    private String chosenDonationId;
    private boolean enableUserInteractionOk = false;
    private boolean onMapReadyOk = false;
    private boolean onMarkerAddOk = true;
    private double latitude,longitude;

    private  String address,city,state,pinCode;

    private Button mConfirmButton;
    private boolean onMarkerClicked;

    public static LatLng chosenHungerSpotLatlng;
    public LatLng chosenDoantionLatLng;


    private GoogleMap mMap;

    private SharedPreferences mSharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunger_spots_map);
        progressBar = findViewById(R.id.progressBar);
        mLinearLayout = findViewById(R.id.hunger_spot_map_container);
        chosenDoantionLatLng = FeedFragment.chosenFoodLatLng;

        mSharedPreferences = getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);

//
        Bundle extras = getIntent().getExtras();
        if(extras != null)
            chosenDonationId = extras.getString("DonationId"," ");
        Log.i("donation id",chosenDonationId);

        onMarkerAddOk = true;
        onMapReadyOk = false;
        enableUserInteractionOk = false;
        onMarkerClicked = false;
//
        hungerSpotCount = readHungerSpotCount = 0;
        doneReadingHungerSpots = false;
        hungerSpotQuery = FirebaseDatabase.getInstance().getReference().child("HungerSpots").orderByChild("status").equalTo("validated");
        hungerSpots = new HashMap<>();

        hungerSpotChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                HungerSpot hungerSpot = dataSnapshot.getValue(HungerSpot.class);
                hungerSpots.put(dataSnapshot.getKey(),hungerSpot);
                readHungerSpotCount++;
                if(readHungerSpotCount==hungerSpotCount)
                    doneReadingHungerSpots = true;

                if(doneReadingHungerSpots)
                    enableUserInteraction();

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };
//
        SupportMapFragment mMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.hungerSpotsMap);
        mMapFragment.getMapAsync(this);
        mConfirmButton = findViewById(R.id.confirmButton);
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hungerSpotAddress = new HashMap<>();
                if(onMarkerClicked){
                    if(city != null)
                        hungerSpotAddress.put("city",city);
                    if(state != null)
                        hungerSpotAddress.put("state",state);
                    if(pinCode != null)
                        hungerSpotAddress.put("pinCode",pinCode);
                    if(address != null)
                        hungerSpotAddress.put("address",address);
                    hungerSpotAddress.put("latitude",latitude+"");
                    hungerSpotAddress.put("longitude",longitude+"");
                    Log.i("button clicked","data submittded");
                    if(chosenDonationId != null && chosenHungerSpotPushId != null)
                        onClickAgreeToDeliver(chosenDonationId,chosenHungerSpotPushId);
                }
            }
        });


//

    }

    @Override
    protected void onStart() {
        super.onStart();
        doneReadingHungerSpots = false;
        readHungerSpotCount = 0;
//
        if(hungerSpots != null) {
            hungerSpots.clear();
        }if(hungerSpotAddress != null) {
            hungerSpotAddress.clear();
        }
        chosenHungerSpotPushId ="";
//
    }

    @Override
    protected void onResume() {
        super.onResume();

        mLinearLayout.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        hungerSpotQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                hungerSpotCount = dataSnapshot.getChildrenCount();

                if(hungerSpotCount!=0)
                    hungerSpotQuery.addChildEventListener(hungerSpotChildEventListener);
                else
                    doneReadingHungerSpots = true;

                if(doneReadingHungerSpots)
                    enableUserInteraction();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(hungerSpotChildEventListener!=null)
            hungerSpotQuery.removeEventListener(hungerSpotChildEventListener);

    }

    private void enableUserInteraction()
    {
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
//
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        enableUserInteractionOk = true;
        addMarker();
//

    }

    private void onClickAgreeToDeliver(String chosenDonationSpot, String chosenHungerSpotPushId){

        Handler mHandler = new Handler();

        DatabaseReference mDeliveryDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Deliveries").
               child(FirebaseAuth.getInstance().getUid());
       DeliveryDetails deliveryDetails = new DeliveryDetails(chosenDonationSpot,chosenHungerSpotPushId,FeedFragment.donorUid,FeedFragment.nameOfDonor,FeedFragment.phoneNumberOfDonor,"",FeedFragment.chosenDonationAddress,hungerSpotAddress,"pending",null,FeedFragment.donationImgUrl);
       mDeliveryDatabaseReference.push().setValue(deliveryDetails);
        final DatabaseReference mDonationDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations").
                child(FeedFragment.donorUid).child(FeedFragment.chosenDonationPushId);




        mDonationDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String,Object> myHashMap = (HashMap<String,Object>)dataSnapshot.getValue();
                myHashMap.put("status","picked");
                myHashMap.put("delivererName",mSharedPreferences.getString("name",""));
                myHashMap.put("delivererContactNumber",mSharedPreferences.getString("mobileNumber",""));
                mDonationDatabaseReference.updateChildren(myHashMap);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
            FeedFragment.chosenDonation = true;
            HomeFragment.hungerSpotLocation = chosenHungerSpotLatlng;
            HomeFragment.donorLocation = FeedFragment.chosenFoodLatLng;
        Intent intent = new Intent();
        setResult(2,intent);
        finish();

    }


//============================================================
// Whenever a hungerSpot marker is clicked, clear hungerSpotAddressHashMap.Get the LatLng of the marker
// Then do reverse geo-coding and use the code given below to set the hungerSpotAddress
//============================================================
//    hungerSpotAddress.put("city","");
//    hungerSpotAddress.put("state","");
//    hungerSpotAddress.put("pinCode","");
//    hungerSpotAddress.put("address","");
//    hungerSpotAddress.put("latitude","");
//    hungerSpotAddress.put("longitude","");

//Use ProgressBar wherever necessary

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                }
            }
        }

    }

    // adding marker
    private void addMarker() {
        if (onMarkerAddOk) {
            if (enableUserInteractionOk && onMapReadyOk)
                for (Map.Entry<String, HungerSpot> entry : hungerSpots.entrySet()) {
                    LatLng latLng = new LatLng(entry.getValue().getLatitude(), entry.getValue().getLongitude());
                    Marker mMarker = mMap.addMarker(new MarkerOptions().position(latLng).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                    mMarker.setTag(entry.getKey());
                    onMarkerAddOk = false;
                }
        }
    }

    // on map ready call back
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        LatLng location = new LatLng(12.971758,77.593712);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,10f));

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else {
            mMap.setMyLocationEnabled(true);
        }
        mMap.setOnMarkerClickListener(this);
        onMapReadyOk = true;
        addMarker();
    }

    // on marker clicked
    @Override
    public boolean onMarkerClick(Marker marker) {
        chosenHungerSpotLatlng = marker.getPosition();
        onMarkerClicked = true;
        LatLng latLng = marker.getPosition();
        chosenHungerSpotLatlng = latLng;
        chosenHungerSpotPushId = marker.getTag().toString();
        Log.i("latitude", latLng.latitude + "");
        Log.i("longitude", latLng.longitude + "");
        Geocoder mgeocoder = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
            List<Address> mListAddress = mgeocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (mListAddress != null && mListAddress.size() > 0) {
                address = "";
                city = "";
                state = "";
                pinCode = "";
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                Log.i("Address", mListAddress.get(0).toString());
                if (mListAddress.get(0).getFeatureName() != null) {
                    Log.i("feature", mListAddress.get(0).getFeatureName().toString());
                    address += mListAddress.get(0).getFeatureName().toString() + " ";
                }
                if (mListAddress.get(0).getThoroughfare() != null) {
                    Log.i("address", mListAddress.get(0).getThoroughfare().toString());
                    address += mListAddress.get(0).getThoroughfare().toString() + " ";
                }
                if (mListAddress.get(0).getSubAdminArea() != null) {
                    Log.i("city", mListAddress.get(0).getSubAdminArea().toString());
                    address += mListAddress.get(0).getSubAdminArea().toString() + " ";
                }
                if (mListAddress.get(0).getLocality() != null) {
                    Log.i("sub city", mListAddress.get(0).getLocality().toString());
                    city += mListAddress.get(0).getLocality().toString();
                }
                if (mListAddress.get(0).getAdminArea() != null) {
                    Log.i("state", mListAddress.get(0).getAdminArea().toString());
                    state += mListAddress.get(0).getAdminArea().toString();
                }
                if (mListAddress.get(0).getPostalCode() != null) {
                    Log.i("postal code", mListAddress.get(0).getPostalCode().toString());
                    pinCode += mListAddress.get(0).getPostalCode().toString();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}

