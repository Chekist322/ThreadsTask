package com.example.batrakov.threadtask;

import java.util.ArrayList;

/**
 * Created by batrakov on 02.11.17.
 */

public class CustomThreadManager {

    ArrayList<LoaderThread> mThreads;

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
        mThreads.get(mTicker % mAmountOfThreads).queueThumbnail(aHolder, aPath);
        mTicker++;
    }

    void clearPool() {
        for (int i = 0; i < mAmountOfThreads; i++) {
            mThreads.get(i).clearQueue();
            mThreads.get(i).quit();
        }
    }
}
