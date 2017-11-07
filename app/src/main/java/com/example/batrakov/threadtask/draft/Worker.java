package com.example.batrakov.threadtask.draft;

/**
 * Worker thread for loading image.
 */
public class Worker extends Thread {
    private final TaskManager mTaskManager;

    /**
     * Constructor.
     *
     * @param aTaskManager task for thread manager.
     */
    Worker(TaskManager aTaskManager) {
        mTaskManager = aTaskManager;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            Task task = mTaskManager.getTask();
            if (!isInterrupted() && !task.isCanceled()) {
                task.process();
            }
        }
    }
}
