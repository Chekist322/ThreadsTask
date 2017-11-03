package com.example.batrakov.threadtask;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by batrakov on 02.11.17.
 */

public class CustomThreadManager {

    private ArrayList<LoaderThread> mThreads;

    private ConcurrentMap<MainActivity.ListHolder, String> mRequestMap = new ConcurrentHashMap<>();

    private int mAmountOfThreads;

    private int mTicker;

    CustomThreadManager (ArrayList<LoaderThread> aThreads, int aAmountOfThreads) {
        mTicker = 0;
        mAmountOfThreads = aAmountOfThreads;
        mThreads = aThreads;
        for (int i = 0; i < mAmountOfThreads; i++) {
            mThreads.get(i).start();
        }
    }

    void sendRequestMessage(MainActivity.ListHolder aHolder, String aPath) {
        if (mTicker == mAmountOfThreads) {
            mTicker = 0;
        }
        mThreads.get(mTicker % mAmountOfThreads).queueThumbnail(aHolder, aPath, mRequestMap);
        mTicker++;
    }

    void clearPool() {
        for (int i = 0; i < mAmountOfThreads; i++) {
            mThreads.get(i).clearQueue();
            mThreads.get(i).quit();
        }
    }
}
