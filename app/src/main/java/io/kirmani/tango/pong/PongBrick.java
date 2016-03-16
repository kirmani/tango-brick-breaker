/*
 * PongBrick.java
 * Copyright (C) 2016 kirmani <sean@kirmani.io>
 *
 * Distributed under terms of the MIT license.
 */

package io.kirmani.tango.pong;

import org.rajawali3d.materials.Material;
import org.rajawali3d.materials.methods.DiffuseMethod;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;

import java.util.Random;

public class PongBrick extends Plane {
    public static float WIDTH = 0.1f;
    public static float HEIGHT = 0.05f;

    private static int[] COLORS = {
        0x00F44336, // Red
        0x00E91E63, // Pink
        0x00673AB7, // Deep Purple
        0x003F51B5, // Indigo
        0x0003A9F4, // Light Blue
        0x004CAF50, // Green
        0x00FF5722, // Deep Orange
    };

    public PongBrick() {
        super(WIDTH, HEIGHT, 1, 1);

        Random random = new Random();

        // Set-up a material: green with application of the light and
        // instructions.
        Material material = new Material();
        material.setColor(COLORS[random.nextInt(COLORS.length)]);
        material.setColorInfluence(0.5f);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        setMaterial(material);
    }

    public void registerHit() {
        Material material = new Material();
        material.setColor(0x00000000);
        material.setColorInfluence(0.5f);
        material.enableLighting(true);
        material.setDiffuseMethod(new DiffuseMethod.Lambert());
        setMaterial(material);
    }
}

