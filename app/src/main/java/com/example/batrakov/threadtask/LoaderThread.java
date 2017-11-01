package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Phoen on 01.11.2017.
 */

public class LoaderThread extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<MainActivity.ListHolder, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener mThumbnailDownloadListener;

    public interface ThumbnailDownloadListener {
        void onThumbnailDownloaded(MainActivity.ListHolder target, Bitmap thumbnail);
    }

    LoaderThread(String name, Handler aHandler) {
        super(name);
        mResponseHandler = aHandler;
    }

    void setThumbnailDownloadListener (ThumbnailDownloadListener aListener) {
        mThumbnailDownloadListener = aListener;
    }

    void queueThumbnail(MainActivity.ListHolder aHolder, String aPath) {
        if (aPath == null) {
            mRequestMap.remove(aHolder);
        } else {
            mRequestMap.put(aHolder, aPath);
            mRequestHandler.obtainMessage(MESSAGE_DOWNLOAD, aHolder)
                    .sendToTarget();
        }
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    MainActivity.ListHolder holder = (MainActivity.ListHolder) msg.obj;
                    handleRequest(holder);
                }
            }
        };
    }

    void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    private void handleRequest(final MainActivity.ListHolder aHolder) {
        final String path = mRequestMap.get(aHolder);
        if (path == null) {
            return;
        }
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = 4;
        bitmapOptions.inDensity = 960;
        bitmapOptions.inTargetDensity = 400;
        final Bitmap thumbnail = BitmapFactory.decodeFile(path, bitmapOptions);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!Objects.equals(mRequestMap.get(aHolder), path)) {
                    return;
                }
                mRequestMap.remove(aHolder);
                mThumbnailDownloadListener.onThumbnailDownloaded(aHolder, thumbnail);
            }
        });
    }
}
