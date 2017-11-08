package com.example.batrakov.threadtask;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.batrakov.threadtask.loadImageTask.Task;
import com.example.batrakov.threadtask.loadImageTask.TaskManager;

/**
 * Service rule ThreadManager and provide work between MainActivity to loader threads.
 */
public class ImageTaskService extends Service {

    private static final String TAG = ImageTaskService.class.getSimpleName();
    private final IBinder mBinder = new ImageTaskBinder();

    private static final int AMOUNT_OF_THREADS = 4;

    private final TaskManager mTaskManager = new TaskManager(AMOUNT_OF_THREADS);

    @Nullable
    @Override
    public IBinder onBind(Intent aIntent) {
        Log.i(TAG, "onBind: ");
        return mBinder;
    }

    /**
     * Add task to TaskManager.
     *
     * @param aTask target task.
     */
    public void addTask(Task aTask) {
        mTaskManager.addTask(aTask);
    }

    /**
     * Service binder that allow to communicate with parent Activity.
     */
    class ImageTaskBinder extends Binder {

        /**
         * Get service by binder.
         *
         * @return binder's service.
         */
        ImageTaskService getService() {
            return ImageTaskService.this;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mTaskManager.clear();
    }
}
