package com.android.developer.feedingindia.fragments;


import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.HungerSpot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.ArrayList;

public class HungerSpotsFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMapLongClickListener {

    private ProgressBar progressBar;
    private DatabaseReference mDatabaseReference;
    private Query hungerSpotQuery;
    private ChildEventListener childEventListener;
    private String userName;
    private ArrayList<Location> mHungerSpots;
    private long hungerSpotCount,readHungerSpots = 0;
    private LinearLayout mLinearLayout;

    private GoogleMap mGoogleMap;
    private boolean mMarkerAdded = false;
    private LatLng mChoosenLatLng;
    private Button mSubmitButton;
    private SharedPreferences mSharedPreferences;
    private String role;

    public HungerSpotsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mGoogleMap.setMyLocationEnabled(true);
                }
            }
        }

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        readHungerSpots = hungerSpotCount = 0;
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);
        userName = sharedPreferences.getString("name","");
        mHungerSpots = new ArrayList<>();

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("HungerSpots");
        hungerSpotQuery = FirebaseDatabase.getInstance().getReference().
                        child("HungerSpots").orderByChild("addedBy").equalTo(userName);
        mSharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);

        String role = mSharedPreferences.getString("userType","");
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                    HungerSpot hungerSpot = dataSnapshot.getValue(HungerSpot.class);
                    Location location = new Location(LocationManager.GPS_PROVIDER);
                    location.setLatitude(hungerSpot.getLatitude());
                    location.setLongitude(hungerSpot.getLongitude());
                    mHungerSpots.add(location);
                    readHungerSpots++;

                if(readHungerSpots == hungerSpotCount)
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

    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_hunger_spots, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.hunger_spot_container);

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.INVISIBLE);

        hungerSpotQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                hungerSpotCount = dataSnapshot.getChildrenCount();

                if(hungerSpotCount == 0)
                    enableUserInteraction();
                else
                    hungerSpotQuery.addChildEventListener(childEventListener);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();

        if(childEventListener!=null)
            hungerSpotQuery.removeEventListener(childEventListener);
        mHungerSpots.clear();
        readHungerSpots = 0;
    }

    private void enableUserInteraction()
    {
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        addMarker();
    }

    private void addHungerSpot(double latitude, double longitude)
    {
        if(role == "admin" || role == "superadmin"){
            HungerSpot hungerSpot = new HungerSpot(userName,"validated",latitude,longitude);
            mDatabaseReference.push().setValue(hungerSpot);
            makeToast("Success! HungerSpot added");
        }else {
            HungerSpot hungerSpot = new HungerSpot(userName, "pending", latitude, longitude);
            mDatabaseReference.push().setValue(hungerSpot);
            makeToast("Success! HungerSpot added");
        }
    }

    private void makeToast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.hungerSpotMark);
        mapFragment.getMapAsync(this);
        mSubmitButton = view.findViewById(R.id.hungerSpotSubmitButton);
        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mMarkerAdded && mChoosenLatLng != null){
                    addHungerSpot(mChoosenLatLng.latitude,mChoosenLatLng.longitude);
                }
            }
        });


    }

    @Override
    public void onMapLongClick(LatLng latLng) {

        mMarkerAdded = true;
        mGoogleMap.clear();
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,19f));
        mGoogleMap.addMarker(markerOptions);
        mChoosenLatLng = latLng;


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mGoogleMap = googleMap;
        LatLng location = new LatLng(12.971758,77.593712);
        mGoogleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location,10f));
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            return;
        }else {
            mGoogleMap.setMyLocationEnabled(true);
        }
        mGoogleMap.setOnMapLongClickListener(this);
    }

    public void addMarker() {
        if (mHungerSpots != null && mHungerSpots.size() > 0) {
            for (int i = 0; i < mHungerSpots.size(); i++) {
                if(mHungerSpots.get(i) != null) {
                    MarkerOptions options = new MarkerOptions().position(new LatLng(mHungerSpots.get(i).getLatitude(), mHungerSpots.get(i).getLongitude()));
                    mGoogleMap.addMarker(options);
                }
            }
        }
    }


}
