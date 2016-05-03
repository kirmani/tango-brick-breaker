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
        int width = 2;
        int iterations = 3;
        int weight = 1;
        Bitmap bitmap = generateRandomNoiseTexture(width, weight);
        Canvas canvas = new Canvas(bitmap);
        final Paint rectPaint = new Paint();
        rectPaint.setStyle(Style.FILL_AND_STROKE);
        rectPaint.setStrokeWidth(10);
        while (iterations > 0) {
            iterations--;
            width *= 2;
            weight *= 2;

            Bitmap finerNoise = generateRandomNoiseTexture(width, weight);
            canvas.drawBitmap(finerNoise, 0, 0, null);
        }

        return bitmap;
    }

    private static Bitmap generateRandomNoiseTexture(int size, int weight) {
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        Random random = new Random();
        final Paint rectPaint = new Paint();
        rectPaint.setStyle(Style.FILL_AND_STROKE);
        rectPaint.setStrokeWidth(10);
        for (int i = 0; i < size * size; i++) {
            for (int j = 0; j < size * size; j++) {
                int color = random.nextInt(256);
                rectPaint.setARGB(255 / weight, color, color, color);
                canvas.drawRect(i, j, i + 1, j + 1, rectPaint);
            }
        }
        return Bitmap.createScaledBitmap(bitmap, BITMAP_SIZE, BITMAP_SIZE, true);
    }
}

