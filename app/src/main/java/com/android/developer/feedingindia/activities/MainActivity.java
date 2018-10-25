package com.android.developer.feedingindia.activities;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.android.developer.feedingindia.fragments.*;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{

    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mNotificationDbReference;
    private ChildEventListener mChildEventListener;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private Handler mHandler;
    private String[] mFragmentTitles;
    private static final String TAG_HOME = "Home";
    private static final String TAG_PROFILE = "Profile";
    private static final String TAG_NOTIFICATIONS = "Notifications";
    private static final String TAG_MY_DONATIONS = "My Donations";
    private static final String TAG_MY_DELIVERIES = "My Deliveries";
    private static final String TAG_SPOTS = "Hunger spots I spotted";
    private static final String TAG_MAKE_ADMIN = "Add/Remove Admin";
    private static final String TAG_POST_NOTIFICATION = "Add Event";
    private static final String TAG_CONTACT_US = "Contact Us";
    private static final String TAG_ABOUT_US = "About Us";
    private int navItemIndex;
    private String CURRENT_TAG;
    private boolean addedAdminMenuItems;
    private SharedPreferences mSharedPreferences;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseReference;
    private AlertDialog.Builder exitBuilder,adminRequestBuilder;
    private AlertDialog adminRequestDialog;
    private static final int MAKE_ADMIN_ID = 1, POST_NOTIFICATION_ID=2;
    private HashMap<String,Object> mMap;
    private long notificationCount = 0;


    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this,mDrawerLayout,toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        mDrawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = findViewById(R.id.nav_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        final View mNavHeader = mNavigationView.getHeaderView(0);
        TextView mNavHeaderUserName = mNavHeader.findViewById(R.id.nav_header_user_name);
        TextView mNavHeaderUserEmail = mNavHeader.findViewById(R.id.nav_header_user_email);




        mHandler = new Handler();
        mMap = new HashMap<>();

        CURRENT_TAG = TAG_HOME;
        navItemIndex = 0;
        addedAdminMenuItems = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            adminRequestBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            exitBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        }
        else {
            adminRequestBuilder = new AlertDialog.Builder(this);
            exitBuilder = new AlertDialog.Builder(this);;
        }

        exitBuilder.setTitle("Exit")
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        moveTaskToBack(true);
                        android.os.Process.killProcess(android.os.Process.myPid());
                        System.exit(1);
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });

        mAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());

        adminRequestDialog = adminRequestBuilder.setTitle("Request to be an admin")
                .setMessage("You are requested to become an admin\nDo you want to be an admin?")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mMap.put("previousRole",mSharedPreferences.getString("userType","normal"));
                        mMap.put("requestedToBeAdmin", false);
                        mMap.put("userType", "admin");
                        mSharedPreferences.edit().putBoolean("clear", true).apply();
                        mSharedPreferences.edit().putString("userType", "admin").apply();
                        mDatabaseReference.setValue(mMap);
                        makeToast("Congo! You are an admin now");

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mMap.put("requestedToBeAdmin", false);
                        mDatabaseReference.setValue(mMap);

                    }
                }).setCancelable(false).create();

        mSharedPreferences = this.getSharedPreferences(getPackageName(),MODE_PRIVATE);

        mNavHeaderUserEmail.setText(mAuth.getCurrentUser().getEmail());
        mNavHeaderUserName.setText(mSharedPreferences.getString("name", ""));

        mFragmentTitles = getResources().getStringArray(R.array.fragment_titles);

        if (savedInstanceState==null)
            loadFragment();

    }

    @Override
    protected void onResume() {

        super.onResume();


        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mNotificationDbReference = mFirebaseDatabase.getReference().child("Notifications");

        mNotificationDbReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.getChildrenCount() == 0){

                }
                else{
                    long childCount = dataSnapshot.getChildrenCount();
                    notificationCount = Long.valueOf(mSharedPreferences.getString("childCount", "0"));
                    if(notificationCount < childCount){
                        TextView textView = (TextView) mNavigationView.getMenu().findItem(R.id.nav_menu_item_notifications).getActionView();
                        textView.setText(""+(childCount - notificationCount));
                        mSharedPreferences.edit().putString("childCount",""+dataSnapshot.getChildrenCount()).apply();


                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                makeToast("dataBaseError");
            }
        });






        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                try{
                    mMap = (HashMap) dataSnapshot.getValue();

                    mSharedPreferences.edit().putString("userType",mMap.get("userType").toString()).apply();

                    if (mMap.get("requestedToBeAdmin").equals(true))
                        adminRequestDialog.show();

                }catch (NullPointerException e){
                    Log.d("Null Pointer Exception","mMap NPE");
                }





            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

                makeToast(databaseError.getMessage());

            }
        });

        String userType = mSharedPreferences.getString("userType", "");
        if((userType.equals("admin")||userType.equals("superadmin"))&&!addedAdminMenuItems){
            addAdminMenuItems(userType);
            addedAdminMenuItems = true;
        }


    }

    private void addAdminMenuItems(String userType){

        Menu menu = mNavigationView.getMenu();
        menu.removeItem(R.id.nav_menu_contact_us);
        if(userType.equals("superadmin"))
            menu.add(0, MAKE_ADMIN_ID, 700, TAG_MAKE_ADMIN);
        menu.add(0,POST_NOTIFICATION_ID,800,TAG_POST_NOTIFICATION);

    }

    @Override
    protected void onPause() {

        super.onPause();

        if(adminRequestDialog.isShowing())
            adminRequestDialog.dismiss();

        if(mMap!=null)
            mMap.clear();

    }

    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
            mDrawerLayout.closeDrawer(GravityCompat.START);
        else
            exitBuilder.show();
    }


    private void loadFragment(){

        setToolBarTitle();

        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
            return;
        }

        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                Fragment fragment = getFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        mHandler.post(mPendingRunnable);
        mDrawerLayout.closeDrawers();

    }

    private void setToolBarTitle() {
        getSupportActionBar().setTitle(mFragmentTitles[navItemIndex]);
    }

    private Fragment getFragment() {

        switch (navItemIndex) {
            case 0:
                return (new HomeFragment());
            case 1:
                return (new ProfileFragment());
            case 2:
                return (new NotificationsFragment());
            case 3:
                return (new MyDonationsFragment());
            case 4:
                return (new MyDeliveriesFragment());
            case 5:
                return (new HungerSpotsFragment());
            case 6:
                return (new AddOrRemoveAdmin());
            case 7:
                return (new AddEventFragment());
            case 8:
                return (new ContactUsFragment());
            case 9:
                return (new AboutUsFragment());
            default:
                return (new HomeFragment());
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu,menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {

            case R.id.main_menu_sign_out:
                mAuth.signOut();
                makeToast("Signed Out");
                finish();
                break;

            case R.id.main_menu_exit:
                exitBuilder.show();

        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

        switch (menuItem.getItemId()){

            case R.id.nav_menu_item_home:
                CURRENT_TAG = TAG_HOME;
                navItemIndex = 0;
                break;

            case R.id.nav_menu_item_profile:
                CURRENT_TAG = TAG_PROFILE;
                navItemIndex = 1;
                break;

            case R.id.nav_menu_item_notifications:
                CURRENT_TAG = TAG_NOTIFICATIONS;
                navItemIndex = 2;
                break;

            case R.id.nav_menu_item_donations:
                CURRENT_TAG = TAG_MY_DONATIONS;
                navItemIndex = 3;
                break;

            case R.id.nav_menu_item_deliveries:
                CURRENT_TAG = TAG_MY_DELIVERIES;
                navItemIndex = 4;
                break;

            case R.id.nav_menu_item_hunger_spots:
                CURRENT_TAG = TAG_SPOTS;
                navItemIndex = 5;
                break;

            case MAKE_ADMIN_ID :
                CURRENT_TAG = TAG_MAKE_ADMIN;
                navItemIndex = 6;
                break;

            case POST_NOTIFICATION_ID :
                CURRENT_TAG = TAG_POST_NOTIFICATION;
                navItemIndex = 7;
                break;

            case R.id.nav_menu_contact_us :
                CURRENT_TAG = TAG_CONTACT_US;
                navItemIndex = 8;
                break;

            case R.id.nav_menu_item_about_us:
                CURRENT_TAG = TAG_ABOUT_US;
                navItemIndex = 9;
                break;

            default:
                CURRENT_TAG = TAG_HOME;
                navItemIndex=0;

        }

        loadFragment();
        return true;

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void makeToast(String message){
        Toast.makeText(MainActivity.this,message,Toast.LENGTH_SHORT).show();
    }

}