package com.android.developer.feedingindia.activities;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
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
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class DonorSignUpActivity extends AppCompatActivity {

    private EditText nameEditText,emailEditText,passwordEditText,mobileNumberEditText;
    private TextView dobTextView;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String userName,userEmail,userPassword,userMobileNumber,userDoB,userCity;
    private int mDay,mMonth,mYear;
    private Calendar mCalender;
    private ProgressDialog mProgressDialog;
    private Spinner spinner;
    public static PhoneAuthProvider.ForceResendingToken token;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_donor_sign_up);

        nameEditText = findViewById(R.id.nameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        mobileNumberEditText = findViewById(R.id.mobileNumberEditText);
        dobTextView = findViewById(R.id.dobTextView);
        userDoB = "empty";
        mCalender = Calendar.getInstance();
        mYear = mCalender.get(Calendar.YEAR);
        mMonth = mCalender.get(Calendar.MONTH);
        mDay = mCalender.get(Calendar.DAY_OF_MONTH);
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Sending Verification Code...");

        spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> mAdapter = ArrayAdapter.createFromResource(this,R.array.cities,android.R.layout.simple_spinner_item);
        mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(mAdapter);


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
                Intent intent = new Intent(DonorSignUpActivity.this,VerificationActivity.class);
                intent.putExtra("userName",userName);
                intent.putExtra("userEmail",userEmail);
                intent.putExtra("userPassword",userPassword);
                intent.putExtra("userCity",userCity);
                intent.putExtra("userMobileNumber",userMobileNumber);
                intent.putExtra("userDoB",userDoB);
                intent.putExtra("userType","normal");
                intent.putExtra("verificationId",verificationId);
                startActivity(intent);

            }

            @Override
            public void onCodeAutoRetrievalTimeOut(String s) {
                super.onCodeAutoRetrievalTimeOut(s);
            }

        };

    }

    public void onClickDatePickerButton(View view){

        final DatePickerDialog mDatePickerDialog = new DatePickerDialog(DonorSignUpActivity.this,new DatePickerDialog.OnDateSetListener() {
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

        if(userName.isEmpty() || userEmail.isEmpty() || userPassword.isEmpty() || userMobileNumber.isEmpty() || userDoB.equals("empty"))
            makeToast("Fields cannot be empty!");
        else if(!Patterns.EMAIL_ADDRESS.matcher(userEmail).matches())
            makeToast("Enter a valid email!");
        else if(userPassword.length()<6)
            makeToast("Password should be minimum of 6 characters!");
        else{
            //Verify Phone Number
            PhoneNumberUtil mPhoneNumberUtil = PhoneNumberUtil.getInstance();
            try {
                Phonenumber.PhoneNumber mPhoneNumber = mPhoneNumberUtil.parse(userMobileNumber,"IN");
                if(mPhoneNumberUtil.isValidNumber(mPhoneNumber))
                {
                    userMobileNumber = mPhoneNumberUtil.format(mPhoneNumber,PhoneNumberUtil.PhoneNumberFormat.E164);

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            userMobileNumber,
                            60,
                            TimeUnit.SECONDS,
                            this,
                            mCallbacks);
                    mProgressDialog.show();
                }
                else
                    makeToast("Please enter a valid mobile number!");
            } catch (NumberParseException e) {
                makeToast(e.getMessage());
            }
        }
    }

    public void makeToast(String message){
        Toast.makeText(DonorSignUpActivity.this,message,Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}
