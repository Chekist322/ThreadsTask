package com.example.batrakov.threadtask;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import java.util.Objects;
import java.util.concurrent.ConcurrentMap;

/**
 * Loader thread that allow to load and scale image from external storage
 * to MainActivity RecyclerView. Add incoming request messages for loading
 * in queue. Consist of interface to communicate with MainActivity.
 */
public class LoaderThread extends HandlerThread {

    private static final int MESSAGE_LOAD = 0;
    private Handler mRequestHandler;
    private Handler mResponseHandler;
    private ConcurrentMap<MainActivity.ListHolder, String> mRequestMap;
    private ThumbnailLoadListener mThumbnailLoadListener;

    private static final int SCALE_MULTIPLIER = 12;

    /**
     * Interface to communicate with current MainActivity.
     */
    public interface ThumbnailLoadListener {

        /**
         * Allow to get info image was successfully loaded.
         *
         * @param aHolder    target ListHolder.
         * @param aThumbnail result Bitmap.
         */
        void onThumbnailLoaded(MainActivity.ListHolder aHolder, Bitmap aThumbnail);
    }

    /**
     * Constructor.
     *
     * @param aName    thread name.
     * @param aHandler response handler from MainActivity.
     */
    LoaderThread(String aName, Handler aHandler) {
        super(aName);
        mResponseHandler = aHandler;
    }

    /**
     * Set load listener for alive MainActivity.
     *
     * @param aListener current listener.
     */
    void setThumbnailLoadListener(ThumbnailLoadListener aListener) {
        mThumbnailLoadListener = aListener;
    }

    /**
     * Add new request to message queue.
     *
     * @param aHolder target ListHolder.
     * @param aPath   path to target image.
     * @param aMap    current map of holders.
     */
    void queueThumbnail(MainActivity.ListHolder aHolder, String aPath,
                        ConcurrentMap<MainActivity.ListHolder, String> aMap) {
        mRequestMap = aMap;
        if (null == aHolder) {
            return;
        }

        if (null == aPath || aPath.isEmpty()) {
            mRequestMap.remove(aHolder);
            return;
        }

        String path = mRequestMap.get(aHolder);
        if (null != path) {
            if (path.equals(aPath)) {
                return;
            } else {
                mRequestMap.remove(aHolder);
            }
        }

        mRequestMap.put(aHolder, aPath);
        mRequestHandler.obtainMessage(MESSAGE_LOAD, aHolder)
                .sendToTarget();
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler(getLooper()) {
            @Override
            public void handleMessage(Message aMsg) {
                if (MESSAGE_LOAD == aMsg.what) {
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
        mRequestHandler.removeMessages(MESSAGE_LOAD);
    }

    /**
     * Do asynchronous image loading and send it to MainActivity.
     *
     * @param aHolder target ListHolder.
     */
    private void handleRequest(final MainActivity.ListHolder aHolder) {
        final String path = mRequestMap.get(aHolder);
        if (null == path) {
            return;
        }
        BitmapFactory.Options bitmapOptions = new BitmapFactory.Options();
        bitmapOptions.inSampleSize = SCALE_MULTIPLIER;
        final Bitmap thumbnail = BitmapFactory.decodeFile(path, bitmapOptions);
        mResponseHandler.post(new Runnable() {
            @Override
            public void run() {
                if (!Objects.equals(mRequestMap.get(aHolder), path)) {
                    return;
                }
                mRequestMap.remove(aHolder);
                mThumbnailLoadListener.onThumbnailLoaded(aHolder, thumbnail);
            }
        });
    }
}
