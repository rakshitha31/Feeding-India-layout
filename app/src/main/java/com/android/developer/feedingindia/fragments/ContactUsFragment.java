package com.android.developer.feedingindia.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.developer.feedingindia.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class ContactUsFragment extends Fragment {

    private ProgressBar progressBar;
    private LinearLayout mLinearLayout;
    private TextView nameTextView,numberTextView,noCityHeadTextView;
    private long userCount;
    private DatabaseReference databaseReference;
    private ChildEventListener childEventListener;
    private long readUserCount;

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        userCount = readUserCount = 0;

       childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                HashMap<String,Object> mMap = (HashMap)dataSnapshot.getValue();
                if(mMap.get("userType").equals("admin")&&mMap.get("city").equals(getActivity().getSharedPreferences(getActivity().getPackageName(), Context.MODE_PRIVATE).getString("city",""))) {
                    nameTextView.setText(mMap.get("name").toString());
                    numberTextView.setText(mMap.get("mobileNumber").toString());
                    progressBar.setVisibility(View.INVISIBLE);
                    mLinearLayout.setVisibility(View.VISIBLE);
                    databaseReference.removeEventListener(childEventListener);
                }
                else {
                    readUserCount++;
                    if (readUserCount == userCount) {
                        noCityHeadTextView.setVisibility(View.VISIBLE);
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                }

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
        View view = inflater.inflate(R.layout.fragment_contact_us, container, false);
        mLinearLayout = view.findViewById(R.id.linear_container);
        nameTextView = view.findViewById(R.id.userName);
        numberTextView = view.findViewById(R.id.userMobileNumber);
        noCityHeadTextView = view.findViewById(R.id.no_city_head);
        progressBar = view.findViewById(R.id.progressBar);
        return view;
    }

    @Override
    public void onResume() {

        mLinearLayout.setVisibility(View.INVISIBLE);
        noCityHeadTextView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);

        databaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                userCount = readUserCount = 0;

                userCount = dataSnapshot.getChildrenCount();

                if(userCount == 0){
                    noCityHeadTextView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.INVISIBLE);
                }
                else
                    databaseReference.addChildEventListener(childEventListener);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        if(childEventListener!=null)
            databaseReference.removeEventListener(childEventListener);

        userCount = readUserCount = 0;

    }

}
