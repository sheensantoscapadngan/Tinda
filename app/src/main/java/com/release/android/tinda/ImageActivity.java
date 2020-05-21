package com.release.android.tinda;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class ImageActivity extends AppCompatActivity {

    private ImageView image;
    private String imageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
        setupViews();


    }

    private void setupViews() {
        image = (ImageView) findViewById(R.id.imageViewImage);

        imageUri = getIntent().getStringExtra("imageUri");
        Glide.with(this).load(imageUri).into(image);

    }
}
