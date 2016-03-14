/*
 * PongWall.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.pong;

import org.rajawali3d.Object3D;

public class PongWall extends Object3D {
    private static final float ROW_BRICK_BUFFER = 0.05f;
    private static final float COL_BRICK_BUFFER = 0.05f;

    public PongWall() {
        super();
        generateWall(10, 10);
    }

    private void generateWall(int numRows, int numCols) {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Object3D mObject = new PongBrick();
                mObject.setPosition(j * PongBrick.WIDTH - ROW_BRICK_BUFFER,
                        i * PongBrick.HEIGHT - COL_BRICK_BUFFER, 0);
                addChild(mObject);
            }
        }
    }
}

