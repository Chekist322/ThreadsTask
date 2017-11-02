package com.example.batrakov.threadtask;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageView extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        ImageView imageView = findViewById(R.id.big_image);

        if (getIntent().hasExtra(MainActivity.IMAGE_PATH)) {
            String path = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
            imageView.setImageBitmap(BitmapFactory.decodeFile(path));
        }
    }
}
