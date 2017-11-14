package com.example.batrakov.imageloaderservice.loadImageTask;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Manage incoming tasks to load images and execute threads for this tasks.
 */
public class TaskManager implements Serializable {

    private ConcurrentLinkedQueue<Task> mTaskQueue;
    private ConcurrentHashMap<Integer, String> mHolderPathsMap;
    private LinkedList<Worker> mThreadsList;

    /**
     * Constructor.
     *
     * @param aAmountOfThreads required amount of worker threads.
     */
    public TaskManager(int aAmountOfThreads) {
        mTaskQueue = new ConcurrentLinkedQueue<>();
        mHolderPathsMap = new ConcurrentHashMap<>();
        mThreadsList = new LinkedList<>();
        for (int i = 0; i < aAmountOfThreads; i++) {
            Worker worker = new Worker(this);
            mThreadsList.add(worker);
            worker.start();
        }
    }

    /**
     * Get task from queue. Launched thread wait until new task become to queue.
     *
     * @return task from queue.
     *
     * @throws InterruptedException on thread interruption.
     */
    Task getTask() throws InterruptedException {
        synchronized (this) {
            while (mTaskQueue.isEmpty()) {
                wait();
            }
            return mTaskQueue.poll();
        }
    }

    /**
     * Add task to queue.
     *
     * @param aHolderID requested holder id.
     * @param aTask     incoming task.
     */
    public void addTask(int aHolderID, Task aTask) {
        synchronized (this) {
            if (aHolderID >= 0) {
                mHolderPathsMap.put(aHolderID, aTask.getTaskPathToImage());
            }
            mTaskQueue.add(aTask);
            notifyAll();
        }
    }

    /**
     * Check is task still need to be processed.
     *
     * @param aTaskID      task id.
     * @param aPathToImage path to target image.
     *
     * @return true if needed.
     */
    boolean isTaskStillNeeded(int aTaskID, String aPathToImage) {
        synchronized (this) {
            return Objects.equals(aPathToImage, mHolderPathsMap.get(aTaskID));
        }
    }

    /**
     * Clear tasks queue and interrupt worker threads.
     */
    public void clear() {
        mTaskQueue.clear();
        mHolderPathsMap.clear();
        while (!mThreadsList.isEmpty()) {
            mThreadsList.poll().interrupt();
        }
    }
}
