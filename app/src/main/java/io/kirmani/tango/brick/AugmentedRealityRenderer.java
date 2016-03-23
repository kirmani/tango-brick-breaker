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
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Sphere;

import com.projecttango.rajawali.DeviceExtrinsics;
import com.projecttango.rajawali.Pose;
import com.projecttango.rajawali.ScenePoseCalculator;
import com.projecttango.rajawali.ar.TangoRajawaliRenderer;

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
    private float mBallSpeed = 0.05f;

    private BrickBreakerWall mWall;
    private Pose mWallPose;
    private boolean mWallPoseUpdated = false;

    private Sphere mBall;

    public AugmentedRealityRenderer(Context context) {
        super(context);
    }

    @Override
    protected void initScene() {
        // Remember to call super.initScene() to allow TangoRajawaliArRenderer
        // to be set-up.
        super.initScene();

        // Add a directional light in an arbitrary direction.
        DirectionalLight light = new DirectionalLight(1, 0.2, -1);
        light.setColor(1, 1, 1);
        light.setPower(0.8f);
        light.setPosition(3, 2, 4);
        getCurrentScene().addLight(light);

        // Build a Cube and place it initially in the origin.
        mWall = new BrickBreakerWall();
        mBall = null;
    }

    @Override
    protected void onRender(long elapsedRealTime, double deltaTime) {
        // Update the AR object if necessary
        // Synchronize against concurrent access with the setter below.
        synchronized (this) {
            if (mWallPoseUpdated) {
                // Place the 3D object in the location of the detected plane.
                mWall.setPosition(mWallPose.getPosition());
                mWall.setOrientation(mWallPose.getOrientation());
                // Move it forward by half of the size of the cube to make it
                // flush with the plane surface.
                mWallPoseUpdated = false;
            }
            if (mBall != null) {
                for (BrickBreakerBrick brick : mWall.getBricks()) {
                    if (Vector3.distanceTo(mBall.getPosition(),
                                brick.getPosition().invertAndCreate()
                                .add(mWall.getPosition())) < 0.1f) {
                        Quaternion normal = brick.getOrientation().clone()
                                .multiplyLeft(mWall.getOrientation());
                        Quaternion newOrientation = mBall.getOrientation().clone()
                            .slerp(normal, 0.5f);
                        mBall.setOrientation(normal);
                        brick.registerHit();
                    }
                }
                if (Vector3.distanceTo(getCurrentCamera().getPosition(),
                            mBall.getPosition()) < 0.5f) {
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

        super.onRender(elapsedRealTime, deltaTime);
    }

    /**
     * Save the updated plane fit pose to update the AR object on the next render pass.
     * This is synchronized against concurrent access in the render loop above.
     */
    public synchronized void updateObjectPose(TangoPoseData planeFitPose) {
        mWallPose = ScenePoseCalculator.toOpenGLPose(planeFitPose);
        getCurrentScene().addChild(mWall);
        mWallPoseUpdated = true;
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
