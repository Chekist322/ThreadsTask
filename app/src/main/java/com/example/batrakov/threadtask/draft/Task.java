package com.example.batrakov.threadtask.draft;

/**
 * Created by batrakov on 07.11.17.
 */

public class Task {
    private boolean mIsCanceled;

    public void cancel() {
        mIsCanceled = true;
    }

    public boolean isCanceled() {
        return mIsCanceled;
    }

    public void process() {

    }
}
