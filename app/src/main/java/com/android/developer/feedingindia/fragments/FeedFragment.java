package com.android.developer.feedingindia.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.activities.HungerSpotsMapActivity;
import com.android.developer.feedingindia.pojos.DonationDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class FeedFragment extends Fragment implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private DatabaseReference donorSpotDatabaseReference;
    private ChildEventListener donorSpotChildEventListener;
    private ProgressBar progressBar;
    private LinearLayout mLinearLayout;
    public static HashMap<String,DonationDetails> donations;
    public static HashMap<String,String> donationPushIdToUserId;
    public static HashMap<String,String> chosenDonationAddress;
    private long donationCount = 0,readDonationCount = 0;
    private boolean doneReadingDonations = false;
    public static String chosenDonationPushId,nameOfDonor,phoneNumberOfDonor,donationImgUrl,donorUid;
    private GoogleMap mMap;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COARSE_LOACTION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private Boolean mLocationPermissionGranted = false;
    private static final int LOCATION_PERMISSION_REQUEST = 1234;
    private FusedLocationProviderClient mFusedLocationProviderClient;
    private static final float DEFAULT_ZOOM = 15;
    private boolean enableUserInteractionOk = false;
    private boolean onMapReadyOk = false;
    private boolean onMarkerAddOk = true;
    public static LatLng chosenFoodLatLng;
    public HashMap<String,String> imageUrls;
    public String mSelectedImageUrl;
    private Handler mHandler;
    private String name,phoneNo;


    public FeedFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        donationCount = readDonationCount = 0;
        doneReadingDonations = false;
        donorSpotDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Donations");
        donations = new HashMap<>();
        donationPushIdToUserId = new HashMap<>();
        chosenDonationAddress = new HashMap<>();
        mHandler = new Handler();


        donorSpotChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                ObjectMapper myMapper = new ObjectMapper();
                HashMap<String,HashMap<String,Object>> myList = (HashMap<String,HashMap<String,Object>>)dataSnapshot.getValue();
                Set mySet = myList.entrySet();
                Iterator iterator = mySet.iterator();
                while(iterator.hasNext()){
                    Map.Entry myMapEntry =(Map.Entry) iterator.next();
                    DonationDetails donationDetails = myMapper.convertValue(myMapEntry.getValue(), DonationDetails.class);
                    if((!donationDetails.isCanDonate()&&donationDetails.getStatus().equals("pending"))&&(!donationDetails.getStatus().equals("cancelled"))) {
                        donations.put(myMapEntry.getKey().toString(), donationDetails);
                        donationPushIdToUserId.put(myMapEntry.getKey().toString(),dataSnapshot.getKey());
                    }
                }

                readDonationCount++;
                if (readDonationCount == donationCount)
                    doneReadingDonations = true;
                if (doneReadingDonations)
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
    public void onStart() {
        super.onStart();
        doneReadingDonations = false;
        readDonationCount = 0;
        if(donations!=null)
            donations.clear();
        if(donationPushIdToUserId!=null)
            donationPushIdToUserId.clear();
        if(chosenDonationAddress!=null)
            chosenDonationAddress.clear();
        chosenDonationPushId = "";
        if(mMap!=null)
            mMap.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_feed, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.feed_container);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.INVISIBLE);
        if(mMap!=null)
            mMap.clear();
        donorSpotDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                donationCount = dataSnapshot.getChildrenCount();

                if(donationCount!=0)
                    donorSpotDatabaseReference.addChildEventListener(donorSpotChildEventListener);
                else
                    doneReadingDonations = true;

                if(doneReadingDonations)
                    enableUserInteraction();


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onPause() {
        super.onPause();
        if(donorSpotChildEventListener!=null)
            donorSpotDatabaseReference.removeEventListener(donorSpotChildEventListener);
    }

    private void enableUserInteraction()
    {
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
        enableUserInteractionOk = true;
        if(HomeFragment.loadCollectAndDeliverFragment){
            Handler mHandler = new Handler();

            final CollectAndDeliverFragment fragment = new CollectAndDeliverFragment();
            final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            if(fragmentManager.getBackStackEntryCount()>0)
                fragmentManager.popBackStack();

            Runnable mPendingRunnable = new Runnable() {

                public void run() {
                    FragmentTransaction transaction = fragmentManager.beginTransaction();
                    transaction.replace(R.id.frame_container, fragment ,"feed");
                    transaction.addToBackStack(null);
                    transaction.commitAllowingStateLoss();
                }
            };

            mHandler.post(mPendingRunnable);


        }// change made for adding marker
        else
            addMarker();
    }

    private void onClickChooseHungerSpot(){

        Query donorAddressQuery = FirebaseDatabase.getInstance().getReference().child("Donations").
                child(donationPushIdToUserId.get(chosenDonationPushId)).child(chosenDonationPushId).
                child("donorAddress");

        donorAddressQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                chosenDonationAddress = (HashMap<String,String>)dataSnapshot.getValue();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //Intent to HungerSpotsMapActivity
        Intent intent = new Intent(getContext(),HungerSpotsMapActivity.class);
        intent.putExtra("DonationId",chosenDonationPushId);
        startActivityForResult(intent,2);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 2) {
            if(resultCode == 2) {
                if (HomeFragment.loadCollectAndDeliverFragment) {
                    final CollectAndDeliverFragment fragment = new CollectAndDeliverFragment();

                    final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    if (fragmentManager.getBackStackEntryCount() > 0)
                        fragmentManager.popBackStack();

                    Runnable mPendingRunnable = new Runnable() {

                        public void run() {
                            FragmentTransaction transaction = fragmentManager.beginTransaction();
                            transaction.replace(R.id.frame_container, fragment, "Collect and Deliver Fragment");
                            transaction.addToBackStack(null);
                            transaction.commitAllowingStateLoss();
                        }
                    };

                    mHandler.post(mPendingRunnable);
                }
            }

        }

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
        }
        mMap.setOnMarkerClickListener(this);
        addMarker();
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
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM);
                        } else {
                            Toast.makeText(getContext(), "unable to get current location", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Toast.makeText(getActivity(), "Location Error", Toast.LENGTH_SHORT).show();
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
        Toast.makeText(getContext(), "Map is ready", Toast.LENGTH_SHORT).show();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.feedMap);
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
    public boolean onMarkerClick(Marker marker) {
        chosenFoodLatLng = marker.getPosition();
        imageUrls = new HashMap<>();
        final String id = marker.getTag().toString();
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getContext());
        String addressOfMarker = " ",Donationinfo = " ";
        Set mMarkerSet = donations.entrySet();
        Iterator iterator = mMarkerSet.iterator();
        while (iterator.hasNext()) {
            Map.Entry mMapMarkerEntry = (Map.Entry) iterator.next();
            DonationDetails mdonationDetails = (DonationDetails) mMapMarkerEntry.getValue();
            HashMap<String, Object> address = mdonationDetails.getDonorAddress();
            if(id.equals(mMapMarkerEntry.getKey())){
                addressOfMarker = address.get("address")+"\n"+address.get("city")+"\n"+address.get("state")
                        +"\n"+address.get("pinCode");
                Donationinfo =
                        "Donor Name :"+mdonationDetails.getDonorName()+"\n"
                                +"FoodDescription : "+mdonationDetails.getFoodDescription()+"\n"
                                +"Food was prepared on : "+mdonationDetails.getFoodPreparedOn()+"\n"
                                +"Donor has container : " +mdonationDetails.isHasContainer()+"\n"
                                +"Food Type :"+mdonationDetails.getFoodType()+"\n"
                                +"Shelf Life Of Food : "+mdonationDetails.getShelfLife()+"\n"
                                +"No Of People It Can Feed : "+mdonationDetails.getNoPeopleCanBeServed()+"\n"
                                +"Contact number 1 : "+mdonationDetails.getDonorContactNumber() +"\n"
                                +"Contact number 2 : "+mdonationDetails.getAdditionalContactNumber() +"\n";
                mSelectedImageUrl = mdonationDetails.getDonationImageUrl();
                name = mdonationDetails.getDonorName();
                phoneNo = mdonationDetails.getDonorContactNumber();
                donorUid = mdonationDetails.getDonationUserId();
                break;
            }
        }
        progressBar.setVisibility(View.VISIBLE);
        ImageView imageView = new ImageView(getContext());
        Picasso.get().load(mSelectedImageUrl).resize(150,150).into(imageView);
        progressBar.setVisibility(View.GONE);
        alertDialogBuilder.setMessage("Address of Donor : \n"+addressOfMarker+"\n"+"Donation Info: \n"+Donationinfo);
        alertDialogBuilder.setPositiveButton("Choose a HungerSpot", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                chosenDonationPushId = id;
                nameOfDonor = name;
                phoneNumberOfDonor = phoneNo;
                donationImgUrl = mSelectedImageUrl;
                onClickChooseHungerSpot();
            }
        }).setView(imageView);
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();



        return false;

    }
    public void addMarker(){
        if(onMarkerAddOk) {
            if (enableUserInteractionOk == true && onMapReadyOk == true) {
                Set mMarkerSet = donations.entrySet();
                Iterator iterator = mMarkerSet.iterator();
                while (iterator.hasNext()){
                    Map.Entry mMapMarkerEntry = (Map.Entry) iterator.next();
                    DonationDetails mDonationDetails = (DonationDetails) mMapMarkerEntry.getValue();
                    HashMap<String,Object> address = mDonationDetails.getDonorAddress();
                    Marker mMarker =  mMap.addMarker(new MarkerOptions().position(new LatLng(Double.parseDouble(address.get("latitude").toString()),Double.parseDouble(address.get("longitude").toString()))));
                    mMarker.setTag(mMapMarkerEntry.getKey());
                }
                onMarkerAddOk = false;
            }
        }
    }

}