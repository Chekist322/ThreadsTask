package com.example.batrakov.imageloaderservice;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.batrakov.imageloaderservice.loadImageTask.ImageLoaderTask;
import com.example.batrakov.imageloaderservice.loadImageTask.TaskGetFileList;
import com.example.batrakov.imageloaderservice.loadImageTask.TaskManager;
import com.example.batrakov.imageloaderservice.loadImageTask.ThumbnailTask;

import com.example.batrakov.threadtask.IServiceCallback;
import com.example.batrakov.threadtask.IServiceRequest;

/**
 * Service rule ThreadManager and provide work between MainActivity to loader threads.
 */
public class ImageTaskService extends Service {

    /**
     * Path to images on external storage.
     */
    public static final String PATH_TO_IMAGES = "/storage/emulated/0/images/";

    /**
     * Id for request to load big image.
     */
    public static final int BIG_TASK_ID = -1;

    /**
     * Id for request to load list of images.
     */
    public static final int LIST_LOADING_TASK_ID = -2;


    private static final String TAG = ImageTaskService.class.getSimpleName();

    private static final int AMOUNT_OF_THREADS = 4;

    private final TaskManager mTaskManager = new TaskManager(AMOUNT_OF_THREADS);

    private final IServiceRequest.Stub mCallbackInterface = new IServiceRequest.Stub() {


        @Override
        public void addThumbnailTask(int aHolderID, String aName, IServiceCallback aCallback,
                                     int aDensity, int aWidth) throws RemoteException {
            mTaskManager.addTask(aHolderID, new ThumbnailTask(aHolderID,
                    aName, aCallback, aDensity, aWidth, mTaskManager));
        }

        @Override
        public void addBigTask(String aPath, IServiceCallback aCallback) throws RemoteException {
            mTaskManager.addTask(BIG_TASK_ID, new ImageLoaderTask(aPath, aCallback));
        }

        @Override
        public void addListTask(IServiceCallback aCallback) throws RemoteException {
            mTaskManager.addTask(LIST_LOADING_TASK_ID, new TaskGetFileList(aCallback));
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent aIntent) {
        Log.i(TAG, "onBind: ");
        return mCallbackInterface;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        mTaskManager.clear();
        super.onDestroy();
    }
}
