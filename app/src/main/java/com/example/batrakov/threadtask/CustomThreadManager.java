package com.example.batrakov.threadtask;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Thread manager class that consist of current Map of requests and ArrayList of working threads.
 */
class CustomThreadManager {

    private ArrayList<LoaderThread> mThreads;

    private ConcurrentMap<MainActivity.ListHolder, String> mRequestMap = new ConcurrentHashMap<>();

    private int mAmountOfThreads;

    private int mTicker;

    /**
     * Constructor.
     *
     * @param aThreads thread for loading images
     * @param aAmountOfThreads chosen amount of threads
     */
    CustomThreadManager(ArrayList<LoaderThread> aThreads, int aAmountOfThreads) {
        mTicker = 0;
        mAmountOfThreads = aAmountOfThreads;
        mThreads = aThreads;
        for (int i = 0; i < mAmountOfThreads; i++) {
            mThreads.get(i).start();
        }
    }

    /**
     * Send request to thread for loading image.
     *
     * @param aHolder target holder from MainActivity.
     * @param aPath path to target image.
     */
    void sendRequestMessage(MainActivity.ListHolder aHolder, String aPath) {
        if (mTicker == mAmountOfThreads) {
            mTicker = 0;
        }
        mThreads.get(mTicker % mAmountOfThreads).queueThumbnail(aHolder, aPath, mRequestMap);
        mTicker++;
    }

    /**
     * Clear and stop threads message queues.
     */
    void clearPool() {
        for (int i = 0; i < mAmountOfThreads; i++) {
            mThreads.get(i).clearQueue();
            mThreads.get(i).quit();
        }
    }
}
