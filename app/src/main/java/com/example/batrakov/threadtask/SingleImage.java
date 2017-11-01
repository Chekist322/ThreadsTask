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

    SingleImage() {
        mName = "empty";
    }

    String getName() {
        return mName;
    }

    void setName(String aName) {
        mName = aName;
    }

    Bitmap buildThumbnail() {
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = 4;
        bitmapOptions.inDensity = 960;
        bitmapOptions.inTargetDensity = 400;
        return BitmapFactory.decodeFile(mPath, bitmapOptions);
    }

    void setPath(String aPath) {
        mPath = aPath;
    }

    public String getPath() {
        return mPath;
    }

    Bitmap loadImageFromPath() {
        return BitmapFactory.decodeFile(mPath);
    }
}
