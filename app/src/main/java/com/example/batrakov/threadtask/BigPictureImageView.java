package com.example.batrakov.threadtask;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Created by batrakov on 01.11.17.
 */

public class BigPictureImageView extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.big_picture_layout);

        mImageView = findViewById(R.id.big_image);

        if (getIntent().hasExtra(MainActivity.OPEN_IMAGE)) {
            int position = getIntent().getIntExtra(MainActivity.OPEN_IMAGE, 0);
            mImageView.setImageBitmap(MainActivity.sImageArrayList.get(position).loadImageFromPath());
        }
    }
}
