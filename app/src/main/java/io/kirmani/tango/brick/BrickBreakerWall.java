/*
 * BrickBreakerWall.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.DiffuseMethod.Toon;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.RectangularPrism;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BrickBreakerWall extends Object3D {
    private static final String TAG = "BrickBreakerWall";

    private static final double ROW_BRICK_BUFFER = 0.005;
    private static final double COL_BRICK_BUFFER = 0.005;

    public static float WIDTH = 0.1f;
    public static float HEIGHT = 0.05f;
    public static float DEPTH = 0.05f;

    private static int[] COLORS = {
        0x00F44336, // Red
        0x00E91E63, // Pink
        0x00673AB7, // Deep Purple
        0x003F51B5, // Indigo
        0x0003A9F4, // Light Blue
        0x004CAF50, // Green
        0x00FF5722, // Deep Orange
    };

    private Set<RectangularPrism> mBricks;

    public BrickBreakerWall(Vector3 bottomLeft, Vector3 topRight) {
        super();
        mBricks = new HashSet<RectangularPrism>();
    }

    public Set<RectangularPrism> getBricks() {
        return mBricks;
    }

    public void setDimensions(Vector3 diagonal) {
        Log.d(TAG, String.format("Diagonal vector: %s", diagonal.toString()));
        generateWall((int) (diagonal.x / (WIDTH + COL_BRICK_BUFFER)),
                (int) (diagonal.y / (HEIGHT + ROW_BRICK_BUFFER)));
    }

    private void generateWall(int numRows, int numCols) {
        Log.d(TAG, String.format("Generating wall with %d rows and %d cols.", numRows, numCols));
        Random random = new Random();
        boolean right = true;
        boolean top = true;
        if (numRows < 0) {
            right = false;
        }
        if (numCols < 0) {
            top = false;
        }
        for (int i = 0; i < Math.abs(numRows); i++) {
            for (int j = 0; j < Math.abs(numCols); j++) {
                RectangularPrism brick = new RectangularPrism(WIDTH, HEIGHT, DEPTH);
                double xPos = i * (WIDTH + COL_BRICK_BUFFER);
                double yPos = j * (HEIGHT + ROW_BRICK_BUFFER);
                brick.setPosition(right ? -xPos : xPos, top ? -yPos : yPos, 0);
                int colorIndex = random.nextInt(COLORS.length);
                Material material = new Material();
                material.setColor(COLORS[colorIndex]);
                material.setColorInfluence(0.5f);
                material.enableLighting(true);
                material.setDiffuseMethod(new DiffuseMethod.Lambert());
                brick.setMaterial(material);
                mBricks.add(brick);
                addChild(brick);
            }
        }
    }
}

