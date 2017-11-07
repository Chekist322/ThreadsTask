package com.example.batrakov.threadtask.draft;

/**
 * Created by batrakov on 07.11.17.
 */

public class Worker extends Thread {
    private final TaskManager mTaskManager;

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
