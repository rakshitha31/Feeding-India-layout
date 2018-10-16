package com.android.developer.feedingindia.activities;

import android.content.res.Configuration;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.android.developer.feedingindia.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText emailEditText;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        emailEditText = findViewById(R.id.emailEditText);
    }

    public void onClickResetPassword(View view){

        String email = emailEditText.getText().toString().trim();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        if(email.isEmpty())

            makeToast("Please enter your email");

        else {

            mAuth.sendPasswordResetEmail(email).addOnSuccessListener(new OnSuccessListener<Void>() {

                @Override
                public void onSuccess(Void aVoid) {

                    makeToast("Mail Sent!You can reset your password now");
                    finish();

                }

            }).addOnFailureListener(new OnFailureListener() {

                @Override
                public void onFailure(@NonNull Exception e) {

                    makeToast(e.getMessage());

                }

            });
        }

    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    public void makeToast(String message){

        Toast.makeText(ResetPasswordActivity.this,message,Toast.LENGTH_SHORT).show();

    }

}