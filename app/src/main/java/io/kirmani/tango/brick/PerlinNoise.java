/*
 * PerlinNoise.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.Log;

import java.util.Random;

public class PerlinNoise {
    private static final String TAG = "PerlinNoise";

    private static final int BITMAP_SIZE = 256;

    public static Bitmap generatePerlinNoiseTexture() {
        int divisions = 16;
        Bitmap bitmap = generateRandomNoiseTexture(divisions);
        Canvas canvas = new Canvas(bitmap);
        divisions /= 2;
        int weight = 2;

        final Paint rectPaint = new Paint();
        rectPaint.setStyle(Style.FILL_AND_STROKE);
        rectPaint.setStrokeWidth(10);
        while (divisions > 0) {
            Bitmap finerNoise = generateRandomNoiseTexture(divisions);
            for (int i = 0; i < BITMAP_SIZE; i++) {
                for (int j = 0; j < BITMAP_SIZE; j++) {
                    int finerColor = Color.red(finerNoise.getPixel(i, j));
                    int currentColor = Color.red(bitmap.getPixel(i, j));
                    int newColor = currentColor + finerColor / weight;
                    // Log.d(TAG, String.format("Finer color: %s", finerColor));
                    // Log.d(TAG, String.format("Current color: %s", currentColor));
                    // Log.d(TAG, String.format("New color: %s", newColor));
                    rectPaint.setARGB(255, newColor, newColor, newColor);
                    canvas.drawRect(i, j, i + 1, j + 1, rectPaint);
                }
            }

            weight *= 2;
            divisions /= 2;
        }

        return bitmap;
    }

    private static Bitmap generateRandomNoiseTexture(int divisions) {
        Bitmap bitmap = Bitmap.createBitmap(BITMAP_SIZE, BITMAP_SIZE, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);

        Random random = new Random();
        final Paint rectPaint = new Paint();
        rectPaint.setStyle(Style.FILL_AND_STROKE);
        rectPaint.setStrokeWidth(10);
        for (int i = 0; i < BITMAP_SIZE; i += divisions) {
            for (int j = 0; j < BITMAP_SIZE; j += divisions) {
                int color = random.nextInt(256);
                rectPaint.setARGB(255, color, color, color);
                canvas.drawRect(i, j, i + divisions, j + divisions, rectPaint);
            }
        }
        return bitmap;
    }
}

