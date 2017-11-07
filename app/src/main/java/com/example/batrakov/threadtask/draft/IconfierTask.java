package com.example.batrakov.threadtask.draft;

import android.graphics.BitmapFactory;
import android.os.Message;

import java.io.File;

/**
 * Created by batrakov on 07.11.17.
 */

public class IconfierTask extends Task {

    private final Message mCallback;
    private final BitmapFactory.Options mBitmapOptions;
    private final String mImagePath;

    public IconfierTask(File aImageFile, BitmapFactory.Options aBitmapOptions, Message aCallbackMessage) {
        mCallback = aCallbackMessage;
        mBitmapOptions = aBitmapOptions;
        mImagePath = aImageFile.getPath();
    }

    @Override
    public void process() {
        mCallback.obj = BitmapFactory.decodeFile(mImagePath, mBitmapOptions);
        if (!isCanceled()) {
            mCallback.sendToTarget();
        }
    }
}
