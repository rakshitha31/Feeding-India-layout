package com.android.developer.feedingindia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.DeliveryDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class HomeFragment extends Fragment {

    private ActionBar toolbar;
    private BottomNavigationView mBottomNavigationView;
    private SharedPreferences mSharedPreferences;
    private Handler mHandler;
    private FrameLayout fragContainer;
    private boolean loadCollectAndDeliverFragment;
    private static final int MENU_ITEM_ID_ONE =1;
    private static final int MENU_ITEM_ID_TWO =2;
    private static final int MENU_ITEM_ID_THREE =3;
    private static final int MENU_ITEM_ID_FOUR =4;
    private static String TAG_DONATE = "Donate";
    private static String TAG_FEED = "Feed";
    private static String TAG_FORM = "Form";
    private static String TAG_VALIDATE = "Validate";
    private static String TAG_COLLECT_AND_DELIVER = "CollectAndDeliver";
    private String CURRENT_TAG = TAG_DONATE;
    private int navItemId;
    private String [] mBottomNavFragmentTitles;
    private ProgressBar mProgressBar;
    public static LatLng donorLocation,hungerSpotLocation;


    public HomeFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        toolbar = ((AppCompatActivity)getActivity()).getSupportActionBar();
        mBottomNavFragmentTitles = getResources().getStringArray(R.array.bottom_nav_fragment_titles);
        mHandler = new Handler();
        mSharedPreferences = this.getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);

        navItemId = 1;
        CURRENT_TAG = TAG_DONATE;

        if(mSharedPreferences.getBoolean("clear",false))
            mSharedPreferences.edit().remove("clear").apply();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mBottomNavigationView = view.findViewById(R.id.bottom_nav_view);
        fragContainer = view.findViewById(R.id.frame_container);
        mProgressBar = view.findViewById(R.id.progressBar);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        setBottomNavBar();

        if(savedInstanceState == null)
            loadInitialFragment();
    }


    @Override
    public void onStart() {
        super.onStart();

        if(mSharedPreferences.getBoolean("clear",false)) {
            getActivity().getSupportFragmentManager().popBackStack();
            mSharedPreferences.edit().remove("clear").apply();
            setBottomNavBar();
            loadInitialFragment();
        }
        loadCollectAndDeliverFragment = false;

    }

    private void loadInitialFragment(){

        String userType = mSharedPreferences.getString("userType","normal");

        switch(userType){

            case "normal" :
                navItemId = 1;
                CURRENT_TAG = TAG_DONATE;
                loadFragment(new DonateFragment());
                break;

            case "hungerhero" :
                navItemId = 2;
                CURRENT_TAG = TAG_FEED;
                loadFragment(new FeedFragment());
                break;

            case "admin" :
                navItemId = 4;
                CURRENT_TAG = TAG_VALIDATE;
                loadFragment(new ValidateFragment());
                break;

            case "superadmin":
                navItemId = 4;
                CURRENT_TAG = TAG_VALIDATE;
                loadFragment(new ValidateFragment());
                break;

            default :
                navItemId = 1;
                CURRENT_TAG = TAG_DONATE;
                loadFragment(new DonateFragment());
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        fragContainer.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
        Query mQuery = FirebaseDatabase.getInstance().getReference().
                child("Deliveries").child(FirebaseAuth.getInstance().getUid()).
                orderByChild("status").equalTo("pending");

        mQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if(dataSnapshot.getChildrenCount()!=0) {

                    loadCollectAndDeliverFragment = true;
                    Log.i("collect_and_deliver",dataSnapshot.toString());
                    DeliveryDetails deliveryDetails;
                    for(DataSnapshot db : dataSnapshot.getChildren()){

                         deliveryDetails = db.getValue(DeliveryDetails.class);

                        Log.i("delivery",deliveryDetails.toString());
                        HashMap<String,String> donorAddress = deliveryDetails.getDonorAddress();
                        HashMap<String,String> hungerSpotAddress = deliveryDetails.getHungerSpotAddress();
                        Log.i("delivery Hunger Spot",donorAddress.get("latitude").toString()+" "+donorAddress.get("longitude"));
                        donorLocation = new LatLng(Double.parseDouble(donorAddress.get("latitude")),Double.parseDouble(donorAddress.get("longitude")));
                        hungerSpotLocation = new LatLng(Double.parseDouble(hungerSpotAddress.get("latitude")),Double.parseDouble(hungerSpotAddress.get("longitude")));

                    }


                        //Log.i("delivery donor Spot",hungerSpotAddress.get("latitude").toString()+" "+hungerSpotAddress.get("longitude"));

                    if(mSharedPreferences.getString("userType","").equals("hungerhero")&&!CURRENT_TAG.equals("Donate"))
                        loadFragment(new CollectAndDeliverFragment());
                }

                enableUserInterAction();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });



        mBottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                switch(menuItem.getItemId()){

                    case MENU_ITEM_ID_ONE:
                        CURRENT_TAG = TAG_DONATE;
                        navItemId = 1;
                        loadFragment(new DonateFragment());
                        break;

                    case MENU_ITEM_ID_TWO:
                        CURRENT_TAG = TAG_FEED;
                        navItemId = 2;

                        if(loadCollectAndDeliverFragment)
                            loadFragment(new CollectAndDeliverFragment());
                        else
                            loadFragment(new FeedFragment());
                        break;

                    case MENU_ITEM_ID_THREE:
                        CURRENT_TAG = TAG_FORM;
                        navItemId = 3;
                        loadFragment(new FormFragment());
                        break;

                    case MENU_ITEM_ID_FOUR:
                        CURRENT_TAG = TAG_VALIDATE;
                        navItemId = 4;
                        loadFragment(new ValidateFragment());
                        break;

                    default:
                        CURRENT_TAG = TAG_DONATE;
                        navItemId = 1;
                        loadFragment(new DonateFragment());

                }

                return true;
            }
        });



    }

    private void setBottomNavBar(){

        String userType =  mSharedPreferences.getString("userType","normal");

        Menu mBottomNavMenu = mBottomNavigationView.getMenu();
        mBottomNavMenu.clear();

        switch(userType){

            case "normal":
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_ONE,Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_TWO,Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_THREE,Menu.NONE,"Form").setIcon(R.drawable.ic_form);
                break;

            case "hungerhero":
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_TWO,Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_ONE,Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                break;

            case "admin":
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_FOUR,Menu.NONE,"Validate").setIcon(R.drawable.ic_validate);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_TWO,Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_ONE,Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                break;

            case "superadmin" :
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_FOUR,Menu.NONE,"Validate").setIcon(R.drawable.ic_validate);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_TWO,Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_ONE,Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                break;

            default:
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_ONE,Menu.NONE,"Donate").setIcon(R.drawable.ic_donate);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_TWO,Menu.NONE,"Feed").setIcon(R.drawable.ic_feed);
                mBottomNavMenu.add(Menu.NONE,MENU_ITEM_ID_THREE,Menu.NONE,"Form").setIcon(R.drawable.ic_form);

        }

    }

    private void loadFragment(final Fragment fragment) {

        final FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

        if(fragmentManager.getBackStackEntryCount()>0)
            fragmentManager.popBackStack();

        setToolBarTitle();
        selNavMenuItem();

        Runnable mPendingRunnable = new Runnable() {

            public void run() {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.replace(R.id.frame_container, fragment ,CURRENT_TAG);
                transaction.addToBackStack(null);
                transaction.commitAllowingStateLoss();
            }
        };

        mHandler.post(mPendingRunnable);
    }

    private void setToolBarTitle(){
        toolbar.setTitle(mBottomNavFragmentTitles[navItemId-1]);
    }

    private void selNavMenuItem(){
        mBottomNavigationView.getMenu().findItem(navItemId).setChecked(true);
    }

    private void enableUserInterAction(){

        fragContainer.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);

    }

}