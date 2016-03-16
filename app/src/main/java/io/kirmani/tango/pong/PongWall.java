/*
 * PongWall.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.pong;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

public class PongWall extends Object3D {
    private static final float ROW_BRICK_BUFFER = 0.05f;
    private static final float COL_BRICK_BUFFER = 0.05f;

    private List<PongBrick> mBricks;

    public PongWall() {
        super();
        mBricks = new ArrayList<PongBrick>();
        generateWall(10, 10);
    }

    public List<PongBrick> getBricks() {
        return mBricks;
    }

    private void generateWall(int numRows, int numCols) {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                PongBrick brick = new PongBrick();
                brick.setPosition(j * PongBrick.WIDTH,
                        i * PongBrick.HEIGHT, 0);
                mBricks.add(brick);
                addChild(brick);
            }
        }
    }
}

