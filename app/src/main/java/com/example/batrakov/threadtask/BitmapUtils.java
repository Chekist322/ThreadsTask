package com.example.batrakov.threadtask;

import android.graphics.BitmapFactory;

/**
 * Utility class for calculations bitmaps parameters.
 */
class BitmapUtils {
    /**
     * Calculate scale multiplier for thumbnail scaling.
     *
     * @param aOptions image options.
     * @param aReqWidth target width.
     * @param aReqHeight target height.
     * @return calculated multiplier.
     */
    static int calculateInSampleSize(BitmapFactory.Options aOptions, int aReqWidth, int aReqHeight) {
        final int height = aOptions.outHeight;
        final int width = aOptions.outWidth;
        int inSampleSize = 1;

        if (height > aReqHeight || width > aReqWidth) {
            while ((height / inSampleSize) >= aReqHeight
                    && (width / inSampleSize) >= aReqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
