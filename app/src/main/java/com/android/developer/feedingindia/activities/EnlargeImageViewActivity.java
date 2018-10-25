package com.android.developer.feedingindia.activities;

import android.content.res.Configuration;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.developer.feedingindia.R;
import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;


public class EnlargeImageViewActivity extends AppCompatActivity {
    private String mImageUrl;
    private String mType;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enlarge_image_view);
        PhotoView photoView = (PhotoView) findViewById(R.id.photo_view);
        photoView.setImageResource(R.drawable.no_image);
        Bundle bundle = getIntent().getExtras();
        mImageUrl = bundle.getString("ImageUrl", "");
        mType = bundle.getString("type","");
        if(mImageUrl != null){
            Glide.with(this)
                    .load(mImageUrl)
                    .into(photoView);
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
}