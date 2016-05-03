/*
 * Copyright 2014 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kirmani.tango.brick;

import com.google.atap.tangoservice.TangoPoseData;

import android.content.Context;
import android.view.MotionEvent;
import android.util.Log;

import org.rajawali3d.Object3D;
import org.rajawali3d.bounds.IBoundingVolume;
import org.rajawali3d.lights.PointLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.primitives.RectangularPrism;
import org.rajawali3d.primitives.Sphere;

import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.rajawali.ar.TangoRajawaliRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

/**
 * Very simple example augmented reality renderer which displays a cube fixed in place.
 * Whenever the user clicks on the screen, the cube is placed flush with the surface detected
 * with the depth camera in the position clicked.
 * <p/>
 * This follows the same development model than any regular Rajawali application
 * with the following peculiarities:
 * - It extends <code>TangoRajawaliArRenderer</code>.
 * - It calls <code>super.initScene()</code> in the initialization.
 * - When an updated pose for the object is obtained after a user click, the object pose is updated
 * in the render loop
 * - The associated AugmentedRealityActivity is taking care of updating the camera pose to match
 * the displayed RGB camera texture and produce the AR effect through a Scene Frame Callback
 * (@see AugmentedRealityActivity)
 */
public class AugmentedRealityRenderer extends TangoRajawaliRenderer {
    private static final String TAG = AugmentedRealityRenderer.class.getSimpleName();
    private static final float SPHERE_RADIUS = 0.05f;
    private static final float CURSOR_RADIUS = 0.025f;
    private float mBallSpeed = 0.05f;

    private BrickBreakerWall mWall;
    private Pose mWallPose;
    private boolean mWallPoseUpdated = false;
    private PointLight mLight;

    private Sphere mBall;
    private Sphere mCursor;
    private List<Pose> mSelectedPoints;
    private Line3D mSelectionLine;

    public AugmentedRealityRenderer(Context context) {
        super(context);
        mCursor = new Sphere(CURSOR_RADIUS, 20, 20);
        mSelectedPoints = new ArrayList<Pose>();
    }

    @Override
    protected void initScene() {
        // Remember to call super.initScene() to allow TangoRajawaliArRenderer
        // to be set-up.
        super.initScene();

        // Add a directional light in an arbitrary direction.
        mLight = new PointLight();
        mLight.setColor(1, 1, 1);
        // mLight.setPower(0.8f);
        // mLight.setPosition(3, 2, 4);
        getCurrentScene().addLight(mLight);

        // Build a Cube and place it initially in the origin.
        mWall = new BrickBreakerWall(null, null);
        mWall.precomputeTextures();
        mBall = null;

        Material material = new Material();
        material.setColor(0x00F44336);
        material.setColorInfluence(0.5f);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        mCursor.setMaterial(material);
        getCurrentScene().addChild(mCursor);
    }

    public void onPreFrame() {
        mLight.setPosition(getCurrentScene().getCamera().getPosition());
        if (mWallPoseUpdated) {
            // Build wall and place at start coordinate.
            Quaternion wallOrientation = mSelectedPoints.get(0).getOrientation().clone()
                    .slerp(mSelectedPoints.get(1).getOrientation(), 0.5);
            Vector3 diagonal = Vector3.subtractAndCreate(mSelectedPoints.get(0).getPosition(),
                    mSelectedPoints.get(1).getPosition()).rotateBy(wallOrientation);
            mWall.setDimensions(diagonal);
            mWall.setPosition(mSelectedPoints.get(0).getPosition());
            mWall.setOrientation(wallOrientation);
            mWallPoseUpdated = false;
        }
        if (mBall != null) {
            // BoundingBox boundingBox = mBall.getGeometry().getBoundingBox();
            // // boundingBox.transform(mWall.getModelMatrix());
            // Object3D brick = mWall.intersectsWith(boundingBox);
            // if (brick != null) {
            //     Quaternion normal = brick.getOrientation().clone()
            //             .multiplyLeft(mWall.getOrientation());
            //     Quaternion newOrientation = mBall.getOrientation().clone()
            //         .slerp(normal, 0.5);
            //     mBall.setOrientation(normal);

            //     // Remove brick.
            //     // iter.remove();
            //     mWall.removeChild(brick);
            // }
            for (Iterator<RectangularPrism> iter = mWall.getBricks().iterator(); iter.hasNext();) {
                RectangularPrism brick = iter.next();
                IBoundingVolume boundingBox = brick.getGeometry().getBoundingSphere();
                if (boundingBox.intersectsWith(mBall.getGeometry().getBoundingSphere())) {
                    Quaternion normal = brick.getOrientation().clone()
                            .multiplyLeft(mWall.getOrientation());
                    Quaternion newOrientation = mBall.getOrientation().clone()
                        .slerp(normal, 0.5);
                    mBall.setOrientation(normal);

                    // Remove brick.
                    iter.remove();
                    mWall.removeChild(brick);
                }
            }
            if (Vector3.distanceTo2(getCurrentCamera().getPosition(),
                        mBall.getPosition()) < 0.25f) {
                Quaternion normal = getCurrentCamera().getOrientation().clone();
                Quaternion yFlip = new Quaternion(Vector3.Y, 180.0);
                normal.multiplyLeft(yFlip);
                normal.setAll(normal.w, -normal.x, -normal.y, -normal.z);
                mBall.setOrientation(normal);
            }
            mBall.moveForward(mBallSpeed);
            if (Vector3.distanceTo2(getCurrentCamera().getPosition(), mWall.getPosition())
                    < Vector3.distanceTo2(mBall.getPosition(), mWall.getPosition())) {
                getCurrentScene().removeChild(mBall);
                mBall = null;
            }
        }
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Update the AR object if necessary
        // Synchronize against concurrent access with the setter below.
        super.onRender(elapsedRealTime, deltaTime);
    }

