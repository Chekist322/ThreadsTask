package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 *
 * Created by batrakov on 01.11.17.
 */

class SingleImage {

    private String mName;
    private String mPath;
    private Bitmap mThumbnail;

    SingleImage() {
        mName = "empty";
        mThumbnail = null;
    }

    String getName() {
        return mName;
    }

    void setName(String aName) {
        mName = aName;
    }

    void buildThumbnail() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = 4;
        bitmapOptions.inDensity = 960;
        bitmapOptions.inTargetDensity = 400;
        mThumbnail = BitmapFactory.decodeFile(mPath, bitmapOptions);
    }

    Bitmap getThumbnail() {
        return mThumbnail;
    }

    void setPath(String aPath) {
        mPath = aPath;
    }

    Bitmap loadImageFromPath() {
        return BitmapFactory.decodeFile(mPath);
    }
}
