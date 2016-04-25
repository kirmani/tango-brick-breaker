/*
 * BoundingVolumeHierachy.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

public class BoundingVolumeHierarchy {
    private BoundingVolumeHierarchy mLeft;
    private BoundingVolumeHierarchy mRight;
    private List<Object3D> mObjects;
    private BoundingBox mBounds;

    public BoundingVolumeHierarchy(List<Object3D> objects) {
        mLeft = null;
        mRight = null;
        mObjects = new ArrayList<Object3D>();

        if (objects.size() == 0)
            return;

        // Compute Bounding Box
        mBounds = new BoundingBox(objects.get(0).getGeometry());
        for (Object3D object : objects) {
            // Merge bounds.
            BoundingBox objectBounds = object.getGeometry().getBoundingBox();
            Vector3 objectMin = objectBounds.getMin();
            Vector3 objectMax = objectBounds.getMax();
            Vector3 min = new Vector3(
                    (objectMin.x < mBounds.getMin().x) ? objectMin.x : mBounds.getMin().x,
                    (objectMin.y < mBounds.getMin().y) ? objectMin.y : mBounds.getMin().y,
                    (objectMin.z < mBounds.getMin().z) ? objectMin.z : mBounds.getMin().z);
            Vector3 max = new Vector3(
                    (objectMax.x < mBounds.getMax().x) ? objectMax.x : mBounds.getMax().x,
                    (objectMax.y < mBounds.getMax().y) ? objectMax.y : mBounds.getMax().y,
                    (objectMax.z < mBounds.getMax().z) ? objectMax.z : mBounds.getMax().z);
            mBounds.setMin(min);
            mBounds.setMax(max);
            // Add object.
            mObjects.add(object);
        }

        if (mObjects.size() == 1)
            return;

        // Find longest axis.
        Vector3 min = mBounds.getMin();
        Vector3 max = mBounds.getMax();
        Vector3 diff = Vector3.subtractAndCreate(max, min);
        int longestAxis = 0;
        double longestAxisLength = diff.x;
        if (diff.y > longestAxisLength) {
            longestAxisLength = diff.y;
            longestAxis = 1;
        }
        if (diff.z > longestAxisLength) {
            longestAxisLength = diff.z;
            longestAxis = 2;
        }

        // Split points into groups along the longest axis.
        sortObjectsAlongAxis(longestAxis);
        List<Object3D> leftObjects = new ArrayList<Object3D>();
        List<Object3D> rightObjects = new ArrayList<Object3D>();
        for (int i = 0; i < mObjects.size() / 2; i++) {
            leftObjects.add(mObjects.get(i));
        }
        for (int i = mObjects.size() / 2; i < mObjects.size(); i++) {
            rightObjects.add(mObjects.get(i));
        }

        // Create subtrees.
        mLeft = new BoundingVolumeHierarchy(leftObjects);
        mRight = new BoundingVolumeHierarchy(rightObjects);
    }

    private void sortObjectsAlongAxis(int axis) {
        boolean done = false;
        while (!done) {
            done = true;
            for (int i = 0; i < mObjects.size() - 1; i++) {
                Vector3 leftCenter = Vector3.addAndCreate(
                        mObjects.get(i).getGeometry().getBoundingBox().getMax(),
                        mObjects.get(i).getGeometry().getBoundingBox().getMin()).multiply(0.5);
                Vector3 rightCenter = Vector3.addAndCreate(
                        mObjects.get(i + 1).getGeometry().getBoundingBox().getMax(),
                        mObjects.get(i + 1).getGeometry().getBoundingBox().getMin()).multiply(0.5);
                if ((axis == 0 && leftCenter.x > rightCenter.x)
                        || (axis == 1 && leftCenter.y > rightCenter.y)
                        || (axis == 2 && leftCenter.z > rightCenter.z)) {
                    Object3D temp = mObjects.get(i);
                    mObjects.set(i, mObjects.get(i + 1));
                    mObjects.set(i + 1, temp);
                    done = false;
                }
            }
        }
    }
}

