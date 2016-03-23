/*
 * BrickBreakerBrick.java
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
import org.rajawali3d.math.Quaternion;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Plane;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BrickBreakerBrick extends Object3D {
    public static float WIDTH = 0.1f;
    public static float HEIGHT = 0.05f;
    public static float DEPTH = 0.05f;

    private Set<Plane> mPlanes;

    private static int[] COLORS = {
        0x00F44336, // Red
        0x00E91E63, // Pink
        0x00673AB7, // Deep Purple
        0x003F51B5, // Indigo
        0x0003A9F4, // Light Blue
        0x004CAF50, // Green
        0x00FF5722, // Deep Orange
    };

    public BrickBreakerBrick() {
        mPlanes = new HashSet<Plane>();

        Plane back = new Plane(WIDTH, HEIGHT, 1, 1);
        Quaternion flip = new Quaternion(Vector3.Y, 180.0);
        back.setOrientation(back.getOrientation().multiplyLeft(flip));
        back.moveForward(DEPTH / 2.0f);
        mPlanes.add(back);
        addChild(back);

        Plane front = new Plane(WIDTH, HEIGHT, 1, 1);
        front.moveForward(DEPTH / 2.0f);
        mPlanes.add(front);
        addChild(front);

        Plane left = new Plane(DEPTH, HEIGHT, 1, 1);
        flip = new Quaternion(Vector3.Y, 90.0);
        left.setOrientation(left.getOrientation().multiplyLeft(flip));
        left.moveForward(WIDTH / 2.0f);
        mPlanes.add(left);
        addChild(left);

        Plane right = new Plane(DEPTH, HEIGHT, 1, 1);
        flip = new Quaternion(Vector3.Y, 270.0);
        right.setOrientation(right.getOrientation().multiplyLeft(flip));
        right.moveForward(WIDTH / 2.0f);
        mPlanes.add(right);
        addChild(right);

        Plane top = new Plane(WIDTH, DEPTH, 1, 1);
        flip = new Quaternion(Vector3.X, 90.0);
        top.setOrientation(top.getOrientation().multiplyLeft(flip));
        top.moveForward(HEIGHT / 2.0f);
        mPlanes.add(top);
        addChild(top);

        Plane bottom = new Plane(WIDTH, DEPTH, 1, 1);
        flip = new Quaternion(Vector3.X, 270.0);
        bottom.setOrientation(bottom.getOrientation().multiplyLeft(flip));
        bottom.moveForward(HEIGHT / 2.0f);
        mPlanes.add(bottom);
        addChild(bottom);

        // Set-up a material: green with application of the light and
        // instructions.
        Random random = new Random();
        int colorIndex = random.nextInt(COLORS.length);
        for (Plane plane : mPlanes) {
            Material material = new Material();
            material.setColor(COLORS[colorIndex]);
            material.setColorInfluence(0.5f);
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            plane.setMaterial(material);
        }
    }

    public void registerHit() {
        for (Plane plane : mPlanes) {
            Material material = new Material();
            material.setColor(0x00000000);
            material.setColorInfluence(0.5f);
            material.enableLighting(true);
            material.setDiffuseMethod(new DiffuseMethod.Lambert());
            plane.setMaterial(material);
        }
    }
}

