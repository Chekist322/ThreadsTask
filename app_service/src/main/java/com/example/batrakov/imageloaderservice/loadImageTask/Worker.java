package com.example.batrakov.imageloaderservice.loadImageTask;

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
            try {
                Task task = mTaskManager.getTask();
                if (task != null && !isInterrupted()) {
                    task.process();
                }
            } catch (InterruptedException aE) {
                Thread.currentThread().interrupt();
                aE.printStackTrace();
            }
        }
    }
}
