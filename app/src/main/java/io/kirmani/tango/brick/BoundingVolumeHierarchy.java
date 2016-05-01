/*
 * BoundingVolumeHierachy.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.brick;

import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.BoundingBox;
import org.rajawali3d.math.vector.Vector3;

import java.util.ArrayList;
import java.util.List;

public class BoundingVolumeHierarchy<T extends Object3D> {
    private static final String TAG = "BoundingVolumeHierarchy";

    private BoundingVolumeHierarchy mLeft;
    private BoundingVolumeHierarchy mRight;
    private List<T> mObjects;
    private BoundingBox mBounds;

    public BoundingVolumeHierarchy(List<T> objects) {
        mLeft = null;
        mRight = null;
        mObjects = new ArrayList<T>();

        if (objects.size() == 0)
            return;

        // Compute Bounding Box
        mBounds = new BoundingBox(objects.get(0).getGeometry());
        mBounds.transform(objects.get(0).getModelMatrix());
        for (T object : objects) {
            // Merge bounds.
            BoundingBox objectBounds = object.getGeometry().getBoundingBox();
            objectBounds.transform(object.getModelMatrix());
            Log.d(TAG, String.format("Object bounds: %s", objectBounds.toString()));
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
        List<T> leftObjects = new ArrayList<T>();
        List<T> rightObjects = new ArrayList<T>();
        for (int i = 0; i < mObjects.size() / 2; i++) {
            leftObjects.add(mObjects.get(i));
        }
        for (int i = mObjects.size() / 2; i < mObjects.size(); i++) {
            rightObjects.add(mObjects.get(i));
        }

        Log.d(TAG, "Building BVH!");
        Log.d(TAG, String.format("Number of Objects on Left: %d", leftObjects.size()));
        Log.d(TAG, String.format("Number of Objects on Right: %d", rightObjects.size()));
        Log.d(TAG, String.format("Longest axis: %d", longestAxis));
        Log.d(TAG, String.format("Longest axis length: %3.4f", longestAxisLength));
        Log.d(TAG, String.format("Longest axis min: %3.4f", (longestAxis == 0)
                ? min.x : (longestAxis == 1) ? min.y : min.z));
        Log.d(TAG, String.format("Longest axis max: %3.4f", (longestAxis == 0)
                ? max.x : (longestAxis == 1) ? max.y : max.z));

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
                    T temp = mObjects.get(i);
                    mObjects.set(i, mObjects.get(i + 1));
                    mObjects.set(i + 1, temp);
                    done = false;
                }
            }
        }
    }

    public T intersectsWith(BoundingBox otherBoundingBox) {
        Log.d(TAG, "Checking if intersection happened within bounding box...");
        Log.d(TAG, otherBoundingBox.toString());
        if (mBounds.intersectsWith(otherBoundingBox)) {
            Log.d(TAG, "Intersection happened within bounding box.");
            // Intersection happened.
            if (mLeft == null && mRight == null) {
                // Leaf node.
                return mObjects.get(0).getGeometry().getBoundingBox()
                    .intersectsWith(otherBoundingBox) ? mObjects.get(0) : null;
            }
            if (mLeft != null) {
                T leftIntersected = (T) mLeft.intersectsWith(otherBoundingBox);
                if (leftIntersected != null) {
                    return leftIntersected;
                }
            }
            if (mRight != null) {
                T rightIntersected = (T) mRight.intersectsWith(otherBoundingBox);
                if (rightIntersected != null) {
                    return rightIntersected;
                }
            }
        }
        return null;
    }
}

