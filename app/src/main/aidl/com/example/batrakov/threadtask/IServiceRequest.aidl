package com.example.batrakov.threadtask;

import com.example.batrakov.threadtask.IServiceCallback;

/**
*   Allow to send requests to bound service.
*/
interface IServiceRequest {

    /**
    *   Add new task to load thumbnail to queue.
    *
    *   @param aName path to target image.
    *   @param aCallback allow to get result from service.
    *   @param aDensity target thumbnail density.
    *   @param aWidth target thumbnail width.
    *   @param aHolderID request holder id.
    */
    oneway void addThumbnailTask(int aHolderID, in String aName,in IServiceCallback aCallback, int aDensity, int aWidth);

    /**
    *   Add new task to load image to queue.
    *
    *   @param aName path to target image.
    *   @param aCallback allow to get result from service.
    */
    oneway void addBigTask(in String aName,in IServiceCallback aCallback);

    /**
    *   Add new task to load list of images to queue.
    *
    *   @param aCallback allow to get result from service.
    */
    oneway void addListTask(in IServiceCallback aCallback);
}
