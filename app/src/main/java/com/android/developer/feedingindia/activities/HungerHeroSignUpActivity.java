package com.android.developer.feedingindia.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

public class HungerHeroSignUpActivity extends AppCompatActivity implements View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    private EditText nameEditText,emailEditText,passwordEditText,mobileNumberEditText,educationalBackgroundEditText,
            currentlyPartOfEditText,localityEditText,pinCodeEditText,reasonForJoiningEditText,aboutMeEditText;
    private String userName,userEmail,userPassword,userMobileNumber,userDoB,city;
    public static String educationalBackground,currentlyPartOf,locality,pinCode,reasonForJoining;
    public static String responsibility = "hungerhero";
    public static String affordableTime = "3-6 hours";
    public static String state;
    public static ArrayList<String> aboutMeList,introducedToFIThrough;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private int mDay,mMonth,mYear;
    private ProgressDialog mProgressDialog;
    public static PhoneAuthProvider.ForceResendingToken token;
    private Spinner stateSpinner,citySpinner;
    private CheckBox checkBox1,checkBox2,checkBox3,checkBox4;
    private RadioButton radioButton1,radioButton2,radioButton3,radioButton4,radioButton5,radioButton6,radioButton7;
    private TextView textView1,textView2,textView3,textView4,dobTextView;
    private Intent intent;
    private Calendar mCalender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hunger_hero_sign_up);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText);
        educationalBackgroundEditText = findViewById(R.id.educationalBackgroundEditText);
        currentlyPartOfEditText = findViewById(R.id.currentlyPartOfEditText);
        localityEditText = findViewById(R.id.localityEditText);
        pinCodeEditText = findViewById(R.id.pinCodeEditText);
        reasonForJoiningEditText = findViewById(R.id.reasonForJoiningEditText);
        aboutMeEditText = findViewById(R.id.aboutMeEditText);

        responsibility = "hungerhero";
        affordableTime = "3-6 hours";
        userDoB = "empty";
        introducedToFIThrough = new ArrayList<>();

        mCalender = Calendar.getInstance();
        mYear = mCalender.get(Calendar.YEAR);
        mMonth = mCalender.get(Calendar.MONTH);
        mDay = mCalender.get(Calendar.DAY_OF_MONTH);

        stateSpinner = findViewById(R.id.stateSpinner);
        citySpinner = findViewById(R.id.citySpinner);

        checkBox1 = findViewById(R.id.checkBox1);
        checkBox2 = findViewById(R.id.checkBox2);
        checkBox3 = findViewById(R.id.checkBox3);
        checkBox4 = findViewById(R.id.checkBox4);

        radioButton1 = findViewById(R.id.radioButton1);
        radioButton2 = findViewById(R.id.radioButton2);
        radioButton3 = findViewById(R.id.radioButton3);
        radioButton4 = findViewById(R.id.radioButton4);
        radioButton5 = findViewById(R.id.radioButton5);
        radioButton6 = findViewById(R.id.radioButton6);
        radioButton7 = findViewById(R.id.radioButton7);

        textView1 = findViewById(R.id.textView1);
        textView2 = findViewById(R.id.textView2);
        textView3 = findViewById(R.id.textView3);
        textView4 = findViewById(R.id.textView4);
        dobTextView = findViewById(R.id.dobTextView);

        ArrayAdapter<CharSequence> stateSpinnerAdapter = ArrayAdapter.createFromResource(this, R.array.india_states, android.R.layout.simple_spinner_item);
        stateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stateSpinner.setAdapter(stateSpinnerAdapter);

        ArrayAdapter<CharSequence> citySpinnerAdapter = ArrayAdapter.createFromResource(this,R.array.cities,android.R.layout.simple_spinner_item);
        citySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        citySpinner.setAdapter(citySpinnerAdapter);

        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Sending Verification Code...");

    }

    @Override
    protected void onResume() {
        super.onResume();

        attachListeners();

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

                token = forceResendingToken;
                intent = new Intent(HungerHeroSignUpActivity.this,VerificationActivity.class);
                intent.putExtra("userName",userName);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("userPassword",userPassword);
                intent.putExtra("userCity",city);
                intent.putExtra("userMobileNumber",userMobileNumber);
                intent.putExtra("userDoB",userDoB);
                intent.putExtra("userType","hungerhero");
                intent.putExtra("verificationId",verificationId);
                startActivity(intent);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

        };

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                state = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        citySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                city = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private void attachListeners(){

        checkBox1.setOnCheckedChangeListener(this);
        checkBox2.setOnCheckedChangeListener(this);
        checkBox3.setOnCheckedChangeListener(this);
        checkBox4.setOnCheckedChangeListener(this);
        radioButton1.setOnClickListener(this);
        radioButton2.setOnClickListener(this);
        radioButton3.setOnClickListener(this);
        radioButton4.setOnClickListener(this);
        radioButton5.setOnClickListener(this);
        radioButton6.setOnClickListener(this);
        radioButton7.setOnClickListener(this);
        textView1.setOnClickListener(this);
        textView2.setOnClickListener(this);
        textView3.setOnClickListener(this);
        textView4.setOnClickListener(this);

    }

    public void onClickDatePickerButton(View view){

        DatePickerDialog mDatePickerDialog = new DatePickerDialog(HungerHeroSignUpActivity.this,new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int dayOfMonth) {

                userDoB = dayOfMonth+"/"+(month+1)+"/"+year;
                mDay = dayOfMonth;
                mMonth = month;
                mYear = year;
                dobTextView.setText(userDoB);

            }
        },mYear,mMonth,mDay);

        mDatePickerDialog.getDatePicker().setMaxDate(mCalender.getTimeInMillis());
        mDatePickerDialog.show();
    }

    public void onClickSubmitButton(View view){


        userName = nameEditText.getText().toString().trim();
        userEmail = emailEditText.getText().toString().trim();
        userPassword = passwordEditText.getText().toString();
        userMobileNumber = mobileNumberEditText.getText().toString().trim();
        educationalBackground = educationalBackgroundEditText.getText().toString().trim();
        currentlyPartOf = currentlyPartOfEditText.getText().toString().trim();
        locality = localityEditText.getText().toString().trim();
        pinCode = pinCodeEditText.getText().toString().trim();
        reasonForJoining = reasonForJoiningEditText.getText().toString().trim();
        String aboutMe[] = aboutMeEditText.getText().toString().trim().split(",");
        aboutMeList = new ArrayList<>();

        if(userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() || userMobileNumber.isEmpty() ||
                userDoB.equals("empty") || educationalBackground.isEmpty() || currentlyPartOf.isEmpty()  ||
                locality.isEmpty() || pinCode.isEmpty() || reasonForJoining.isEmpty() || !(aboutMe.length>0) || !(introducedToFIThrough.size()>0))
            makeToast("Fields cannot be empty!");
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
            makeToast("Enter a valid email!");
        else if(userPassword.length()<6)
            makeToast("Password should be minimum of 6 characters!");

        else{

            Collections.addAll(aboutMeList,aboutMe);
            //Verify Phone Number
            PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber mPhoneNumber = mPhoneNumberUtil.parse(userMobileNumber,"IN");
                if(mPhoneNumberUtil.isValidNumber(mPhoneNumber))
                {
                    mProgressDialog.show();
                    userMobileNumber = mPhoneNumberUtil.format(mPhoneNumber,PhoneNumberUtil.PhoneNumberFormat.E164);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            userMobileNumber,
                            60,
                            TimeUnit.SECONDS,
                            this,
                            mCallbacks);
                }
                else
                    makeToast("Please enter a valid mobile number!");
            } catch (NumberParseException e) {
                makeToast(e.getMessage());
            }
        }

    }

    public void makeToast(String message){
        Toast.makeText(HungerHeroSignUpActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.textView1:
                openBrowser("https://www.feedingindia.org/get-involved");
                break;

            case R.id.textView2:
                openBrowser("https://www.feedingindia.org/the-challenge");
                break;

            case R.id.textView3:
                openBrowser("https://www.facebook.com/feedingindia");
                break;

            case R.id.textView4:
                openBrowser("https://www.feedingindia.org");
                break;

            case R.id.radioButton1 :
                affordableTime = "3-6 hours";
                break;

            case R.id.radioButton2 :
                affordableTime = "6-9 hours";
                break;

            case R.id.radioButton3 :
                affordableTime = "9-12 hours";
                break;

            case R.id.radioButton4 :
                responsibility = "hungerhero";
                break;

            case R.id.radioButton5 :
                responsibility = "superhero";
                break;

            case R.id.radioButton6 :
                affordableTime = "12-15 hours";
                break;

            case R.id.radioButton7 :
                affordableTime = "15+ hours";
        }

    }

    private void openBrowser(String url){

        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);

    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {

        switch (compoundButton.getId()){

            case R.id.checkBox1 :
                addOrRemove("Facebook",checked);
                break;

            case R.id.checkBox2 :
                addOrRemove("Website",checked);
                break;

            case R.id.checkBox3 :
                addOrRemove("Media/News",checked);
                break;

            case R.id.checkBox4 :
                addOrRemove("Through a Friend",checked);
                break;

        }
    }

    private void addOrRemove(String s,boolean checked){

        if(checked){
            if(!introducedToFIThrough.contains(s))
                introducedToFIThrough.add(s);
        }
        else
        if(introducedToFIThrough.contains(s))
            introducedToFIThrough.remove(s);

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}