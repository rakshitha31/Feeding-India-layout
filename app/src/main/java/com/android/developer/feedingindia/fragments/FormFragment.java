package com.android.developer.feedingindia.fragments;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.android.developer.feedingindia.pojos.HungerHero;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.ArrayList;
import java.util.Collections;

public class FormFragment extends Fragment implements  View.OnClickListener,CompoundButton.OnCheckedChangeListener{

    private EditText educationalBackgroundEditText,currentlyPartOfEditText,localityEditText,
                     pinCodeEditText,reasonForJoiningEditText,aboutMeEditText;
    private String responsibility;
    private String affordableTime;
    private String state;
    private ArrayList<String> introducedToFIThrough;
    private Spinner stateSpinner;
    private ArrayAdapter<CharSequence> stateSpinnerAdapter;
    private Button hungerHeroDetailsSubmitButton;
    private TextView textView1,textView2,textView3,textView4;
    private CheckBox checkBox1,checkBox2,checkBox3,checkBox4;
    private RadioButton radioButton1,radioButton2,radioButton3,radioButton4,radioButton5,radioButton6,radioButton7;
    private ProgressDialog mProgressDialog;
    private SharedPreferences mSharedPreferences;
    private DatabaseReference mDatabaseReference;

    public FormFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        introducedToFIThrough = new ArrayList<>();
        mSharedPreferences = getActivity().getSharedPreferences("com.android.developer.feedingindia", Context.MODE_PRIVATE);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users").child(FirebaseAuth.getInstance().getUid());

        mProgressDialog = new ProgressDialog(getContext());
        mProgressDialog.setCancelable(false);
        mProgressDialog.setMessage("Please wait...");


        stateSpinnerAdapter = ArrayAdapter.createFromResource(getContext(), R.array.india_states, android.R.layout.simple_spinner_item);
        stateSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        responsibility = "hungerhero";
        affordableTime = "3-6 hours";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_form, container, false);
        educationalBackgroundEditText = view.findViewById(R.id.educationalBackgroundEditText);
        currentlyPartOfEditText = view.findViewById(R.id.currentlyPartOfEditText);
        localityEditText = view.findViewById(R.id.localityEditText);
        pinCodeEditText = view.findViewById(R.id.pinCodeEditText);
        reasonForJoiningEditText = view.findViewById(R.id.reasonForJoiningEditText);
        aboutMeEditText = view.findViewById(R.id.aboutMeEditText);
        stateSpinner = view.findViewById(R.id.stateSpinner);
        stateSpinner.setAdapter(stateSpinnerAdapter);
        hungerHeroDetailsSubmitButton = view.findViewById(R.id.submitButton);
        checkBox1 = view.findViewById(R.id.checkBox1);
        checkBox2 = view.findViewById(R.id.checkBox2);
        checkBox3 = view.findViewById(R.id.checkBox3);
        checkBox4 = view.findViewById(R.id.checkBox4);
        radioButton1 = view.findViewById(R.id.radioButton1);
        radioButton2 = view.findViewById(R.id.radioButton2);
        radioButton3 = view.findViewById(R.id.radioButton3);
        radioButton4 = view.findViewById(R.id.radioButton4);
        radioButton5 = view.findViewById(R.id.radioButton5);
        radioButton6 = view.findViewById(R.id.radioButton6);
        radioButton7 = view.findViewById(R.id.radioButton7);
        textView1 = view.findViewById(R.id.textView1);
        textView2 = view.findViewById(R.id.textView2);
        textView3 = view.findViewById(R.id.textView3);
        textView4 = view.findViewById(R.id.textView4);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        attachListeners();

        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                state = adapterView.getItemAtPosition(i).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        hungerHeroDetailsSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onClickSubmitButton();
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

    private void onClickSubmitButton(){

        String educationalBackground = educationalBackgroundEditText.getText().toString().trim();
        String currentlyPartOf = currentlyPartOfEditText.getText().toString().trim();
        String locality = localityEditText.getText().toString().trim();
        String pinCode = pinCodeEditText.getText().toString().trim();
        String reasonForJoining = reasonForJoiningEditText.getText().toString().trim();
        String aboutMe[] = aboutMeEditText.getText().toString().trim().split(",");
        ArrayList<String> aboutMeList = new ArrayList<>();

        if(educationalBackground.isEmpty() || currentlyPartOf.isEmpty()||
           locality.isEmpty() || pinCode.isEmpty() || reasonForJoining.isEmpty() || !(aboutMe.length>0)
            || !(introducedToFIThrough.size()>0))
            makeToast("Fields cannot be empty!");

        else {
            mProgressDialog.show();
            Collections.addAll(aboutMeList,aboutMe);
            String name = mSharedPreferences.getString("name","");
            String dateOfBirth = mSharedPreferences.getString("doB","");
            String email = mSharedPreferences.getString("email","");
            String mobileNumber = mSharedPreferences.getString("mobileNumber","");
            String city = mSharedPreferences.getString("city","");
            mSharedPreferences.edit().putString("userType","hungerhero").apply();
            mSharedPreferences.edit().putBoolean("clear",true).apply();
            HungerHero hungerHero = new HungerHero(name,dateOfBirth,email,mobileNumber,"hungerhero",
                    educationalBackground,state,city,locality,pinCode,reasonForJoining,affordableTime,responsibility,
                    currentlyPartOf,introducedToFIThrough,aboutMeList,false,"hungerhero",false);
            mDatabaseReference.setValue(hungerHero);
            mProgressDialog.cancel();
            makeToast("Congo! You are a "+responsibility+" now");
            FirebaseAuth.getInstance().signOut();
            getActivity().finish();
        }

    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){

            case R.id.textView1:
                openBrowser("https://www.google.com");
                break;

            case R.id.textView2:
                openBrowser("https://www.facebook.com");
                break;

            case R.id.textView3:
                openBrowser("https://www.google.com");
                break;

            case R.id.textView4:
                openBrowser("https://www.facebook.com");
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
                break;
        }

    }

    private void openBrowser(String url){

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
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

    private void makeToast(String message){

        Toast.makeText(getActivity(),message,Toast.LENGTH_SHORT).show();
    }

}
