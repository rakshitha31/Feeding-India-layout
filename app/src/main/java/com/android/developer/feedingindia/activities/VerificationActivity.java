package com.android.developer.feedingindia.activities;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.FeedingIndiaDonor;
import com.android.developer.feedingindia.pojos.HungerHero;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.concurrent.TimeUnit;

public class VerificationActivity extends AppCompatActivity {

    private TextView timerTextView;
    private EditText verificationCodeEditText;
    private ProgressDialog mProgressDialog;
    private DatabaseReference mDatabaseReference;
    private String userName,userEmail,userPassword,userMobileNumber,userDoB,userType,verificationId,userCity;
    private CountDownTimer mCountDownTimer;
    private PhoneAuthProvider.ForceResendingToken token;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private static final String TAG = "VerificationActivity";
    private long left;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verification);

        timerTextView = findViewById(R.id.timerTextView);
        verificationCodeEditText = findViewById(R.id.verificationCodeEditText);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        Intent intent = getIntent();
        userName = intent.getStringExtra("userName");
        userEmail = intent.getStringExtra("userEmail");
        userPassword = intent.getStringExtra("userPassword");
        userMobileNumber = intent.getStringExtra("userMobileNumber");
        userDoB = intent.getStringExtra("userDoB");
        userType = intent.getStringExtra("userType");
        userCity = intent.getStringExtra("userCity");
        verificationId = intent.getStringExtra("verificationId");
        token = (userType.equals("hungerhero")) ? HungerHeroSignUpActivity.token : DonorSignUpActivity.token;
        left = 1000;

        startTimer(65100);
    }

    private CountDownTimer getCountDownTimer(long initialMilliSecs){

        return new CountDownTimer(initialMilliSecs, 1000) {
            @Override
            public void onTick(long l) {

                long sec = l / 1000 - 5;
                left = l;
                if(left < 5000){
                    makeToast("Time out! Try again");
                    finish();
                }
                else {
                    String mTimeToDisplay = sec + ":00";
                    timerTextView.setText(mTimeToDisplay);
                }
            }

            @Override
            public void onFinish() {


            }
        };

    }

    @Override
    protected void onResume() {

        super.onResume();

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                mProgressDialog.cancel();
                makeToast(e.getMessage());
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                super.onCodeSent(verificationId, forceResendingToken);
                mProgressDialog.cancel();
                startTimer(65100);
            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }
        };

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mCountDownTimer!=null)
        mCountDownTimer.cancel();
    }

    private void startTimer(long initialMilliSecs){

        if(mCountDownTimer!=null)
        mCountDownTimer.cancel();

        mCountDownTimer = getCountDownTimer(initialMilliSecs);
        mCountDownTimer.start();

    }

    public void onClickResendCode(View view){

        mProgressDialog.setMessage("Resending Code");
        mProgressDialog.show();
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                userMobileNumber,
                60,
                TimeUnit.SECONDS,
                this,
                mCallbacks,
                token);
    }

    public void onClickVerifyButton(View view){

        String codeEntered = verificationCodeEditText.getText().toString().trim();

        if(codeEntered.isEmpty())
            makeToast("Please enter the verification code");
        else {

            mCountDownTimer.cancel();

            mProgressDialog.setMessage("Verifying Credentials");
            mProgressDialog.show();

            final PhoneAuthCredential mPhoneAuthCredential = PhoneAuthProvider.getCredential(verificationId,codeEntered);

            final FirebaseAuth mAuth = FirebaseAuth.getInstance();

            mAuth.signInWithCredential(mPhoneAuthCredential).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if(task.isSuccessful())
                    {

                        /*Since we want users to sign up with their email,auth object constructed using
                          the phone number is deleted */
                        mAuth.getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()) {
                                    Log.i(TAG, "onComplete: " + "wo1");
                                    signUpWithEmail();
                                }
                                else
                                    deleteUser();

                            }
                        });


                    }
                    else {

                        mProgressDialog.cancel();

                        makeToast(task.getException().getMessage());

                        if(left<=6000)
                        {
                            makeToast("Time out! Try again");
                            finish();
                        }

                        verificationCodeEditText.setText("");
                        startTimer(left);
                        }
                }
            });
        }

    }

    private void signUpWithEmail()
    {

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.createUserWithEmailAndPassword(userEmail,userPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {

            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if(task.isSuccessful()) {

                    if(userType.equals("hungerhero")){
                        //User wants to be a hungerhero
                        HungerHero hungerHero = new HungerHero(userName,userDoB,userEmail,userMobileNumber,
                         "hungerhero",HungerHeroSignUpActivity.educationalBackground,HungerHeroSignUpActivity.state,
                          userCity,HungerHeroSignUpActivity.locality,HungerHeroSignUpActivity.pinCode,
                          HungerHeroSignUpActivity.reasonForJoining,HungerHeroSignUpActivity.affordableTime,
                          HungerHeroSignUpActivity.responsibility,HungerHeroSignUpActivity.currentlyPartOf,
                          HungerHeroSignUpActivity.introducedToFIThrough,HungerHeroSignUpActivity.aboutMeList,false,"hungerhero",false);

                        mDatabaseReference.child(mAuth.getCurrentUser().getUid()).setValue(hungerHero).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()) {
                                    if(mAuth.getCurrentUser()!=null)
                                        mAuth.getCurrentUser().sendEmailVerification();
                                    endTask();
                                }
                                else
                                    deleteUser();

                            }
                        });

                    }
                    else {
                        //User wants to be a donor
                        FeedingIndiaDonor feedingIndiaDonor = new FeedingIndiaDonor(userName, userEmail, userMobileNumber, userDoB, "normal",userCity,false,"normal");
                        mDatabaseReference.child(mAuth.getCurrentUser().getUid()).setValue(feedingIndiaDonor).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {

                                if(task.isSuccessful()) {
                                    endTask();
                                }
                                else
                                    deleteUser();

                            }
                        });
                    }
                }
                else {

                    mProgressDialog.cancel();
                    makeToast(task.getException().getMessage());
                    finish();
                }
            }
        });
    }

    private void deleteUser(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null)
            mAuth.getCurrentUser().delete();
        mProgressDialog.cancel();
        makeToast("Sorry,could not create your account!Please try again");
        finish();

    }

    private void endTask(){

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(mAuth.getCurrentUser()!=null) {
            mAuth.signOut();
            mProgressDialog.cancel();
            makeToast("Welcome to Feeding India!");
            finish();
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void makeToast(String message){
        Toast.makeText(VerificationActivity.this,message,Toast.LENGTH_SHORT).show();
    }
}
