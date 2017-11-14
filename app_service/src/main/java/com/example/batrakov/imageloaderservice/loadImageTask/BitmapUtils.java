package com.example.batrakov.imageloaderservice.loadImageTask;

import android.graphics.BitmapFactory;

/**
 * Utility class for calculations bitmaps parameters.
 */
abstract class BitmapUtils {
    /**
     * Calculate scale multiplier for thumbnail scaling.
     *
     * @param aOptions image options.
     * @param aReqWidth target width.
     * @return calculated multiplier.
     */
    static int calculateInSampleSize(BitmapFactory.Options aOptions, int aReqWidth) {
        final int width = aOptions.outWidth;
        int inSampleSize = 1;
        int halfWidth = width / 2;
        if (halfWidth > aReqWidth) {
            while ((halfWidth / inSampleSize) >= aReqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