    /**
     * Save the updated plane fit pose to update the AR object on the next render pass.
     * This is synchronized against concurrent access in the render loop above.
     */
    public synchronized void updateObjectPose(TangoPoseData planeFitPose) {
        getCurrentScene().removeChild(mCursor);
        mWallPose = ScenePoseCalculator.toOpenGLPose(planeFitPose);
        getCurrentScene().addChild(mWall);
        mWallPoseUpdated = true;
    }

    public synchronized void drawCursor(Vector3 position) {
        if (position != null) {
            mCursor.setPosition(position);
        }
    }

    public synchronized void fireBall() {
        if (mBall == null) {
            mBall = new Sphere(SPHERE_RADIUS, 20, 20);
            Material material = new Material();
            material.setColor(0x00FF9800);
            material.setColorInfluence(0.5f);
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            mBall.setMaterial(material);
            mBall.setPosition(getCurrentCamera().getPosition());
            Quaternion yFlip = new Quaternion(Vector3.Y, 180.0);
            mBall.setOrientation(yFlip.multiply(getCurrentCamera().getOrientation()));
            mBall.moveForward(0.5f);
            getCurrentScene().addChild(mBall);
        }
    }

    public void startSelecting(TangoPoseData start) {
        mSelectedPoints.add(ScenePoseCalculator.toOpenGLPose(start));
        mSelectedPoints.add(ScenePoseCalculator.toOpenGLPose(start));
        Stack<Vector3> points = new Stack<Vector3>();
        points.push(mSelectedPoints.get(0).getPosition());
        points.push(mSelectedPoints.get(1).getPosition());
        mSelectionLine = new Line3D(points, 50);
        Material material = new Material();
        material.setColor(0xffffff00);
        material.setColorInfluence(0.5f);
        mSelectionLine.setMaterial(material);
        getCurrentScene().addChild(mSelectionLine);
    }

    public void setEndSelection(TangoPoseData end) {
        if (end != null) {
            getCurrentScene().removeChild(mSelectionLine);
            mSelectedPoints.remove(1);
            mSelectedPoints.add(ScenePoseCalculator.toOpenGLPose(end));
            Stack<Vector3> points = new Stack<Vector3>();
            points.push(mSelectedPoints.get(0).getPosition());
            points.push(mSelectedPoints.get(1).getPosition());
            mSelectionLine = new Line3D(points, 50);
            Material material = new Material();
            material.setColor(0xffffff00);
            material.setColorInfluence(0.5f);
            mSelectionLine.setMaterial(material);
            getCurrentScene().addChild(mSelectionLine);
            Log.d(TAG, String.format("Selection vector: %s -> %s", mSelectionLine.getPoint(0),
                    mSelectionLine.getPoint(1)));
        }
    }

    public void endSelecting() {
        getCurrentScene().removeChild(mSelectionLine);
    }

    /**
     * Update the scene camera based on the provided pose in Tango start of service frame.
     * The device pose should match the pose of the device at the time the last rendered RGB
     * frame, which can be retrieved with this.getTimestamp();
     * <p/>
     * NOTE: This must be called from the OpenGL render thread - it is not thread safe.
     */
    public void updateRenderCameraPose(TangoPoseData devicePose, DeviceExtrinsics extrinsics) {
        Pose cameraPose = ScenePoseCalculator.toOpenGlCameraPose(devicePose, extrinsics);
        getCurrentCamera().setRotation(cameraPose.getOrientation());
        getCurrentCamera().setPosition(cameraPose.getPosition());
    }

    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {
    }

    @Override
    public void onTouchEvent(MotionEvent event) {

    }
}
