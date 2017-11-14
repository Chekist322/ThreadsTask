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
    private final int mTargetImageWidth;
    private final Integer mHolderID;
    private final TaskManager mTaskManager;

    /**
     * Constructor.
     *
     * @param aHolderID         target holder id.
     * @param aImageName        image name.
     * @param aCallbackMessage  callback.
     * @param aDensity          screen density.
     * @param aTargetImageWidth target image width in pixels.
     */
    public ThumbnailTask(Integer aHolderID, String aImageName, IServiceCallback aCallbackMessage, int aDensity, int aTargetImageWidth, TaskManager aTaskManager) {
        mHolderID = aHolderID;
        mCallback = aCallbackMessage;
        mImageName = aImageName;
        mTargetDensity = aDensity;
        mTargetImageWidth = aTargetImageWidth;
        mTaskManager = aTaskManager;
    }

    @Override
    public void process() {
        BitmapFactory.Options options = new BitmapFactory.Options();

        String pathToImage = ImageTaskService.PATH_TO_IMAGES + mImageName;

        options.inTargetDensity = mTargetDensity;
        options.inScaled = true;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathToImage, options);

        options.inJustDecodeBounds = false;
        options.inSampleSize = BitmapUtils.calculateInSampleSize(options, mTargetImageWidth);

        if (mTaskManager.isTaskStillNeeded(mHolderID, pathToImage)) {

            Bitmap thumbnail = BitmapFactory.decodeFile(pathToImage, options);

            if (!Thread.currentThread().isInterrupted() && mTaskManager.isTaskStillNeeded(mHolderID, pathToImage)) {
                try {
                    mCallback.bitmapLoaded(mImageName, thumbnail);
                } catch (RemoteException aE) {
                    aE.printStackTrace();
                }
            }
        }
    }

    @Override
    public String getTaskPathToImage() {
        return ImageTaskService.PATH_TO_IMAGES + mImageName;
    }
}
