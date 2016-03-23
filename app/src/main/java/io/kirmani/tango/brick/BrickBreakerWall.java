/*
 * BrickBreakerWall.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import org.rajawali3d.Object3D;

import java.util.ArrayList;
import java.util.List;

public class BrickBreakerWall extends Object3D {
    private static final float ROW_BRICK_BUFFER = 0.005f;
    private static final float COL_BRICK_BUFFER = 0.005f;

    private List<BrickBreakerBrick> mBricks;

    public BrickBreakerWall() {
        super();
        mBricks = new ArrayList<BrickBreakerBrick>();
        generateWall(10, 10);
    }

    public List<BrickBreakerBrick> getBricks() {
        return mBricks;
    }

    private void generateWall(int numRows, int numCols) {
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                BrickBreakerBrick brick = new BrickBreakerBrick();
                brick.setPosition(j * (BrickBreakerBrick.WIDTH + ROW_BRICK_BUFFER),
                        i * (BrickBreakerBrick.HEIGHT + COL_BRICK_BUFFER), 0);
                mBricks.add(brick);
                addChild(brick);
            }
        }
    }
}

