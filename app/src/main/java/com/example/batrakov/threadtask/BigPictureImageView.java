package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageView extends AppCompatActivity {

    ImageView mImageView;

    @Override
    protected void onCreate(@Nullable Bundle aSavedInstanceState) {
        super.onCreate(aSavedInstanceState);
        setContentView(R.layout.big_picture_layout);

        mImageView = findViewById(R.id.big_image);

        if (getIntent().hasExtra(MainActivity.IMAGE_PATH)) {
            String path = getIntent().getStringExtra(MainActivity.IMAGE_PATH);
            LoadImage loadImage = new LoadImage(this);
            loadImage.execute(path);
        }
    }

    static class LoadImage extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<BigPictureImageView> mReference;

        LoadImage(BigPictureImageView aView) {
            mReference = new WeakReference<>(aView);
        }

        @Override
        protected Bitmap doInBackground(String... aStrings) {
            return BitmapFactory.decodeFile(aStrings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap aBitmap) {
            mReference.get().mImageView.setImageBitmap(aBitmap);
        }
    }
}
