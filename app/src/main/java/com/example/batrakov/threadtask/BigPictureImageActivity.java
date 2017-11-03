package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

/**
 * Represent requested image in full size.
 */
public class BigPictureImageActivity extends AppCompatActivity {

    private ImageView mImageView;

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

    /**
     * AsyncTask to load image.
     */
    static class LoadImage extends AsyncTask<String, Void, Bitmap> {

        private WeakReference<BigPictureImageActivity> mReference;

        /**
         * Constructor.
         *
         * @param aActivity target activity.
         */
        LoadImage(BigPictureImageActivity aActivity) {
            mReference = new WeakReference<>(aActivity);
        }

        @Override
        protected Bitmap doInBackground(String... aStrings) {
            return BitmapFactory.decodeFile(aStrings[0]);
        }

        @Override
        protected void onPostExecute(Bitmap aBitmap) {
            if (null != mReference.get()) {
                mReference.get().mImageView.setImageBitmap(aBitmap);
            }
        }
    }
}
