package com.example.expensehelper;

import android.graphics.Bitmap;

public final class CompressBitmapUtil {


    public static Bitmap getResizedBitmap(Bitmap image, int shortSide) {

        if (image.getWidth() <= image.getHeight()) {

            if (image.getWidth() > shortSide) {

                int width = image.getWidth();
                int height = image.getHeight();

                float bitmapRatio = (float) width / (float) height;

                width = shortSide;
                height = (int) (width / bitmapRatio);

                return Bitmap.createScaledBitmap(image, width, height, true);
            } else {
                return image;
            }

        }  else {

            if (image.getHeight() > shortSide) {

                int width = image.getWidth();
                int height = image.getHeight();

                float bitmapRatio = (float) width / (float) height;

                height = shortSide;
                width = (int) (height * bitmapRatio);

                return Bitmap.createScaledBitmap(image, width, height, true);


            } else {
                return image;
            }
        }

    }
}