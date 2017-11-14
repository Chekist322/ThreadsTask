package com.example.batrakov.imageloaderservice.loadImageTask;

/**
 * Describe task to load image.
 */
public abstract class Task {

    /**
     * Start task process.
     */
    public void process() {
    }

    /**
     * Get path to image that need to be loaded in task.
     *
     * @return path to image.
     */
    public String getTaskPathToImage() {
        return "not specified";
    }
}
