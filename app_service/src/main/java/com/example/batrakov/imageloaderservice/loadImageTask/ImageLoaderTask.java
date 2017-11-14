package com.example.batrakov.imageloaderservice.loadImageTask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.RemoteException;

import com.example.batrakov.imageloaderservice.ImageTaskService;
import com.example.batrakov.threadtask.IServiceCallback;

/**
 * Describe task to load image in it base configuration.
 */
public class ImageLoaderTask extends Task {

    private final IServiceCallback mCallback;
    private final String mImageName;

    /**
     * Constructor.
     *
     * @param aImageName path to image from external storage.
     * @param aCallback  callback to parent.
     */
    public ImageLoaderTask(String aImageName, IServiceCallback aCallback) {
        mCallback = aCallback;
        mImageName = aImageName;
    }

    @Override
    public void process() {
        String pathToImage = ImageTaskService.PATH_TO_IMAGES + mImageName;
        Bitmap bitmap = BitmapFactory.decodeFile(pathToImage);
        if (!Thread.currentThread().isInterrupted()) {
            try {
                mCallback.bitmapLoaded(mImageName, bitmap);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
        }
    }
}
