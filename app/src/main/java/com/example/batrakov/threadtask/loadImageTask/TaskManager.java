package com.example.batrakov.threadtask.loadImageTask;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manage incoming tasks to load images and execute threads for this tasks.
 */
public class TaskManager implements Serializable {
    private ConcurrentLinkedQueue<Task> mTaskQueue;
    private LinkedList<Worker> mThreadsList;

    /**
     * Constructor.
     *
     * @param aAmountOfThreads required amount of worker threads.
     */
    public TaskManager(int aAmountOfThreads) {
        mTaskQueue = new ConcurrentLinkedQueue<>();
        mThreadsList = new LinkedList<>();
        for (int i = 0; i < aAmountOfThreads; i++) {
            Worker task = new Worker(this);
            mThreadsList.add(task);
            task.start();
        }
    }

    /**
     * Get task from queue.
     *
     * @return task from queue.
     */
    Task getTask() {
        synchronized (this) {
            while (mTaskQueue.isEmpty()) {
                try {
                    wait();
                } catch (InterruptedException aE) {
                    Thread.currentThread().interrupt();
                    aE.printStackTrace();
                    if (Thread.currentThread().isInterrupted()) {
                        return null;
                    }
                }
            }
            return mTaskQueue.poll();
        }
    }

    /**
     * Add task to queue.
     *
     * @param aTask incoming task.
     */
    public void addTask(Task aTask) {
        synchronized (this) {
            mTaskQueue.add(aTask);
            notifyAll();
        }
    }

    /**
     * Clear tasks queue and interrupt worker threads.
     */
    public void clear() {
        mTaskQueue.clear();
        while (!mThreadsList.isEmpty()) {
            mThreadsList.poll().interrupt();
        }
    }
}
