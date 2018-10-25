package com.android.developer.feedingindia.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.HungerSpot;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Map;

public class ValidateFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private ProgressBar progressBar;
    private LinearLayout mLinearLayout;
    private Query hungerSpotQuery;
    private ChildEventListener childEventListener;
    private HashMap<String,Location> mHungerSpots;
    private long hungerSpotCount,readHungerSpots = 0;

    private GoogleMap mGoogleMap;
    String hungerSpotDetails = "";

    public ValidateFragment() {
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

        mHungerSpots = new HashMap<>();
        readHungerSpots = 0;

        hungerSpotQuery = FirebaseDatabase.getInstance().getReference().child("HungerSpots").
                orderByChild("status").equalTo("pending");

        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                HungerSpot hungerSpot = dataSnapshot.getValue(HungerSpot.class);
                Location location = new Location(LocationManager.GPS_PROVIDER);
                location.setLatitude(hungerSpot.getLatitude());
                location.setLongitude(hungerSpot.getLongitude());
                mHungerSpots.put(dataSnapshot.getKey(),location);

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
        View view = inflater.inflate(R.layout.fragment_validate, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.validate_container);

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
    public void addMarker() {

        for(Map.Entry<String,Location> entry : mHungerSpots.entrySet()){
            LatLng latLng = new LatLng(entry.getValue().getLatitude(),entry.getValue().getLongitude());
            Marker mMarker = mGoogleMap.addMarker(new MarkerOptions().position(latLng));
            mMarker.setTag(entry.getKey());
        }
    }


    /*Since pushId's are unique,they are made HashMap keys.When an admin taps on a location to validate
      as a hungerSpot,this function could be called with the key value so that the status could be
      updated from "pending".
     */


    public void onClickValidate(String key) {

        update(key,"validated");

    }

    public void onClickInvalidate(String key){

        update(key,"invalid");
    }

    private void update(String key, final String status){

        final DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("HungerSpots").
                child(key);
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override

            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                HashMap<String,Object> post =(HashMap<String,Object>) dataSnapshot.getValue();
                if(status.equals("validated")) {
                    post.put("status", "validated");
                    mDatabaseReference.updateChildren(post);
                }
                else
                {
                    dataSnapshot.getRef().removeValue();
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.validateMap);
        mapFragment.getMapAsync(this);


    }
    private void removeMarker(Marker marker){
        if(marker != null) {
            marker.remove();
        }
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
        mGoogleMap.setOnMarkerClickListener(this);

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {

        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("HungerSpots").
                child(marker.getTag().toString());
        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String details = "";
                if(dataSnapshot.getChildrenCount() > 0){
                    HungerSpot hungerSpot = dataSnapshot.getValue(HungerSpot.class);
                    details += "added by : "+hungerSpot.getAddedBy()+"\n";
                    details += "role of the person : "+hungerSpot.getUserRole()+"\n";
                    details += "added On : "+hungerSpot.getAddedOn()+"\n";
                }
                hungerSpotDetails = details;
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        alertDialogBuilder.setTitle("Do you want to validate this place as hunger spot ?");
        alertDialogBuilder.setMessage(hungerSpotDetails);
        alertDialogBuilder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(marker != null && marker.getTag().toString() != null){
                    //marker.remove();
                    marker.setVisible(false);
                    onClickValidate(marker.getTag().toString());
                    removeMarker(marker);
                }
            }
        });
        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(marker != null && marker.getTag().toString() != null){
                    //marker.remove();
                    onClickInvalidate(marker.getTag().toString());
                    removeMarker(marker);
                }
            }
        });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();

        return false;
    }

}