package com.android.developer.feedingindia.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private EditText emailEditText;
    private EditText passwordEditText;
    private TextView forgotPasswordTextView,donateTextView;
    private CheckBox rememberMeCheckBox;
    private ProgressDialog mProgressDialog;
    private Intent mIntent;
    private SharedPreferences mSharedPreferences;
    private AlertDialog mAlertDialog;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private FirebaseUser mFireBaseUser;
    private ImageButton facebookIcon,twitterIcon;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        donateTextView = findViewById(R.id.donate);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox);
        facebookIcon = findViewById(R.id.facebook_icon);
        twitterIcon = findViewById(R.id.twitter_icon);

        mAuth = FirebaseAuth.getInstance();

        mSharedPreferences = this.getSharedPreferences(getPackageName(), MODE_PRIVATE);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);

        if(mSharedPreferences.getBoolean("remember",false))
            rememberMeCheckBox.setChecked(true);

        AlertDialog.Builder mBuilder;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            mBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
        else
            mBuilder = new AlertDialog.Builder(this);

        mAlertDialog = mBuilder.setTitle("Send Verification Mail")

                .setMessage("Not received the verification mail yet?")

                .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        if(mAuth.getCurrentUser()!=null)
                        mAuth.getCurrentUser().sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                mAuth.signOut();

                                if(task.isSuccessful())
                                    makeToast("Mail sent!");
                                else
                                    makeToast(task.getException().getMessage());

                            }

                        });

                    }
                })

                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {

                        mAuth.signOut();

                    } })

                .setOnDismissListener(new DialogInterface.OnDismissListener() {

                        @Override
                    public void onDismiss(DialogInterface dialogInterface) {

                            mAuth.signOut();

                    }
                }).setCancelable(false).create();

        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            /*Triggered when attached to a FireBase Auth object
            and every time the auth state changes i.e,when the user
            signs in and signs out.
            */
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {

                mFireBaseUser = firebaseAuth.getCurrentUser();

                if (mFireBaseUser != null) {
                    //Signed In


                    if(!mSharedPreferences.getString("email","").equals(mAuth.getCurrentUser().getEmail())){

                        //Fetch Data
                        DatabaseReference mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(mAuth.getUid());
                        mDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                                HashMap<String,Object> userData = (HashMap) dataSnapshot.getValue();
                                String userType = userData.get("userType").toString();
                                mSharedPreferences.edit().putString("email",userData.get("email").toString()).apply();
                                mSharedPreferences.edit().putString("doB",userData.get("dateOfBirth").toString()).apply();
                                mSharedPreferences.edit().putString("userType",userType).apply();
                                mSharedPreferences.edit().putString("mobileNumber",userData.get("mobileNumber").toString()).apply();
                                mSharedPreferences.edit().putString("name",userData.get("name").toString()).apply();
                                mSharedPreferences.edit().putString("city",userData.get("city").toString()).apply();
                                Log.i("Hello", "onDataChange: " + userType + mAuth.getCurrentUser().getEmail());
                                if(userType.equals("hungerhero"))
                                    mSharedPreferences.edit().putBoolean("emailVerified",(boolean)userData.get("emailVerified")).apply();
                                if(userType.equals("hungerhero")) {
                                    checkIfEmailIsVerified();
                                }
                                else
                                {
                                    if(mProgressDialog.isShowing())
                                    mProgressDialog.cancel();
                                    mIntent = new Intent(SignInActivity.this,MainActivity.class);
                                    startActivity(mIntent);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                                if(mProgressDialog.isShowing())
                                    mProgressDialog.cancel();
                                mAuth.signOut();
                                makeToast(databaseError.getMessage());

                            }
                        });

                    }
                    else {

                        Log.i("Hello", "onAuthStateChanged: ");
                        String userType = mSharedPreferences.getString("userType","");
                        if(userType.equals("hungerhero")) {
                            checkIfEmailIsVerified();
                        }
                        else{
                            if(!userType.equals("")) {
                                if (mProgressDialog.isShowing())
                                    mProgressDialog.cancel();
                                mIntent = new Intent(SignInActivity.this, MainActivity.class);
                                startActivity(mIntent);
                            }
                        }

                    }
                }
            }
        };

    }

    private void checkIfEmailIsVerified(){

        if(mProgressDialog.isShowing())
            mProgressDialog.cancel();

        if(mFireBaseUser!=null)
        if (mFireBaseUser.isEmailVerified() || mSharedPreferences.getBoolean("emailVerified",false)) {
            //Check if the email is verified
            Log.i("Hello", "checkIfEmailIsVerified: ");
            mIntent = new Intent(SignInActivity.this,MainActivity.class);
            startActivity(mIntent);
        }

        else {
            mAlertDialog.show();
            makeToast("Email not verified yet!");

        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(mAuthStateListener);

        if(mSharedPreferences.getBoolean("remember",false))
        {
            emailEditText.setText(mSharedPreferences.getString("email",""));
            passwordEditText.setText(mSharedPreferences.getString("password",""));
        }

        rememberMeCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

                if(checked)
                    mSharedPreferences.edit().putBoolean("remember", true).apply();
                else
                    mSharedPreferences.edit().putBoolean("remember", false).apply();
            }
        });

        forgotPasswordTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mIntent = new Intent(SignInActivity.this,ResetPasswordActivity.class);
                startActivity(mIntent);

            }
        });

        donateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.feedingindia.org/donate"));
            }
        });

        facebookIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.facebook.com/feedingindia"));
                startActivity(intent);
            }
        });

        twitterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.twitter.com/FeedingIndia"));
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();

        //Detaching listener when the activity is no longer visible

        if(mAuthStateListener!=null) {
            Log.i("Hello", "onPause: ");
            mAuth.removeAuthStateListener(mAuthStateListener);
        }

        if(mAlertDialog.isShowing())
            mAlertDialog.dismiss();

    }

    public void onClickSignInButton(View view){

        final String userEmail,userPassword;

        userEmail = emailEditText.getText().toString().trim();
        userPassword = passwordEditText.getText().toString();

        if(userEmail.isEmpty() || userPassword.isEmpty())
            makeToast("Fields cannot be empty!");

        else {

            mProgressDialog.setMessage("Signing in...");
            mProgressDialog.show();

            mAuth.signInWithEmailAndPassword(userEmail, userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {
                        //Signed in
                        mSharedPreferences.edit().putString("password",userPassword).apply();

                    }
                    else {
                        //Sign In Failed
                        mProgressDialog.cancel();
                        makeToast(task.getException().getMessage());
                    }
                }
            });
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {

        super.onConfigurationChanged(newConfig);

    }

    public void onClickDonorSignUpButton(View view){

        mIntent = new Intent(SignInActivity.this,DonorSignUpActivity.class);
        startActivity(mIntent);

    }

    public void onClickHungerHeroSignUpButton(View view){

        mIntent = new Intent(SignInActivity.this,HungerHeroSignUpActivity.class);
        startActivity(mIntent);

    }

    public void onClickAlreadyHungerHero(View view){

        mIntent = new Intent(SignInActivity.this,ExistingHungerHeroActivity.class);
        startActivity(mIntent);

    }

    public void makeToast(String message){
        Toast.makeText(SignInActivity.this,message,Toast.LENGTH_SHORT).show();
    }

}
