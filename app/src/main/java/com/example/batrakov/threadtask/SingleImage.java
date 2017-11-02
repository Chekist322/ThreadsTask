package com.example.batrakov.threadtask;

/**
 *Class represents single Image element which consist of image name
 * and path to image in external storage.
 */
class SingleImage {

    private String mName;
    private String mPath;

    /**
     * Constructor.
     */
    SingleImage() {
        mName = "empty";
    }

    /**
     * Get image name.
     *
     * @return image name.
     */
    String getName() {
        return mName;
    }

    /**
     * Set image name.
     *
     * @param aName target name.
     */
    void setName(String aName) {
        mName = aName;
    }

    /**
     * Set path to image.
     *
     * @param aPath target path.
     */
    void setPath(String aPath) {
        mPath = aPath;
    }

    /**
     * Get image path.
     *
     * @return image path in external storage.
     */
    String getPath() {
        return mPath;
    }
}
