package com.example.batrakov.threadtask.loadImageTask;

import android.graphics.BitmapFactory;
import android.os.Message;

/**
 * Describe task to load scaled image from external storage.
 */
public class ThumbnailTask extends Task {

    private final Message mCallback;
    private final BitmapFactory.Options mBitmapOptions;
    private final String mImagePath;

    /**
     * @param aImagePath       path to image.
     * @param aBitmapOptions   options for scaling.
     * @param aCallbackMessage callback.
     */
    public ThumbnailTask(String aImagePath, BitmapFactory.Options aBitmapOptions, Message aCallbackMessage) {
        mCallback = aCallbackMessage;
        mBitmapOptions = aBitmapOptions;
        mImagePath = aImagePath;
    }

    @Override
    public void process() {
        if (!isCanceled()) {
            mCallback.obj = BitmapFactory.decodeFile(mImagePath, mBitmapOptions);
            if (!isCanceled()) {
                mCallback.sendToTarget();
            }
        }
    }
}
