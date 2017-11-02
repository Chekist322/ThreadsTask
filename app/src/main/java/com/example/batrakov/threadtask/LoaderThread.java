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
 * Loader thread that allow to load and scale image from external storage
 * to MainActivity RecyclerView. Add incoming request messages for loading
 * in queue. Consist of interface to communicate with MainActivity.
 */
public class LoaderThread extends HandlerThread {

    private static final int MESSAGE_DOWNLOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<MainActivity.ListHolder, String> mRequestMap = new ConcurrentHashMap<>();
    private ThumbnailDownloadListener mThumbnailDownloadListener;

    private static final int SCALE_MULTIPLIER = 4;
    private static final int SRC_DENSITY = 960;
    private static final int TARGET_DENSITY = 400;

    /**
     * Interface to communicate with current MainActivity.
     */
    public interface ThumbnailDownloadListener {

        /**
         * Allow to get info image was successfully loaded.
         *
         * @param aHolder target ListHolder.
         * @param aThumbnail result Bitmap.
         */
        void onThumbnailDownloaded(MainActivity.ListHolder aHolder, Bitmap aThumbnail);
    }

    /**
     * Constructor.
     *
     * @param aName thread name.
     * @param aHandler response handler from MainActivity.
     */
    LoaderThread(String aName, Handler aHandler) {
        super(aName);
        mResponseHandler = aHandler;
    }

    /**
     * Set download listener for alive MainActivity.
     *
     * @param aListener current listener.
     */
    void setThumbnailDownloadListener(ThumbnailDownloadListener aListener) {
        mThumbnailDownloadListener = aListener;
    }

    /**
     * Add new request to message queue.
     *
     * @param aHolder target ListHolder.
     * @param aPath path to target image.
     */
    void queueThumbnail(MainActivity.ListHolder aHolder, String aPath) {
        System.out.println(aHolder.getAdapterPosition());
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
            public void handleMessage(Message aMsg) {
                if (aMsg.what == MESSAGE_DOWNLOAD) {
                    MainActivity.ListHolder holder = (MainActivity.ListHolder) aMsg.obj;
                    handleRequest(holder);
                }
            }
        };
    }

    /**
     * Clear message queue if MainActivity was destroyed.
     */
    void clearQueue() {
        mRequestHandler.removeMessages(MESSAGE_DOWNLOAD);
    }

    /**
     * Do asynchronous image loading and send it to MainActivity.
     *
     * @param aHolder target ListHolder.
     */
    private void handleRequest(final MainActivity.ListHolder aHolder) {
        final String path = mRequestMap.get(aHolder);
        if (path == null) {
            return;
        }
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inScaled = true;
        bitmapOptions.inSampleSize = SCALE_MULTIPLIER;
        bitmapOptions.inDensity = SRC_DENSITY;
        bitmapOptions.inTargetDensity = TARGET_DENSITY;
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
