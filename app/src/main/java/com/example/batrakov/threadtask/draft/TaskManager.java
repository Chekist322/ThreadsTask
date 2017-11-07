package com.example.batrakov.threadtask.draft;

import java.util.LinkedList;

/**
 * Created by batrakov on 07.11.17.
 */

public class TaskManager {
    private TaskQueue mTaskQueue;
    private boolean mInWork = true;
    private final LinkedList<Worker> mTasks = new LinkedList<>();

    public TaskManager() {
        for (int i = 0; i < 4; i++) {
            Worker task = new Worker(this);
            mTasks.add(task);
            task.start();
        }
    }

    public Task getTask() {
        synchronized (this) {
            if (mTaskQueue.isEmpty()) {
                while (mInWork) {
                    try {
                        wait();
                    } catch (InterruptedException aE) {
                        aE.printStackTrace();
                    }
                }
            }
        }
        return mTaskQueue.poll();
    }

    public void addTask(Task aTask) {
        synchronized (this) {
            mTaskQueue.add(aTask);
            notifyAll();
        }
    }
}
