package com.example.batrakov.imageloaderservice.loadImageTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;

import com.example.batrakov.imageloaderservice.ImageTaskService;
import com.example.batrakov.threadtask.IServiceCallback;

/**
 * Describe task to load scaled image from external storage.
 */
public class ThumbnailTask extends Task {

    private final IServiceCallback mCallback;
    private final String mImageName;
    private final int mTargetDensity;
    private final int mTargetWidth;

    /**
     * @param aImageName       image name.
     * @param aCallbackMessage callback.
     * @param aDensity         screen density.
     * @param aImageWidth      target image width in pixels.
     */
    public ThumbnailTask(String aImageName, IServiceCallback aCallbackMessage, int aDensity, int aImageWidth) {
        mCallback = aCallbackMessage;
        mImageName = aImageName;
        mTargetDensity = aDensity;
        mTargetWidth = aImageWidth;
    }

    @Override
    public void process() {
        BitmapFactory.Options options = new BitmapFactory.Options();

        String path = ImageTaskService.PATH_TO_IMAGES + mImageName;

                options.inTargetDensity = mTargetDensity;
        options.inScaled = true;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);
        options.inJustDecodeBounds = false;
        options.inSampleSize = BitmapUtils.calculateInSampleSize(options, mTargetWidth);
        Bitmap thumbnail = BitmapFactory.decodeFile(path, options);
        if (!Thread.currentThread().isInterrupted()) {
            try {
                mCallback.bitmapLoaded(mImageName, thumbnail);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
        }
    }
}
