package com.android.developer.feedingindia.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.developer.feedingindia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class ProfileFragment extends Fragment {

    private TextView textView1,textView2,textView3,textView4,textView5,textView6;
    private SharedPreferences mSharedPreferences;
    private ProgressBar progressBar;
    private LinearLayout mLinearLayout;

    public ProfileFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile, container, false);
        progressBar = view.findViewById(R.id.progressBar);
        mLinearLayout = view.findViewById(R.id.profileContainer);
        textView1 = view.findViewById(R.id.userName);
        textView2 = view.findViewById(R.id.userDoB);
        textView3 = view.findViewById(R.id.userEmail);
        textView4 = view.findViewById(R.id.userType);
        textView5 = view.findViewById(R.id.userMobileNumber);
        textView6 = view.findViewById(R.id.number_of_meals);

        return  view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textView1.setText(mSharedPreferences.getString("name",""));
        textView2.setText(mSharedPreferences.getString("doB",""));
        textView3.setText(mSharedPreferences.getString("email",""));
        textView5.setText(mSharedPreferences.getString("mobileNumber",""));

        String role = mSharedPreferences.getString("userType","");

        switch (role){
            case "admin" :
                role = "ADMIN";
                break;
            case "normal" :
                role = "DONOR";
                break;
            case "hungerhero" :
                role = "HUNGERHERO";
                break;
            case "superadmin":
                role = "SUPERADMIN";
                break;
        }

        textView4.setText(role);

    }

    @Override
    public void onResume() {
        super.onResume();

        progressBar.setVisibility(View.VISIBLE);
        mLinearLayout.setVisibility(View.INVISIBLE);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Donations").child(FirebaseAuth.getInstance().getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                textView6.setText(String.valueOf(dataSnapshot.getChildrenCount()));
                enableUserInteraction();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void enableUserInteraction()
    {
        progressBar.setVisibility(View.GONE);
        mLinearLayout.setVisibility(View.VISIBLE);
    }
}
