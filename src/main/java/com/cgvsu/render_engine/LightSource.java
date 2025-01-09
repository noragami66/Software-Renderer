package com.cgvsu.render_engine;


import com.cgvsu.math.vector.Vector3f;

public class LightSource {
    private final String name;
    private final Vector3f position;

    public LightSource(String name, Vector3f position) {
        this.name = name;
        this.position = position;
    }

    public String getName() {
        return name;
    }

    public Vector3f getPosition() {
        return position;
    }
}

