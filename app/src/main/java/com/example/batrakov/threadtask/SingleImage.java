package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by batrakov on 01.11.17.
 */

public class SingleImage {

    private String mName;
    private String mPath;
    private Bitmap mThumbnail;

    SingleImage() {
        mName = "empty";
        mThumbnail = null;
    }

    SingleImage(String aName, String aPath) {
        mName = aName;
        mPath = aPath;
        mThumbnail = null;
        buildThumbnail();
    }

    String getName() {
        return mName;
    }

    public void setName(String aName) {
        mName = aName;
    }

    public void buildThumbnail() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = 4;
        bitmapOptions.inDensity = 960;
        bitmapOptions.inTargetDensity = 100;
        mThumbnail = BitmapFactory.decodeFile(mPath, bitmapOptions);
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }


    public String getPath() {
        return mPath;
    }

    public void setPath(String aPath) {
        mPath = aPath;
    }

    public Bitmap loadImageFromPath() {
        return BitmapFactory.decodeFile(mPath);
    }
}
