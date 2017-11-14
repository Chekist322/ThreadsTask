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

    private static final String TAG = ImageTaskService.class.getSimpleName();

    private static final int AMOUNT_OF_THREADS = 4;

    private final TaskManager mTaskManager = new TaskManager(AMOUNT_OF_THREADS);

    /**
     * Path to images on external storage.
     */
    public static final String PATH_TO_IMAGES = "/storage/emulated/0/images/";

    private final IServiceRequest.Stub mCallbackInterface = new IServiceRequest.Stub() {


        @Override
        public void addThumbnailTask(String aName, IServiceCallback aCallback,
                                     int aDensity, int aWidth) throws RemoteException {
            mTaskManager.addTask(new ThumbnailTask(aName, aCallback, aDensity, aWidth));
        }

        @Override
        public void addBigTask(String aPath, IServiceCallback aCallback) throws RemoteException {
            mTaskManager.addTask(new ImageLoaderTask(aPath, aCallback));
        }

        @Override
        public void addListTask(IServiceCallback aCallback) throws RemoteException {
            mTaskManager.addTask(new TaskGetFileList(aCallback));
        }
    };

    @Override
    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        return super.onStartCommand(aIntent, aFlags, aStartId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent aIntent) {
        Log.i(TAG, "onBind: ");
        return mCallbackInterface;
    }

    @Override
    public void onTaskRemoved(Intent aRootIntent) {
        Log.i(TAG, "onTaskRemoved: ");
        super.onTaskRemoved(aRootIntent);
    }

    @Override
    public boolean onUnbind(Intent aIntent) {
        Log.i(TAG, "onUnbind: ");
        return super.onUnbind(aIntent);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
        mTaskManager.clear();
    }
}
