package com.example.batrakov.threadtask.loadImageTask;

import android.graphics.BitmapFactory;
import android.os.Message;

/**
 * Describe task to load scaled image from external storage.
 */
public class ThumbnailTask extends Task {

    private final Message mCallback;
    private final String mImagePath;
    private final int mTargetDensity;
    private final int mTargetWidth;

    /**
     * @param aImagePath       path to image.
     * @param aCallbackMessage callback.
     * @param aDensity screen density.
     * @param aImageWidth target image width in pixels.
     */
    public ThumbnailTask(String aImagePath, Message aCallbackMessage, int aDensity, int aImageWidth) {
        mCallback = aCallbackMessage;
        mImagePath = aImagePath;
        mTargetDensity = aDensity;
        mTargetWidth = aImageWidth;
    }

    @Override
    public void process() {
        if (!isCanceled()) {
            BitmapFactory.Options options = new BitmapFactory.Options();

            options.inTargetDensity = mTargetDensity;
            options.inScaled = true;
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(mImagePath, options);
            options.inJustDecodeBounds = false;
            options.inSampleSize = BitmapUtils.calculateInSampleSize(options, mTargetWidth);

            mCallback.obj = BitmapFactory.decodeFile(mImagePath, options);
            if (!isCanceled()) {
                mCallback.sendToTarget();
            }
        }
    }
}
