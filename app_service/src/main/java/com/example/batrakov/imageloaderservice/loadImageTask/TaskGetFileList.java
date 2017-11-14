package com.example.batrakov.imageloaderservice.loadImageTask;

import android.os.Environment;
import android.os.RemoteException;

import com.example.batrakov.threadtask.IServiceCallback;

import java.io.File;
import java.util.ArrayList;

/**
 * Task for getting list of files name from service.
 */

public class TaskGetFileList extends Task {

    private IServiceCallback mCallback;

    /**
     * Constructor.
     *
     * @param aCallback callback to main app.
     */
    public TaskGetFileList(IServiceCallback aCallback) {
        mCallback = aCallback;
    }

    @Override
    public void process() {
        File sImagesDirectory = new File(Environment.getExternalStorageDirectory() + "/images");
        System.out.println(sImagesDirectory);
        ArrayList<String> filesNameList = new ArrayList<>();

        if (sImagesDirectory.listFiles() != null) {
            File[] listFiles = sImagesDirectory.listFiles();
            for (File file : listFiles) {
                filesNameList.add(file.getName());
            }

            if (!Thread.currentThread().isInterrupted()) {
                try {
                    mCallback.listsLoaded(filesNameList);
                } catch (RemoteException aE) {
                    aE.printStackTrace();
                }
            }
        } else {
            try {
                mCallback.listsLoaded(null);
            } catch (RemoteException aE) {
                aE.printStackTrace();
            }
        }
    }
}
