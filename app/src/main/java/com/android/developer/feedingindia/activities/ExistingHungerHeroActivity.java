package com.android.developer.feedingindia.activities;

import android.app.ProgressDialog;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.HungerHero;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class ExistingHungerHeroActivity extends AppCompatActivity {

    private EditText emailEditText,passwordEditText;
    private Spinner spinner;
    private String userCity,userEmail,userPassword;
    private ProgressDialog mProgressDialog;
    private ChildEventListener mChildEventListener;
    private DatabaseReference mDatabaseReference;
    private long userCount;
    private long readCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_existing_hunger_hero);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        userCount = readCount = 0;

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this,R.array.cities,android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait...\nSigning Up");

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Temporary");

        mChildEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull final DataSnapshot dataSnapshot, @Nullable String s) {

                HashMap<String,Object> temp = (HashMap)dataSnapshot.getValue();

                if(temp.get("email").equals(userEmail)){

                    mDatabaseReference.removeEventListener(mChildEventListener);

                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

                    temp.put("city",userCity);
                    temp.put("requestedToBeAdmin",false);
                    temp.put("userType","hungerhero");
                    temp.put("previousRole","hungerhero");
                    temp.put("emailVerified",true);
                    ObjectMapper mObjectMapper = new ObjectMapper();
                    final HungerHero hungerHero = mObjectMapper.convertValue(temp, HungerHero.class);
                    mAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){

                                FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid()).setValue(hungerHero).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {

                                        if(task.isSuccessful()){

                                            mAuth.signOut();
                                            makeToast("Welcome to Feeding India!\nAccount Created\nYou can sign in with your email and password");
                                            FirebaseDatabase.getInstance().getReference().child("Temporary").child(dataSnapshot.getKey()).removeValue();

                                            new CountDownTimer(1000,1000) {
                                                @Override
                                                public void onTick(long l) {

                                                }

                                                @Override
                                                public void onFinish() {

                                                    mProgressDialog.cancel();
                                                    moveTaskToBack(true);
                                                    android.os.Process.killProcess(android.os.Process.myPid());
                                                    System.exit(1);

                                                }
                                            }.start();


                                        }
                                        else{
                                            mProgressDialog.cancel();
                                            makeToast(task.getException().getMessage());
                                            mAuth.getCurrentUser().delete();
                                        }

                                    }
                                });
                            }
                            else{
                                mProgressDialog.cancel();
                                makeToast(task.getException().getMessage());
                            }
                        }
                    });

                }
                else{

                    readCount++;
                    if(userCount==readCount){
                        mProgressDialog.cancel();
                        makeToast("Sorry!The email id does not match any existing hungerheroes");
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
    protected void onResume() {
        super.onResume();


        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                userCity = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    public void onClickSubmitButton(View view) {

        userEmail = emailEditText.getText().toString().trim();
        userPassword = passwordEditText.getText().toString();

        if(userEmail.isEmpty() || userPassword.isEmpty())
            makeToast("Fields cannot be empty!");
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
            makeToast("Enter a valid email!");
        else if(userPassword.length()<6)
            makeToast("Password should be minimum of 6 characters!");
        else{

            mProgressDialog.show();

            mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    userCount = readCount = 0;

                    userCount = dataSnapshot.getChildrenCount();

                    if(userCount==0) {
                        mProgressDialog.cancel();
                        makeToast("Sorry!The email id does not match any existing hungerheroes");
                    }
                    else {

                        //Create a hungerhero object and push to database
                        mDatabaseReference.addChildEventListener(mChildEventListener);
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                    mProgressDialog.cancel();
                    makeToast(databaseError.getMessage());

                }
            });
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if(mChildEventListener!=null)
            mDatabaseReference.removeEventListener(mChildEventListener);

        userCount = readCount = 0;

    }

    public void makeToast(String message){
        Toast.makeText(ExistingHungerHeroActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

}