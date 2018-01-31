package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Florian on 22.01.2018.
 * Data Packet which contains all necessary control data
 */

public class ControlData {
    private float angularVelocity;
    private float x;
    private float y;

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "ControlData{" +
                "angularVelocity=" + angularVelocity +
                ", x=" + x +
                ", y=" + y +
                '}';
    }
}
