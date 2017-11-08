package com.example.batrakov.threadtask.loadImageTask;

/**
 * Describe task to load image.
 */
public class Task {

    private boolean mIsCanceled;

    /**
     * Cancel task.
     */
    public void cancel() {
        mIsCanceled = true;
    }

    /**
     * Check if task is canceled.
     *
     * @return cancel state.
     */
    boolean isCanceled() {
        return mIsCanceled;
    }

    /**
     * Start task process.
     */
    public void process() {
    }
}
