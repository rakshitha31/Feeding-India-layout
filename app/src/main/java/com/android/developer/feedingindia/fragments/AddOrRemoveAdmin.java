package com.android.developer.feedingindia.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class AddOrRemoveAdmin extends Fragment {

    private EditText emailEditText;
    private Button makeAdminButton,removeAdminButton;

    public AddOrRemoveAdmin() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_add_or_remove_admin, container, false);
        emailEditText = view.findViewById(R.id.emailEditText);
        makeAdminButton = view.findViewById(R.id.makeAdminButton);
        removeAdminButton = view.findViewById(R.id.removeAdminButton);
        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        makeAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickMakeAdmin();
            }
        });
        removeAdminButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickRemoveAdmin();
            }
        });

    }

    private void onClickRemoveAdmin(){

        String email = emailEditText.getText().toString().trim();

        if(email.isEmpty())
            makeToast("Please enter the email");
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            makeToast("Enter a valid email!");
        else if(email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            makeToast("This task cannot be done!");
        else{
            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.getChildrenCount()==0)
                        makeToast("The person is not a user yet!");
                    else
                    {
                        HashMap<String,HashMap<String,Object>> value = (HashMap)dataSnapshot.getValue();
                        Set mySet = value.entrySet();
                        Iterator iterator = mySet.iterator();
                        Map.Entry myMapEntry = (Map.Entry) iterator.next();
                        String userUID = myMapEntry.getKey().toString();
                        HashMap<String,Object> userToBeRemovedAsAdmin = (HashMap<String,Object>)myMapEntry.getValue();
                        if(userToBeRemovedAsAdmin.get("userType").equals("admin")){
                            userToBeRemovedAsAdmin.put("userType", userToBeRemovedAsAdmin.get("previousRole"));
                            userToBeRemovedAsAdmin.put("requestedToBeAdmin", false);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);
                            databaseReference.setValue(userToBeRemovedAsAdmin);
                            makeToast("Success!");
                        }

                        else {
                            makeToast("The person is not an admin");
                        }
                        emailEditText.setText("");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    makeToast(databaseError.getMessage());

                }
            });
        }


    }

    private void onClickMakeAdmin()
    {

        String email = emailEditText.getText().toString().trim();

        if(email.isEmpty())
            makeToast("Please enter the email");
        else if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
            makeToast("Enter a valid email!");
        else if(email.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail()))
            makeToast("You are already an admin!");
        else{
            Query query = FirebaseDatabase.getInstance().getReference().child("Users").orderByChild("email").equalTo(email);

            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    if(dataSnapshot.getChildrenCount()==0)
                        makeToast("The person is not a user yet!");
                    else
                    {
                        HashMap<String,HashMap<String,Object>> value = (HashMap)dataSnapshot.getValue();
                        Set mySet = value.entrySet();
                        Iterator iterator = mySet.iterator();
                        Map.Entry myMapEntry = (Map.Entry) iterator.next();
                        String userUID = myMapEntry.getKey().toString();
                        HashMap<String,Object> userToBeAdmin = (HashMap<String,Object>)myMapEntry.getValue();
                        if(userToBeAdmin.get("userType").equals("admin"))
                            makeToast("The person is already an admin");
                        else {
                            userToBeAdmin.put("requestedToBeAdmin", true);
                            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(userUID);
                            databaseReference.setValue(userToBeAdmin);
                            makeToast("Success!");
                        }
                        emailEditText.setText("");
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    makeToast(databaseError.getMessage());

                }
            });
        }

    }


    private void makeToast(String message){
        Toast.makeText(getContext(),message,Toast.LENGTH_SHORT).show();
    }

}
