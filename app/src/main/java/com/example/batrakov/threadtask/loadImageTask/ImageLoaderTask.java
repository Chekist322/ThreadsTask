package com.example.batrakov.threadtask.loadImageTask;

import android.graphics.BitmapFactory;
import android.os.Message;

/**
 * Describe task to load image in it base configuration.
 */
public class ImageLoaderTask extends Task {

    private final Message mCallback;
    private final String mImagePath;

    /**
     * Constructor.
     *
     * @param aImagePath       path to image from external storage.
     * @param aCallbackMessage callback to parent.
     */
    public ImageLoaderTask(String aImagePath, Message aCallbackMessage) {
        mCallback = aCallbackMessage;
        mImagePath = aImagePath;
    }

    @Override
    public void process() {
        mCallback.obj = BitmapFactory.decodeFile(mImagePath);
        if (!isCanceled()) {
            System.out.println("kek");
            mCallback.sendToTarget();
        }
    }
}
