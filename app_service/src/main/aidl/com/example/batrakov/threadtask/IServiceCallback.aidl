// ServiceAidlInteface.aidl
package com.example.batrakov.threadtask;

// Declare any non-default types here with import statements

/**
*   Provide callback from Service to main app.
*/
interface IServiceCallback {

    /**
    *   Send message bitmap was loaded.
    *
    *   @param aPath contain path to loaded image.
    *   @param aName loaded image.
    */
    oneway void bitmapLoaded(in String aPath, in Bitmap aBitmap);

    /**
    *   Send message Lists were leaded.
    *
    *   @param aNameList contains images's names.
    */
    oneway void listsLoaded(in List<String> aNameList);
}
