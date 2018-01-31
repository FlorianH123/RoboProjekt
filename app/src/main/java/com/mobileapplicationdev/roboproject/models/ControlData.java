package com.mobileapplicationdev.roboproject.models;

/**
 * Created by Florian on 22.01.2018.
 * Data Packet which contains all necessary control data
 */

public class ControlData {
    private float angularVelocity;

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
    }

    @Override
    public String toString() {
        return "ControlData{" +
                "angularVelocity=" + angularVelocity +
                '}';
    }
}
