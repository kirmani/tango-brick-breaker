/*
 * BrickBreakerWall.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import org.rajawali3d.Object3D;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.methods.DiffuseMethod.Toon;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.RectangularPrism;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BrickBreakerWall extends Object3D {
    private static final float ROW_BRICK_BUFFER = 0.005f;
    private static final float COL_BRICK_BUFFER = 0.005f;

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

    public BrickBreakerWall() {
        super();
        mBricks = new HashSet<RectangularPrism>();
        generateWall(10, 10);
    }

    public Set<RectangularPrism> getBricks() {
        return mBricks;
    }

    private void generateWall(int numRows, int numCols) {
        Random random = new Random();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                RectangularPrism brick = new RectangularPrism(WIDTH, HEIGHT, DEPTH);
                brick.setPosition(j * (WIDTH + ROW_BRICK_BUFFER),
                        i * (HEIGHT + COL_BRICK_BUFFER), 0);
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

